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
    private const val TECH_PENALTY = -15 // Penalty for tech-related content
    
    /**
     * Calculates FitScore for a suggestion based on user context.
     * 
     * @param suggestion The API suggestion to score
     * @param context The current user context
     * @return FitScore from 0-100
     */
    // Tech exclusion keywords (same as ApiRepository and HabitClassifier)
    private val techExclusionKeywords = listOf(
        "app", "application", "software", "platform", "saas", "api", "sdk",
        "code", "programming", "developer", "tech", "technology", "startup",
        "vc", "venture capital", "funding", "ai model", "llm", "gpt", "chatbot"
    )
    
    fun calculate(suggestion: ApiSuggestion, context: UserContext): Int {
        var score = BASE_SCORE
        val category = suggestion.category ?: HabitCategory.GENERAL
        
        // Penalize tech content (-15) - Check title and content for tech keywords
        val titleAndContent = (suggestion.title + " " + (suggestion.content ?: "")).lowercase()
        val hasTechKeywords = techExclusionKeywords.any { titleAndContent.contains(it) }
        if (hasTechKeywords) {
            // Only penalize if it's clearly tech-focused, not just mentioning tech in health context
            val hasHealthContext = titleAndContent.contains("health") || 
                    titleAndContent.contains("fitness") || 
                    titleAndContent.contains("wellness") ||
                    titleAndContent.contains("nutrition") ||
                    titleAndContent.contains("meditation")
            
            if (!hasHealthContext) {
                score += TECH_PENALTY
                android.util.Log.d("FitScoreCalculator", "Applied tech penalty to: '${suggestion.title}'")
            }
        }
        
        // Category match (+20) - Most important factor
        if (category in context.preferredCategories) {
            score += CATEGORY_MATCH_BONUS
        }
        
        // Boost score for specific categories that are more actionable
        // Health/wellness categories get higher bonuses
        when (category) {
            HabitCategory.FITNESS -> score += 12 // Strongly prioritize fitness content
            HabitCategory.WELLNESS -> score += 11 // Wellness is highly actionable
            HabitCategory.HEALTHY_EATING -> score += 11 // Nutrition and food habits should stay near the top
            HabitCategory.PRODUCTIVITY -> score += 6 // Still highlight high-impact productivity reads
            HabitCategory.LEARNING -> score += 3 // Light boost for learning-related content
            HabitCategory.GENERAL -> score -= 10 // Push generic content further down
        }
        
        // Time appropriateness (+15)
        if (isTimeAppropriate(suggestion, context.currentTime)) {
            score += TIME_APPROPRIATE_BONUS
        }
        
        // Weather appropriateness (+10) - optional
        context.currentWeather?.let { weather ->
            if (isWeatherAppropriate(category, weather)) {
                score += WEATHER_APPROPRIATE_BONUS
            } else {
                if (category == HabitCategory.FITNESS) {
                    // Outdoor workouts are less appealing in bad weather
                    score -= 5
                } else if (isBadOutdoorWeather(weather)) {
                    // Rainy/snowy weather makes indoor habits (reading, meditation, cooking) more appealing
                    score += 5
                }
            }
        }
        
        // Location appropriateness (+5)
        if (context.currentLocation != null && 
            isLocationAppropriate(suggestion, context.currentLocation)) {
            score += LOCATION_APPROPRIATE_BONUS
        }
        
        // Motion state match (+10)
        if (isMotionStateAppropriate(category, context.recentMotionState)) {
            score += MOTION_STATE_MATCH_BONUS
        }
        
        // Boost score if title contains actionable words (makes it a better habit)
        val titleLower = (suggestion.title ?: "").lowercase()
        val actionableWords = listOf(
            "daily", "every day", "routine", "practice", "habit", "exercise", "workout", 
            "meditation", "read", "learn", "cook", "meal prep", "healthy eating", "nutrition"
        )
        if (actionableWords.any { titleLower.contains(it) }) {
            score += 5
        }
        
        val nutritionBoostWords = listOf(
            "supplement", "supplements", "vitamin", "vitamins", "protein", "recipe",
            "meal prep", "diet", "keto", "plant-based", "mediterranean", "hydration"
        )
        if (nutritionBoostWords.any { titleAndContent.contains(it) }) {
            score += 4
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Checks if a suggestion is time-appropriate.
     * Fitness activities are better in morning/evening, wellness in evening, etc.
     */
    private fun isTimeAppropriate(suggestion: ApiSuggestion, currentTime: LocalTime): Boolean {
        val hour = currentTime.hour
        val category = suggestion.category ?: HabitCategory.GENERAL
        return when (category) {
            HabitCategory.FITNESS -> {
                // Fitness: better in morning (6-10) or evening (17-20)
                hour in 6..10 || hour in 17..20
            }
            HabitCategory.WELLNESS -> {
                // Wellness: better in morning (6-9) or evening (18-22)
                hour in 6..9 || hour in 18..22
            }
            HabitCategory.HEALTHY_EATING -> {
                // Healthy eating: better around meal times (7-9 breakfast, 12-14 lunch, 18-20 dinner)
                hour in 7..9 || hour in 12..14 || hour in 18..20
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
        category: HabitCategory,
        weather: com.microhabitcoach.data.model.Weather
    ): Boolean {
        return when (category) {
            HabitCategory.FITNESS -> {
                weather.condition in listOf(
                com.microhabitcoach.data.model.WeatherCondition.SUNNY,
                com.microhabitcoach.data.model.WeatherCondition.CLOUDY
            )
        }
            else -> true // Other categories are generally weather-independent
        }
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
        category: HabitCategory,
        motionState: MotionState
    ): Boolean {
        return when (category) {
            HabitCategory.FITNESS -> {
                // Fitness activities match when user is walking or running
                motionState in listOf(MotionState.WALKING, MotionState.RUNNING)
            }
            HabitCategory.WELLNESS -> {
                // Wellness activities (meditation, etc.) match when stationary
                motionState == MotionState.STATIONARY
            }
            HabitCategory.HEALTHY_EATING -> {
                // Healthy eating activities match when stationary (cooking, meal prep, eating)
                motionState == MotionState.STATIONARY
            }
            HabitCategory.PRODUCTIVITY, HabitCategory.LEARNING -> {
                // Productivity/learning match when stationary
                motionState == MotionState.STATIONARY
            }
            HabitCategory.GENERAL -> true // Always appropriate
        }
    }

    private fun isBadOutdoorWeather(weather: com.microhabitcoach.data.model.Weather): Boolean {
        return weather.condition in listOf(
            com.microhabitcoach.data.model.WeatherCondition.RAINY,
            com.microhabitcoach.data.model.WeatherCondition.SNOWY,
            com.microhabitcoach.data.model.WeatherCondition.WINDY
        )
    }
}

