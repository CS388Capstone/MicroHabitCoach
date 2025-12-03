package com.microhabitcoach.data.api

import com.google.gson.annotations.SerializedName

/**
 * Minimal subset of OpenWeather response needed for FitScore.
 */
data class WeatherApiResponse(
    @SerializedName("weather") val weather: List<WeatherConditionEntry>?,
    @SerializedName("main") val main: MainInfo?
)

data class WeatherConditionEntry(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val group: String?,
    @SerializedName("description") val description: String?
)

data class MainInfo(
    @SerializedName("temp") val temp: Double?
)

