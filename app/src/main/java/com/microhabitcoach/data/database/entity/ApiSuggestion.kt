package com.microhabitcoach.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.microhabitcoach.data.model.HabitCategory

@Entity(
    tableName = "api_suggestions",
    indices = [Index(value = ["cachedAt"]), Index(value = ["fitScore"])]
)
data class ApiSuggestion(
    @PrimaryKey
    val id: String,
    val title: String?,
    val content: String? = null,
    val source: String?,
    val sourceUrl: String? = null,
    val category: HabitCategory?,
    val fitScore: Int, // 0-100
    val cachedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // Optional expiration time
    // Extra fields from APIs
    val imageUrl: String? = null, // Article image from News API
    val author: String? = null, // Author from Hacker News
    val publishedAt: String? = null, // ISO date string from News API
    val sourceName: String? = null, // Source name (e.g., "BBC News")
    val score: Int? = null, // Upvotes from Hacker News
    val commentCount: Int? = null // Comment count from Hacker News
)
