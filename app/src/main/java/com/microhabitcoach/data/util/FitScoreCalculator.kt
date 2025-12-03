package com.microhabitcoach.data.util

import android.location.Location
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.MotionState
import com.microhabitcoach.data.model.UserContext
import java.time.LocalTime

/**
 * Calculates FitScore (0-100) for habit suggestions based on user context.
 * Higher scores indicate better fit for the user's current situation.
 */
object FitScoreCalculator {
    
    private const val BASE_SCORE = 50
    private const val CATEGORY_MATCH_BONUS = 20
    private const val TIME_APPROPRIATE_BONUS = 15
    private const val WEATHER_APPROPRIATE_BONUS = 10
    private const val LOCATION_APPROPRIATE_BONUS = 5
    private const val MOTION_STATE_MATCH_BONUS = 10
    
    /**
     * Calculates FitScore for a suggestion based on user context.
     * 
     * @param suggestion The API suggestion to score
     * @param context The current user context
     * @return FitScore from 0-100
     */
    fun calculate(suggestion: ApiSuggestion, context: UserContext): Int {
        var score = BASE_SCORE
        
        // Category match (+20) - Most important factor
        if (suggestion.category in context.preferredCategories) {
            score += CATEGORY_MATCH_BONUS
        }
        
        // Boost score for specific categories that are more actionable
        when (suggestion.category) {
            HabitCategory.FITNESS -> score += 5 // Fitness habits are highly actionable
            HabitCategory.WELLNESS -> score += 5 // Wellness habits are highly actionable
            HabitCategory.PRODUCTIVITY -> score += 3 // Productivity habits are actionable
            HabitCategory.LEARNING -> score += 2 // Learning habits are actionable
            HabitCategory.GENERAL -> score -= 5 // General is less specific, reduce score
        }
        
        // Time appropriateness (+15)
        if (isTimeAppropriate(suggestion, context.currentTime)) {
            score += TIME_APPROPRIATE_BONUS
        }
        
        // Weather appropriateness (+10) - optional
        if (context.currentWeather != null && 
            isWeatherAppropriate(suggestion, context.currentWeather)) {
            score += WEATHER_APPROPRIATE_BONUS
        }
        
        // Location appropriateness (+5)
        if (context.currentLocation != null && 
            isLocationAppropriate(suggestion, context.currentLocation)) {
            score += LOCATION_APPROPRIATE_BONUS
        }
        
        // Motion state match (+10)
        if (isMotionStateAppropriate(suggestion, context.recentMotionState)) {
            score += MOTION_STATE_MATCH_BONUS
        }
        
        // Boost score if title contains actionable words (makes it a better habit)
        val titleLower = suggestion.title.lowercase()
        val actionableWords = listOf("daily", "every day", "routine", "practice", "habit", "exercise", "workout", "meditation", "read", "learn")
        if (actionableWords.any { titleLower.contains(it) }) {
            score += 5
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Checks if a suggestion is time-appropriate.
     * Fitness activities are better in morning/evening, wellness in evening, etc.
     */
    private fun isTimeAppropriate(suggestion: ApiSuggestion, currentTime: LocalTime): Boolean {
        val hour = currentTime.hour
        
        return when (suggestion.category) {
            HabitCategory.FITNESS -> {
                // Fitness: better in morning (6-10) or evening (17-20)
                hour in 6..10 || hour in 17..20
            }
            HabitCategory.WELLNESS -> {
                // Wellness: better in morning (6-9) or evening (18-22)
                hour in 6..9 || hour in 18..22
            }
            HabitCategory.PRODUCTIVITY -> {
                // Productivity: better during work hours (9-17)
                hour in 9..17
            }
            HabitCategory.LEARNING -> {
                // Learning: better during day (9-18)
                hour in 9..18
            }
            HabitCategory.GENERAL -> true // Always appropriate
        }
    }
    
    /**
     * Checks if a suggestion is weather-appropriate.
     * Outdoor activities need good weather, indoor activities are always fine.
     */
    private fun isWeatherAppropriate(
        suggestion: ApiSuggestion,
        weather: com.microhabitcoach.data.model.Weather
    ): Boolean {
        // For MVP, we'll consider fitness/outdoor activities need good weather
        if (suggestion.category == HabitCategory.FITNESS) {
            return weather.condition in listOf(
                com.microhabitcoach.data.model.WeatherCondition.SUNNY,
                com.microhabitcoach.data.model.WeatherCondition.CLOUDY
            )
        }
        // Other categories are generally weather-independent
        return true
    }
    
    /**
     * Checks if a suggestion is location-appropriate.
     * Some activities are better at home, gym, outdoors, etc.
     */
    private fun isLocationAppropriate(
        suggestion: ApiSuggestion,
        location: Location
    ): Boolean {
        // For MVP, we'll use a simple heuristic:
        // If the suggestion mentions "home", prefer being at home
        // If it mentions "gym", prefer being near a gym (we'd need gym locations)
        // For now, we'll return true for most cases (can be enhanced later)
        return true
    }
    
    /**
     * Checks if a suggestion matches the user's current motion state.
     * Fitness activities match walking/running, stationary activities match stationary state.
     */
    private fun isMotionStateAppropriate(
        suggestion: ApiSuggestion,
        motionState: MotionState
    ): Boolean {
        return when (suggestion.category) {
            HabitCategory.FITNESS -> {
                // Fitness activities match when user is walking or running
                motionState in listOf(MotionState.WALKING, MotionState.RUNNING)
            }
            HabitCategory.WELLNESS -> {
                // Wellness activities (meditation, etc.) match when stationary
                motionState == MotionState.STATIONARY
            }
            HabitCategory.PRODUCTIVITY, HabitCategory.LEARNING -> {
                // Productivity/learning match when stationary
                motionState == MotionState.STATIONARY
            }
            HabitCategory.GENERAL -> true // Always appropriate
        }
    }
}

