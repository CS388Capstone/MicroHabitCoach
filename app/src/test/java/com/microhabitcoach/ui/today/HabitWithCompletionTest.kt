package com.microhabitcoach.ui.today

import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HabitWithCompletionTest {

    private fun createHabit(
        id: String = "habit-1",
        name: String = "Test Habit"
    ) = Habit(
        id = id,
        name = name,
        category = HabitCategory.GENERAL,
        type = HabitType.TIME,
        reminderTimes = listOf(LocalTime.NOON),
        reminderDays = listOf(1),
        streakCount = 0,
        isActive = true
    )

    @Test
    fun habitWithCompletion_containsHabit() {
        val habit = createHabit()
        val habitWithCompletion = HabitWithCompletion(habit, false)
        
        assertEquals(habit, habitWithCompletion.habit)
    }

    @Test
    fun habitWithCompletion_tracksCompletionStatus() {
        val habit = createHabit()
        val completed = HabitWithCompletion(habit, true)
        val notCompleted = HabitWithCompletion(habit, false)
        
        assertTrue(completed.isCompletedToday)
        assertFalse(notCompleted.isCompletedToday)
    }

    @Test
    fun habitWithCompletion_equality() {
        val habit1 = createHabit("habit-1", "Habit 1")
        val habit2 = createHabit("habit-1", "Habit 1")
        
        val item1 = HabitWithCompletion(habit1, true)
        val item2 = HabitWithCompletion(habit2, true)
        
        assertEquals(item1.habit.id, item2.habit.id)
        assertEquals(item1.isCompletedToday, item2.isCompletedToday)
    }

    @Test
    fun habitWithCompletion_differentCompletionStatus() {
        val habit = createHabit()
        val completed = HabitWithCompletion(habit, true)
        val notCompleted = HabitWithCompletion(habit, false)
        
        assertEquals(completed.habit.id, notCompleted.habit.id)
        assertTrue(completed.isCompletedToday != notCompleted.isCompletedToday)
    }
}

