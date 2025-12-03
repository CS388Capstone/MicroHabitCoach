package com.microhabitcoach.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for OpenWeather current weather endpoint.
 * Docs: https://openweathermap.org/current
 */
interface WeatherApi {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): WeatherApiResponse
}

