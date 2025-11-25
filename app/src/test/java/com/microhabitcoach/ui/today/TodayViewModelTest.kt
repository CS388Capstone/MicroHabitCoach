package com.microhabitcoach.ui.today

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.FakeHabitRepository
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val application = Application()

    @Test
    fun loadHabits_emitsFlowValues() = runTest {
        val repository = FakeHabitRepository()
        val viewModel = TodayViewModel(application, repository)
        repository.seedHabits(listOf(sampleHabit()))

        viewModel.loadHabits()
        advanceUntilIdle()

        val habits = viewModel.habits.getOrAwaitValue()
        assertEquals(1, habits.size)
        assertEquals("Habit-1", habits.first().habit.id)
    }

    @Test
    fun completeHabit_updatesRepository() = runTest {
        val repository = FakeHabitRepository()
        val viewModel = TodayViewModel(application, repository)
        val habit = sampleHabit(streak = 2)
        repository.seedHabits(listOf(habit))
        viewModel.loadHabits()
        advanceUntilIdle()

        viewModel.completeHabit(habit.id)
        advanceUntilIdle()

        val updated = repository.getHabitById(habit.id)
        assertEquals(3, updated?.streakCount)
    }

    @Test
    fun completeHabit_error_setsError() = runTest {
        val repository = FakeHabitRepository().apply {
            seedHabits(listOf(sampleHabit()))
            shouldFailComplete = true
        }
        val viewModel = TodayViewModel(application, repository)
        viewModel.loadHabits()
        advanceUntilIdle()

        viewModel.completeHabit("Habit-1")
        advanceUntilIdle()

        val error = viewModel.error.getOrAwaitValue()
        assertTrue(error?.contains("failed", ignoreCase = true) == true)
    }

    private fun sampleHabit(
        id: String = "Habit-1",
        streak: Int = 0
    ) = Habit(
        id = id,
        name = "Sample",
        category = HabitCategory.GENERAL,
        type = HabitType.TIME,
        reminderTimes = listOf(LocalTime.NOON),
        reminderDays = listOf(1, 2),
        streakCount = streak
    )
}

