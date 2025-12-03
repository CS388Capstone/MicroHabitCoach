package com.microhabitcoach.ui.explore

/**
 * Represents the weather banner shown on the Explore screen.
 */
data class WeatherUiState(
    val isAvailable: Boolean,
    val conditionText: String,
    val temperatureText: String?,
    val impactMessage: String,
    val impactType: WeatherImpactType
)

enum class WeatherImpactType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

