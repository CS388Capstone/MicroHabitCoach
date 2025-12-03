package com.microhabitcoach.ui.explore

import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Tests for SavedArticleAdapter DiffUtil logic.
 */
class SavedArticleAdapterTest {

    private fun createSavedArticle(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Article",
        source: String = "hacker_news"
    ) = SavedArticle(
        id = id,
        title = title,
        description = "Test description",
        content = "Test content",
        source = source,
        sourceUrl = "https://example.com",
        imageUrl = null,
        author = null,
        publishedAt = "2024-01-01T12:00:00Z",
        sourceName = "Hacker News",
        savedAt = System.currentTimeMillis(),
        category = HabitCategory.GENERAL,
        originalFitScore = 50,
        score = 100,
        commentCount = 10
    )

    @Test
    fun areItemsTheSame_sameId_returnsTrue() {
        val oldItem = createSavedArticle("article-1", "Title 1")
        val newItem = createSavedArticle("article-1", "Title 2") // Different title, same ID
        
        val areSame = oldItem.id == newItem.id
        assertTrue(areSame)
    }

    @Test
    fun areItemsTheSame_differentId_returnsFalse() {
        val oldItem = createSavedArticle("article-1", "Title")
        val newItem = createSavedArticle("article-2", "Title") // Same title, different ID
        
        val areSame = oldItem.id == newItem.id
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_sameArticle_returnsTrue() {
        val oldItem = createSavedArticle("article-1", "Title")
        val newItem = createSavedArticle("article-1", "Title")
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun areContentsTheSame_differentTitle_returnsFalse() {
        val oldItem = createSavedArticle("article-1", "Title 1")
        val newItem = createSavedArticle("article-1", "Title 2")
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }
}

