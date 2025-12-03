package com.microhabitcoach.data.repository

import com.microhabitcoach.BuildConfig
import com.microhabitcoach.data.api.ApiModule
import com.microhabitcoach.data.api.WeatherApiResponse
import com.microhabitcoach.data.model.Weather
import com.microhabitcoach.data.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.round

/**
 * Lightweight repository for fetching and caching weather data.
 * Weather is cached for 30 minutes per location to limit API calls.
 */
class WeatherRepository {

    private val weatherApi = ApiModule.weatherApi
    private val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

    private val cacheDurationMs = 30 * 60 * 1000L

    @Volatile
    private var cachedWeather: Weather? = null
    @Volatile
    private var lastFetchTime: Long = 0L
    @Volatile
    private var lastLocationKey: String? = null

    suspend fun getWeather(latitude: Double, longitude: Double): Weather? {
        if (apiKey.isBlank()) {
            android.util.Log.w("WeatherRepository", "OpenWeather API key missing. Skipping weather fetch.")
            return null
        }

        val locationKey = "${latitude.roundTo(2)},${longitude.roundTo(2)}"
        val now = System.currentTimeMillis()
        if (cachedWeather != null &&
            now - lastFetchTime < cacheDurationMs &&
            locationKey == lastLocationKey
        ) {
            android.util.Log.d("WeatherRepository", "Using cached weather for $locationKey")
            return cachedWeather
        }

        return try {
            val response = withContext(Dispatchers.IO) {
                weatherApi.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    units = "metric",
                    apiKey = apiKey
                )
            }
            val mappedWeather = mapResponseToWeather(response)
            if (mappedWeather != null) {
                cachedWeather = mappedWeather
                lastFetchTime = now
                lastLocationKey = locationKey
                android.util.Log.d("WeatherRepository", "Fetched weather: $mappedWeather")
            }
            mappedWeather
        } catch (e: Exception) {
            android.util.Log.e("WeatherRepository", "Failed to fetch weather: ${e.message}", e)
            null
        }
    }

    private fun mapResponseToWeather(response: WeatherApiResponse): Weather? {
        val conditionEntry = response.weather?.firstOrNull()
        val condition = mapCondition(conditionEntry?.id, conditionEntry?.group)
        val temperature = response.main?.temp
        return Weather(condition = condition, temperature = temperature)
    }

    private fun mapCondition(code: Int?, group: String?): WeatherCondition {
        if (code == null) return WeatherCondition.UNKNOWN

        return when (code) {
            in 200..531 -> WeatherCondition.RAINY // Thunderstorm + drizzle + rain
            in 600..622 -> WeatherCondition.SNOWY
            in 700..781 -> WeatherCondition.WINDY
            800 -> WeatherCondition.SUNNY
            in 801..804 -> WeatherCondition.CLOUDY
            else -> {
                when (group?.lowercase()) {
                    "clear" -> WeatherCondition.SUNNY
                    "clouds" -> WeatherCondition.CLOUDY
                    "rain", "drizzle", "thunderstorm" -> WeatherCondition.RAINY
                    "snow" -> WeatherCondition.SNOWY
                    else -> WeatherCondition.UNKNOWN
                }
            }
        }
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals.toDouble())
        return round(this * factor) / factor
    }
}

