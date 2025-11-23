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
    val id: String, // Can be Hacker News item ID or generated UUID
    val title: String,
    val content: String? = null,
    val source: String, // "hacker_news" or "news_api"
    val sourceUrl: String? = null,
    val category: HabitCategory,
    val fitScore: Int, // 0-100
    val cachedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null // Optional expiration time
)

