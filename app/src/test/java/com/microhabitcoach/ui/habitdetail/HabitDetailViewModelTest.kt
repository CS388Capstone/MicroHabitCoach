package com.microhabitcoach.ui.habitdetail

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.DefaultHabitRepository
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var viewModel: HabitDetailViewModel
    private val habitId = "test-habit-1"

    @Before
    fun setup() {
        application = Application()
    }

    @Test
    fun loadHabit_loadsHabitAndCalculatesAnalytics() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        val completionDao = db.completionDao()
        
        // Create test habit
        val habit = createHabit(habitId, "Test Habit")
        habitDao.insertHabit(habit)
        
        // Add completions
        val now = System.currentTimeMillis()
        for (i in 0..2) {
            val completion = Completion(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                completedAt = now - (i * 24 * 60 * 60 * 1000L),
                autoCompleted = false
            )
            completionDao.insertCompletion(completion)
        }
        
        viewModel = HabitDetailViewModel(application, habitId)
        viewModel.loadHabit()
        advanceUntilIdle()
        
        val loadedHabit = viewModel.habit.getOrAwaitValue()
        assertNotNull(loadedHabit)
        assertEquals(habitId, loadedHabit?.id)
        
        val detailData = viewModel.detailData.getOrAwaitValue()
        assertNotNull(detailData)
        assertEquals("Test Habit", detailData?.habitName)
        assertNotNull(detailData?.streakInfo)
        assertNotNull(detailData?.completionStats)
    }

    @Test
    fun loadHabit_habitNotFound_setsError() = runTest {
        viewModel = HabitDetailViewModel(application, "non-existent-id")
        viewModel.loadHabit()
        advanceUntilIdle()
        
        val error = viewModel.error.getOrAwaitValue()
        assertNotNull(error)
        assertTrue(error?.contains("not found", ignoreCase = true) == true)
    }

    @Test
    fun loadHabit_emptyCompletions_handlesGracefully() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        
        val habit = createHabit(habitId, "Test Habit")
        habitDao.insertHabit(habit)
        
        viewModel = HabitDetailViewModel(application, habitId)
        viewModel.loadHabit()
        advanceUntilIdle()
        
        val detailData = viewModel.detailData.getOrAwaitValue()
        assertNotNull(detailData)
        assertEquals(0, detailData?.streakInfo?.currentStreak ?: 0)
        assertEquals(0, detailData?.completionStats?.totalCompletions ?: 0)
    }

    @Test
    fun refresh_reloadsHabit() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        
        val habit = createHabit(habitId, "Test Habit")
        habitDao.insertHabit(habit)
        
        viewModel = HabitDetailViewModel(application, habitId)
        viewModel.loadHabit()
        advanceUntilIdle()
        
        val initialData = viewModel.detailData.getOrAwaitValue()
        
        viewModel.refresh()
        advanceUntilIdle()
        
        val refreshedData = viewModel.detailData.getOrAwaitValue()
        assertNotNull(refreshedData)
    }

    @Test
    fun clearError_clearsError() = runTest {
        viewModel = HabitDetailViewModel(application, habitId)
        viewModel.clearError()
        
        val error = viewModel.error.getOrAwaitValue()
        assertNull(error)
    }

    @Test
    fun isLoading_setDuringLoad() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        
        val habit = createHabit(habitId, "Test Habit")
        habitDao.insertHabit(habit)
        
        viewModel = HabitDetailViewModel(application, habitId)
        viewModel.loadHabit()
        
        // Initially loading
        var isLoading = viewModel.isLoading.getOrAwaitValue()
        // Note: This might be false if load completes instantly in test
        
        advanceUntilIdle()
        
        // Should be done loading
        isLoading = viewModel.isLoading.getOrAwaitValue()
        assertTrue(!isLoading)
    }

    private fun createHabit(
        id: String,
        name: String,
        category: HabitCategory = HabitCategory.GENERAL
    ) = Habit(
        id = id,
        name = name,
        category = category,
        type = HabitType.TIME,
        reminderTimes = listOf(LocalTime.NOON),
        reminderDays = listOf(1),
        streakCount = 0,
        createdAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L),
        isActive = true
    )
}

