package com.microhabitcoach.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Module for creating and providing API service instances.
 */
object ApiModule {
    
    private const val HACKER_NEWS_BASE_URL = "https://hacker-news.firebaseio.com/v0/"
    private const val NEWS_API_BASE_URL = "https://newsapi.org/v2/"
    private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    // News API key loaded from BuildConfig (which reads from local.properties)
    val NEWS_API_KEY: String = try {
        val key = com.microhabitcoach.BuildConfig.NEWS_API_KEY
        android.util.Log.d("ApiModule", "Loaded News API key from BuildConfig: ${key.take(10)}... (length: ${key.length})")
        key
    } catch (e: Exception) {
        android.util.Log.e("ApiModule", "Failed to load NEWS_API_KEY from BuildConfig: ${e.message}", e)
        // Fallback to hardcoded key if BuildConfig fails
        "pub_22f1f73468c64821ba6cbe73d373aaf4"
    }
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Changed to BODY to see full request/response
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val hackerNewsRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(HACKER_NEWS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    private val newsApiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NEWS_API_BASE_URL)
        .client(okHttpClient) // Use same client (key sent as query parameter)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    private val weatherRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(OPEN_WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    /**
     * Provides Hacker News API service instance.
     */
    val hackerNewsApi: HackerNewsApi = hackerNewsRetrofit.create(HackerNewsApi::class.java)
    
    /**
     * Provides News API service instance.
     */
    val newsApi: NewsApi = newsApiRetrofit.create(NewsApi::class.java)
    
    /**
     * Provides OpenWeather API service instance.
     */
    val weatherApi: WeatherApi = weatherRetrofit.create(WeatherApi::class.java)
}

