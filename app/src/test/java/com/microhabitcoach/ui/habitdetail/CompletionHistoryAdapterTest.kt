package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Tests for CompletionHistoryAdapter DiffUtil logic.
 */
class CompletionHistoryAdapterTest {

    private fun createCompletion(
        id: String = UUID.randomUUID().toString(),
        autoCompleted: Boolean = false
    ) = Completion(
        id = id,
        habitId = "habit-1",
        completedAt = System.currentTimeMillis(),
        autoCompleted = autoCompleted
    )

    private fun createHistoryItem(
        completion: Completion,
        formattedDate: String = "Jan 1, 2024",
        formattedTime: String = "12:00 PM",
        isToday: Boolean = false,
        isThisWeek: Boolean = true
    ) = CompletionHistoryItem(
        completion = completion,
        formattedDate = formattedDate,
        formattedTime = formattedTime,
        isToday = isToday,
        isThisWeek = isThisWeek
    )

    @Test
    fun areItemsTheSame_sameCompletionId_returnsTrue() {
        val completion = createCompletion("completion-1")
        val oldItem = createHistoryItem(completion, formattedDate = "Jan 1")
        val newItem = createHistoryItem(completion, formattedDate = "Jan 2") // Different date, same completion
        
        val areSame = oldItem.completion.id == newItem.completion.id
        assertTrue(areSame)
    }

    @Test
    fun areItemsTheSame_differentCompletionId_returnsFalse() {
        val completion1 = createCompletion("completion-1")
        val completion2 = createCompletion("completion-2")
        val oldItem = createHistoryItem(completion1)
        val newItem = createHistoryItem(completion2)
        
        val areSame = oldItem.completion.id == newItem.completion.id
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_sameItem_returnsTrue() {
        val completion = createCompletion("completion-1")
        val oldItem = createHistoryItem(completion)
        val newItem = createHistoryItem(completion)
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun areContentsTheSame_differentFormattedDate_returnsFalse() {
        val completion = createCompletion("completion-1")
        val oldItem = createHistoryItem(completion, formattedDate = "Jan 1, 2024")
        val newItem = createHistoryItem(completion, formattedDate = "Jan 2, 2024")
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }

    @Test
    fun completionHistoryItem_formatsCorrectly() {
        val completion = createCompletion()
        val item = createHistoryItem(
            completion,
            formattedDate = "Jan 1, 2024",
            formattedTime = "12:00 PM",
            isToday = true,
            isThisWeek = true
        )
        
        assertEquals("Jan 1, 2024", item.formattedDate)
        assertEquals("12:00 PM", item.formattedTime)
        assertTrue(item.isToday)
        assertTrue(item.isThisWeek)
        assertEquals(completion, item.completion)
    }
}

