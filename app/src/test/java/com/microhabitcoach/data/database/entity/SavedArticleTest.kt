package com.microhabitcoach.data.database.entity

import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SavedArticleTest {

    @Test
    fun savedArticle_creation_withDefaults_setsCorrectValues() {
        val article = SavedArticle(
            id = UUID.randomUUID().toString(),
            title = "Test Article",
            source = "hacker_news"
        )

        assertEquals("Test Article", article.title)
        assertEquals("hacker_news", article.source)
        assertTrue(article.savedAt > 0)
        assertNull(article.description)
        assertNull(article.category)
    }

    @Test
    fun savedArticle_withAllFields_setsCorrectly() {
        val article = SavedArticle(
            id = UUID.randomUUID().toString(),
            title = "Test Article",
            description = "Test Description",
            content = "Test Content",
            source = "news_api",
            sourceUrl = "https://example.com",
            imageUrl = "https://example.com/image.jpg",
            author = "John Doe",
            publishedAt = "2024-01-01T00:00:00Z",
            sourceName = "Test News",
            savedAt = 1000L,
            category = HabitCategory.FITNESS,
            originalFitScore = 85,
            score = 100,
            commentCount = 50
        )

        assertEquals("Test Description", article.description)
        assertEquals("Test Content", article.content)
        assertEquals("https://example.com", article.sourceUrl)
        assertEquals("https://example.com/image.jpg", article.imageUrl)
        assertEquals("John Doe", article.author)
        assertEquals("Test News", article.sourceName)
        assertEquals(1000L, article.savedAt)
        assertEquals(HabitCategory.FITNESS, article.category)
        assertEquals(85, article.originalFitScore)
        assertEquals(100, article.score)
        assertEquals(50, article.commentCount)
    }

    @Test
    fun savedArticle_copy_updatesFields() {
        val original = SavedArticle(
            id = UUID.randomUUID().toString(),
            title = "Original",
            source = "hacker_news"
        )

        val updated = original.copy(
            title = "Updated",
            category = HabitCategory.WELLNESS
        )

        assertEquals("Updated", updated.title)
        assertEquals(HabitCategory.WELLNESS, updated.category)
        assertEquals(original.id, updated.id)
        assertEquals(original.source, updated.source)
    }

    @Test
    fun savedArticle_equals_sameIdAndFields_returnsTrue() {
        val id = UUID.randomUUID().toString()
        val savedAt = System.currentTimeMillis()
        val article1 = SavedArticle(
            id = id,
            title = "Test Article",
            source = "hacker_news",
            savedAt = savedAt
        )
        val article2 = SavedArticle(
            id = id,
            title = "Test Article",
            source = "hacker_news",
            savedAt = savedAt
        )

        assertEquals(article1, article2)
    }
    
    @Test
    fun savedArticle_equals_sameIdDifferentFields_returnsFalse() {
        val id = UUID.randomUUID().toString()
        val article1 = SavedArticle(
            id = id,
            title = "Title 1",
            source = "hacker_news"
        )
        val article2 = SavedArticle(
            id = id,
            title = "Title 2",
            source = "news_api"
        )

        // Kotlin data classes compare ALL fields, not just ID
        assertFalse(article1 == article2)
    }
}

