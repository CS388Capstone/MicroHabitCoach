package com.microhabitcoach.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.microhabitcoach.data.model.HabitCategory

/**
 * Entity for articles saved by the user.
 * Separate from ApiSuggestion which is temporary cache.
 */
@Entity(
    tableName = "saved_articles",
    indices = [Index(value = ["savedAt"]), Index(value = ["source"])]
)
data class SavedArticle(
    @PrimaryKey
    val id: String, // Same as ApiSuggestion.id or generated
    val title: String,
    val description: String? = null,
    val content: String? = null,
    val source: String, // "hacker_news" or "news_api"
    val sourceUrl: String? = null,
    val imageUrl: String? = null, // Article image from News API
    val author: String? = null, // Author from Hacker News
    val publishedAt: String? = null, // ISO date string from News API
    val sourceName: String? = null, // Source name (e.g., "BBC News")
    val savedAt: Long = System.currentTimeMillis(), // When user saved it
    val category: HabitCategory? = null, // Preserve category if available
    val originalFitScore: Int? = null, // Preserve original score
    val score: Int? = null, // Upvotes from Hacker News
    val commentCount: Int? = null // Comment count from Hacker News
)

