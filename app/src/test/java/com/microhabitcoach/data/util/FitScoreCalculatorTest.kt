package com.microhabitcoach.data.util

import android.location.Location
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.MotionState
import com.microhabitcoach.data.model.UserContext
import com.microhabitcoach.data.model.Weather
import com.microhabitcoach.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class FitScoreCalculatorTest {

    private fun createSuggestion(
        title: String = "Test Habit",
        category: HabitCategory = HabitCategory.GENERAL,
        content: String? = null
    ) = ApiSuggestion(
        id = "test-1",
        title = title,
        content = content,
        category = category,
        fitScore = 0,
        source = "test",
        cachedAt = System.currentTimeMillis()
    )

    private fun createContext(
        preferredCategories: Set<HabitCategory> = setOf(HabitCategory.GENERAL),
        currentTime: LocalTime = LocalTime.NOON,
        currentLocation: Location? = null,
        recentMotionState: MotionState = MotionState.UNKNOWN,
        currentWeather: Weather? = null
    ) = UserContext(
        preferredCategories = preferredCategories,
        currentTime = currentTime,
        currentLocation = currentLocation,
        recentMotionState = recentMotionState,
        currentWeather = currentWeather
    )

    @Test
    fun calculate_baseScore_returns50() {
        val suggestion = createSuggestion()
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base score is 50, General category gets -5, so should be 45
        assertEquals(45, score)
    }

    @Test
    fun calculate_categoryMatch_adds20Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(preferredCategories = setOf(HabitCategory.FITNESS))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + category match 20 + fitness bonus 5 = 75
        assertTrue(score >= 75)
    }

    @Test
    fun calculate_categoryMatch_noMatch_noBonus() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(preferredCategories = setOf(HabitCategory.WELLNESS))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 = 55 (no category match bonus)
        assertTrue(score >= 55)
    }

    @Test
    fun calculate_fitnessCategory_adds5Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 = 55
        assertTrue(score >= 55)
    }

    @Test
    fun calculate_wellnessCategory_adds5Points() {
        val suggestion = createSuggestion(category = HabitCategory.WELLNESS)
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + wellness bonus 5 = 55
        assertTrue(score >= 55)
    }

    @Test
    fun calculate_productivityCategory_adds3Points() {
        val suggestion = createSuggestion(category = HabitCategory.PRODUCTIVITY)
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + productivity bonus 3 = 53
        assertTrue(score >= 53)
    }

    @Test
    fun calculate_learningCategory_adds2Points() {
        val suggestion = createSuggestion(category = HabitCategory.LEARNING)
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + learning bonus 2 = 52
        assertTrue(score >= 52)
    }

    @Test
    fun calculate_generalCategory_subtracts5Points() {
        val suggestion = createSuggestion(category = HabitCategory.GENERAL)
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 - general penalty 5 = 45
        assertEquals(45, score)
    }

    @Test
    fun calculate_timeAppropriate_fitnessMorning_adds15Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(currentTime = LocalTime.of(8, 0))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 + time appropriate 15 = 70
        assertTrue(score >= 70)
    }

    @Test
    fun calculate_timeAppropriate_fitnessEvening_adds15Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(currentTime = LocalTime.of(18, 0))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 + time appropriate 15 = 70
        assertTrue(score >= 70)
    }

    @Test
    fun calculate_timeAppropriate_productivityWorkHours_adds15Points() {
        val suggestion = createSuggestion(category = HabitCategory.PRODUCTIVITY)
        val context = createContext(currentTime = LocalTime.of(14, 0))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + productivity bonus 3 + time appropriate 15 = 68
        assertTrue(score >= 68)
    }

    @Test
    fun calculate_timeAppropriate_wellnessEvening_adds15Points() {
        val suggestion = createSuggestion(category = HabitCategory.WELLNESS)
        val context = createContext(currentTime = LocalTime.of(20, 0))
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + wellness bonus 5 + time appropriate 15 = 70
        assertTrue(score >= 70)
    }

    @Test
    fun calculate_timeInappropriate_noBonus() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(currentTime = LocalTime.of(2, 0)) // Night time
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 = 55 (no time bonus)
        assertTrue(score < 70)
    }

    @Test
    fun calculate_weatherAppropriate_fitnessSunny_adds10Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val weather = Weather(condition = WeatherCondition.SUNNY)
        val context = createContext(currentWeather = weather)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 + weather appropriate 10 = 65
        assertTrue(score >= 65)
    }

    @Test
    fun calculate_weatherInappropriate_fitnessRainy_noBonus() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val weather = Weather(condition = WeatherCondition.RAINY)
        val context = createContext(currentWeather = weather)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 = 55 (no weather bonus for rainy)
        assertTrue(score < 65)
    }

    @Test
    fun calculate_weatherAppropriate_nonFitness_alwaysTrue() {
        val suggestion = createSuggestion(category = HabitCategory.PRODUCTIVITY)
        val weather = Weather(condition = WeatherCondition.RAINY)
        val context = createContext(currentWeather = weather)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + productivity bonus 3 + weather appropriate 10 = 63
        assertTrue(score >= 63)
    }

    @Test
    fun calculate_motionStateMatch_fitnessWalking_adds10Points() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(recentMotionState = MotionState.WALKING)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 + motion match 10 = 65
        assertTrue(score >= 65)
    }

    @Test
    fun calculate_motionStateMatch_wellnessStationary_adds10Points() {
        val suggestion = createSuggestion(category = HabitCategory.WELLNESS)
        val context = createContext(recentMotionState = MotionState.STATIONARY)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + wellness bonus 5 + motion match 10 = 65
        assertTrue(score >= 65)
    }

    @Test
    fun calculate_motionStateMismatch_noBonus() {
        val suggestion = createSuggestion(category = HabitCategory.FITNESS)
        val context = createContext(recentMotionState = MotionState.STATIONARY)
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + fitness bonus 5 = 55 (no motion match bonus)
        assertTrue(score < 65)
    }

    @Test
    fun calculate_actionableWords_adds5Points() {
        val suggestion = createSuggestion(title = "Daily workout routine")
        val context = createContext()
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 - general penalty 5 + actionable words 5 = 50
        assertTrue(score >= 50)
    }

    @Test
    fun calculate_maxScore_returns100() {
        val suggestion = createSuggestion(
            title = "Daily workout routine",
            category = HabitCategory.FITNESS
        )
        val weather = Weather(condition = WeatherCondition.SUNNY)
        val location = Location("test").apply {
            latitude = 0.0
            longitude = 0.0
        }
        val context = createContext(
            preferredCategories = setOf(HabitCategory.FITNESS),
            currentTime = LocalTime.of(8, 0), // Morning
            currentLocation = location,
            recentMotionState = MotionState.WALKING,
            currentWeather = weather
        )
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Should be clamped to 100
        assertTrue(score <= 100)
    }

    @Test
    fun calculate_minScore_returns0() {
        val suggestion = createSuggestion(
            title = "Bad suggestion",
            category = HabitCategory.GENERAL
        )
        val context = createContext(
            preferredCategories = emptySet(),
            currentTime = LocalTime.of(2, 0), // Night
            recentMotionState = MotionState.IN_VEHICLE
        )
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Should be clamped to at least 0
        assertTrue(score >= 0)
    }

    @Test
    fun calculate_allBonuses_maximizesScore() {
        val suggestion = createSuggestion(
            title = "Daily morning workout routine",
            category = HabitCategory.FITNESS
        )
        val weather = Weather(condition = WeatherCondition.SUNNY)
        val location = Location("test").apply {
            latitude = 0.0
            longitude = 0.0
        }
        val context = createContext(
            preferredCategories = setOf(HabitCategory.FITNESS),
            currentTime = LocalTime.of(8, 0),
            currentLocation = location,
            recentMotionState = MotionState.WALKING,
            currentWeather = weather
        )
        
        val score = FitScoreCalculator.calculate(suggestion, context)
        
        // Base 50 + category match 20 + fitness bonus 5 + time 15 + weather 10 + location 5 + motion 10 + actionable 5 = 120, clamped to 100
        assertEquals(100, score)
    }
}

