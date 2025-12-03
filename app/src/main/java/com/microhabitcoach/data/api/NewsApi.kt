package com.microhabitcoach.data.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for News API.
 * Documentation: https://newsapi.org/docs/endpoints/everything
 */
interface NewsApi {
    
    /**
     * Fetches articles matching the query.
     * 
     * @param q Search query (keywords)
     * @param apiKey API key for authentication
     * @param pageSize Number of results (max 100)
     * @param sortBy Sort order (relevancy, popularity, publishedAt)
     * @param language Language code (en)
     * @return NewsApiResponse containing articles
     */
    @GET("everything")
    suspend fun getEverything(
        @Query("q") q: String,
        @Query("apiKey") apiKey: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "relevancy",
        @Query("language") language: String = "en"
    ): NewsApiResponse
}

