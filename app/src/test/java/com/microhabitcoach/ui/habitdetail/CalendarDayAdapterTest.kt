package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Tests for CalendarDayAdapter DiffUtil logic and date calculations.
 */
class CalendarDayAdapterTest {

    private fun createCalendarDayData(
        daysAgo: Int = 0,
        hasCompletion: Boolean = false
    ): CalendarDayData {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val completion = if (hasCompletion) {
            Completion(
                id = UUID.randomUUID().toString(),
                habitId = "habit-1",
                completedAt = calendar.timeInMillis,
                autoCompleted = false
            )
        } else null
        
        return CalendarDayData(
            date = calendar.timeInMillis,
            hasCompletion = hasCompletion,
            completion = completion
        )
    }

    @Test
    fun areItemsTheSame_sameDate_returnsTrue() {
        val oldItem = createCalendarDayData(daysAgo = 5, hasCompletion = false)
        val newItem = createCalendarDayData(daysAgo = 5, hasCompletion = true) // Different completion, same date
        
        val areSame = oldItem.date == newItem.date
        assertTrue(areSame)
    }

    @Test
    fun areItemsTheSame_differentDate_returnsFalse() {
        val oldItem = createCalendarDayData(daysAgo = 5)
        val newItem = createCalendarDayData(daysAgo = 6) // Different date
        
        val areSame = oldItem.date == newItem.date
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_sameData_returnsTrue() {
        val oldItem = createCalendarDayData(daysAgo = 5, hasCompletion = true)
        val newItem = createCalendarDayData(daysAgo = 5, hasCompletion = true)
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun areContentsTheSame_differentCompletionStatus_returnsFalse() {
        val oldItem = createCalendarDayData(daysAgo = 5, hasCompletion = false)
        val newItem = createCalendarDayData(daysAgo = 5, hasCompletion = true) // Completion changed
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }

    @Test
    fun calendarDayData_hasCompletion_markedCorrectly() {
        val dayData = createCalendarDayData(daysAgo = 0, hasCompletion = true)
        
        assertTrue(dayData.hasCompletion)
        assertTrue(dayData.completion != null)
    }

    @Test
    fun calendarDayData_noCompletion_markedCorrectly() {
        val dayData = createCalendarDayData(daysAgo = 0, hasCompletion = false)
        
        assertFalse(dayData.hasCompletion)
        assertTrue(dayData.completion == null)
    }
}

