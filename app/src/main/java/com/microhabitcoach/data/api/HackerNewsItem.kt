package com.microhabitcoach.data.api

import com.google.gson.annotations.SerializedName

/**
 * Data model for Hacker News API item.
 * Based on Hacker News API structure: https://github.com/HackerNews/API
 */
data class HackerNewsItem(
    val id: Int,
    val title: String?,
    val text: String?,
    val url: String?,
    val by: String?,
    val time: Long,
    val type: String,
    val score: Int? = null,
    val descendants: Int? = null, // Number of comments
    val kids: List<Int>? = null // Child comment IDs
) {
    /**
     * Returns the content (title or text) for classification.
     */
    fun getContentForClassification(): String {
        return (title ?: "") + " " + (text ?: "")
    }
    
    /**
     * Checks if this item is a story (not a comment or job).
     */
    fun isStory(): Boolean {
        return type == "story" && title != null && !title.isBlank()
    }
}

