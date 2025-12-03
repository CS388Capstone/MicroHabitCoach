package com.microhabitcoach.data.util

import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType

/**
 * Infers the most appropriate HabitType for a suggestion based on its content.
 */
object HabitTypeInferrer {
    
    // Motion-related keywords
    private val motionKeywords = listOf(
        "walk", "walking", "run", "running", "jog", "jogging", "bike", "cycling",
        "exercise", "workout", "gym", "cardio", "sprint", "marathon", "hike", "hiking"
    )
    
    // Location-related keywords
    private val locationKeywords = listOf(
        "at home", "at gym", "at work", "at office", "at park", "at beach",
        "location", "place", "where", "venue", "spot", "geofence"
    )
    
    // Time-related keywords (less specific, but can indicate time-based habits)
    private val timeKeywords = listOf(
        "daily", "every day", "morning", "evening", "night", "afternoon",
        "schedule", "reminder", "alarm", "time", "when", "routine"
    )
    
    /**
     * Infers the HabitType for a suggestion based on its title and content.
     * 
     * @param suggestion The API suggestion to analyze
     * @return The inferred HabitType (defaults to TIME if unclear)
     */
    fun inferType(suggestion: ApiSuggestion): HabitType {
        val text = (suggestion.title + " " + (suggestion.content ?: "")).lowercase()
        
        // Check for motion keywords first (most specific)
        if (motionKeywords.any { text.contains(it) }) {
            return HabitType.MOTION
        }
        
        // Check for location keywords
        if (locationKeywords.any { text.contains(it) }) {
            return HabitType.LOCATION
        }
        
        // Check for time keywords
        if (timeKeywords.any { text.contains(it) }) {
            return HabitType.TIME
        }
        
        // Category-based inference as fallback
        return when (suggestion.category) {
            HabitCategory.FITNESS -> HabitType.MOTION // Fitness activities are often motion-based
            HabitCategory.WELLNESS -> HabitType.TIME // Wellness activities are often time-based
            HabitCategory.PRODUCTIVITY -> HabitType.TIME // Productivity habits are often time-based
            HabitCategory.LEARNING -> HabitType.TIME // Learning habits are often time-based
            HabitCategory.GENERAL -> HabitType.TIME // Default to time-based
        }
    }
    
    /**
     * Suggests default parameters for a habit based on its type and category.
     * 
     * @param suggestion The API suggestion
     * @param type The inferred HabitType
     * @return A map of suggested parameters
     */
    fun suggestParameters(suggestion: ApiSuggestion, type: HabitType): Map<String, Any?> {
        return when (type) {
            HabitType.MOTION -> {
                mapOf(
                    "motionType" to inferMotionType(suggestion),
                    "duration" to 30 // Default 30 minutes for motion habits
                )
            }
            HabitType.LOCATION -> {
                mapOf(
                    "radius" to 100f // Default 100 meters for geofence
                )
            }
            HabitType.TIME -> {
                mapOf(
                    "reminderTimes" to emptyList<Int>(), // User can add times
                    "reminderDays" to emptyList<Int>() // User can select days
                )
            }
        }
    }
    
    /**
     * Infers the motion type (walk, run, stationary) from suggestion content.
     */
    private fun inferMotionType(suggestion: ApiSuggestion): String {
        val text = (suggestion.title + " " + (suggestion.content ?: "")).lowercase()
        
        return when {
            text.contains("run") || text.contains("running") || text.contains("sprint") -> "Run"
            text.contains("walk") || text.contains("walking") || text.contains("hike") -> "Walk"
            else -> "Walk" // Default to walk
        }
    }
}

