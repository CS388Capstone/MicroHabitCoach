package com.microhabitcoach.data.database.entity

import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ApiSuggestionTest {

    @Test
    fun apiSuggestion_creation_withDefaults_setsCorrectValues() {
        val suggestion = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = "Test Title",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 75
        )

        assertEquals("Test Title", suggestion.title)
        assertEquals("hacker_news", suggestion.source)
        assertEquals(HabitCategory.FITNESS, suggestion.category)
        assertEquals(75, suggestion.fitScore)
        assertTrue(suggestion.cachedAt > 0)
        assertNull(suggestion.expiresAt)
    }

    @Test
    fun apiSuggestion_withAllFields_setsCorrectly() {
        val suggestion = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = "Test Title",
            content = "Test content",
            source = "news_api",
            sourceUrl = "https://example.com",
            category = HabitCategory.WELLNESS,
            fitScore = 85,
            cachedAt = 1000L,
            expiresAt = 2000L,
            imageUrl = "https://example.com/image.jpg",
            author = "John Doe",
            publishedAt = "2024-01-01T00:00:00Z",
            sourceName = "Test News",
            score = 100,
            commentCount = 50
        )

        assertEquals("Test content", suggestion.content)
        assertEquals("https://example.com", suggestion.sourceUrl)
        assertEquals(1000L, suggestion.cachedAt)
        assertEquals(2000L, suggestion.expiresAt)
        assertEquals("https://example.com/image.jpg", suggestion.imageUrl)
        assertEquals("John Doe", suggestion.author)
        assertEquals("Test News", suggestion.sourceName)
        assertEquals(100, suggestion.score)
        assertEquals(50, suggestion.commentCount)
    }

    @Test
    fun apiSuggestion_withNullFields_handlesCorrectly() {
        val suggestion = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = null,
            source = "hacker_news",
            category = null,
            fitScore = 50
        )

        assertNull(suggestion.title)
        assertNull(suggestion.category)
        assertNull(suggestion.content)
        assertNull(suggestion.expiresAt)
    }

    @Test
    fun apiSuggestion_copy_updatesFields() {
        val original = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = "Original",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 50
        )

        val updated = original.copy(
            title = "Updated",
            fitScore = 90
        )

        assertEquals("Updated", updated.title)
        assertEquals(90, updated.fitScore)
        assertEquals(original.id, updated.id)
        assertEquals(original.source, updated.source)
    }

    @Test
    fun apiSuggestion_equals_sameIdAndFields_returnsTrue() {
        val id = UUID.randomUUID().toString()
        val cachedAt = System.currentTimeMillis()
        val suggestion1 = ApiSuggestion(
            id = id,
            title = "Test Title",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 50,
            cachedAt = cachedAt
        )
        val suggestion2 = ApiSuggestion(
            id = id,
            title = "Test Title",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 50,
            cachedAt = cachedAt
        )

        assertEquals(suggestion1, suggestion2)
    }
    
    @Test
    fun apiSuggestion_equals_sameIdDifferentFields_returnsFalse() {
        val id = UUID.randomUUID().toString()
        val suggestion1 = ApiSuggestion(
            id = id,
            title = "Title 1",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 50
        )
        val suggestion2 = ApiSuggestion(
            id = id,
            title = "Title 2",
            source = "news_api",
            category = HabitCategory.WELLNESS,
            fitScore = 100
        )

        // Kotlin data classes compare ALL fields, not just ID
        assertFalse(suggestion1 == suggestion2)
    }

    @Test
    fun apiSuggestion_fitScore_range_handlesCorrectly() {
        val minScore = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = "Min",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 0
        )
        val maxScore = ApiSuggestion(
            id = UUID.randomUUID().toString(),
            title = "Max",
            source = "hacker_news",
            category = HabitCategory.FITNESS,
            fitScore = 100
        )

        assertEquals(0, minScore.fitScore)
        assertEquals(100, maxScore.fitScore)
    }
}

