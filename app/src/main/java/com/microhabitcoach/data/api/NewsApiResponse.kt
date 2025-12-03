package com.microhabitcoach.data.api

import com.google.gson.annotations.SerializedName

/**
 * Response model for News API.
 */
data class NewsApiResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticle>
)

/**
 * Individual article from News API.
 */
data class NewsArticle(
    val title: String?,
    val description: String?,
    val content: String?,
    val url: String?,
    @SerializedName("urlToImage")
    val urlToImage: String?,
    val publishedAt: String?,
    val source: NewsSource?
)

/**
 * Source information for News API article.
 */
data class NewsSource(
    val id: String?,
    val name: String?
)

