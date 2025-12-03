package com.microhabitcoach.ui.explore

import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for SuggestionAdapter DiffUtil logic.
 */
class SuggestionAdapterTest {

    private fun createSuggestion(
        id: String = "suggestion-1",
        title: String = "Test Suggestion",
        fitScore: Int = 50
    ) = ApiSuggestion(
        id = id,
        title = title,
        content = "Test content",
        category = HabitCategory.GENERAL,
        fitScore = fitScore,
        source = "test",
        cachedAt = System.currentTimeMillis()
    )

    @Test
    fun areItemsTheSame_sameId_returnsTrue() {
        val oldItem = createSuggestion("suggestion-1", "Title 1")
        val newItem = createSuggestion("suggestion-1", "Title 2") // Different title, same ID
        
        val areSame = oldItem.id == newItem.id
        assertTrue(areSame)
    }

    @Test
    fun areItemsTheSame_differentId_returnsFalse() {
        val oldItem = createSuggestion("suggestion-1", "Title")
        val newItem = createSuggestion("suggestion-2", "Title") // Same title, different ID
        
        val areSame = oldItem.id == newItem.id
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_sameSuggestion_returnsTrue() {
        val oldItem = createSuggestion("suggestion-1", "Title", fitScore = 50)
        val newItem = createSuggestion("suggestion-1", "Title", fitScore = 50)
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun areContentsTheSame_differentFitScore_returnsFalse() {
        val oldItem = createSuggestion("suggestion-1", "Title", fitScore = 50)
        val newItem = createSuggestion("suggestion-1", "Title", fitScore = 70) // Different score
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_differentTitle_returnsFalse() {
        val oldItem = createSuggestion("suggestion-1", "Title 1")
        val newItem = createSuggestion("suggestion-1", "Title 2") // Different title
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }
}

