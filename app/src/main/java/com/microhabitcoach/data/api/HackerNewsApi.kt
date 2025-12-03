package com.microhabitcoach.data.api

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit interface for Hacker News API.
 * Base URL: https://hacker-news.firebaseio.com/v0/
 * 
 * Documentation: https://github.com/HackerNews/API
 */
interface HackerNewsApi {
    
    /**
     * Fetches the top 500 story IDs.
     * Returns a list of story IDs.
     */
    @GET("topstories.json")
    suspend fun getTopStories(): List<Int>
    
    /**
     * Fetches a specific item by ID.
     * Can be a story, comment, job, poll, etc.
     */
    @GET("item/{id}.json")
    suspend fun getItem(@Path("id") id: Int): HackerNewsItem
}

