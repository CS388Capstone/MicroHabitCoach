package com.microhabitcoach.data.model

import android.location.Location
import java.time.LocalTime

/**
 * Represents the current user context for FitScore calculation.
 * Includes location, motion state, preferences, and time.
 */
data class UserContext(
    val preferredCategories: Set<HabitCategory>,
    val currentTime: LocalTime,
    val currentLocation: Location? = null,
    val recentMotionState: MotionState = MotionState.UNKNOWN,
    val currentWeather: Weather? = null // Optional for MVP
)

/**
 * Represents the user's recent motion/activity state.
 */
enum class MotionState {
    WALKING,
    RUNNING,
    STATIONARY,
    IN_VEHICLE,
    UNKNOWN
}

/**
 * Represents current weather conditions (optional for MVP).
 */
data class Weather(
    val condition: WeatherCondition,
    val temperature: Double? = null
)

enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    WINDY,
    UNKNOWN
}

