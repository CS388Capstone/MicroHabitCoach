package com.microhabitcoach.ui.stats

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.LocalTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileStatsViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var viewModel: ProfileStatsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        application = Application()
    }

    @Test
    fun loadStats_emptyHabits_returnsEmptyStats() = runTest {
        // This test would require mocking the database
        // For now, we'll test the logic with a real database instance
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        val completionDao = db.completionDao()
        
        viewModel = ProfileStatsViewModel(application)
        
        viewModel.loadStats()
        advanceUntilIdle()
        
        val stats = viewModel.statsData.getOrAwaitValue()
        assertNotNull(stats)
        assertEquals(0, stats?.aggregateStats?.totalHabits ?: 0)
    }

    @Test
    fun loadStats_withHabits_calculatesAggregateStats() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        val completionDao = db.completionDao()
        
        // Create test habits
        val habit1 = createHabit("habit-1", "Habit 1", streakCount = 5)
        val habit2 = createHabit("habit-2", "Habit 2", streakCount = 3)
        
        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)
        
        viewModel = ProfileStatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        val stats = viewModel.statsData.getOrAwaitValue()
        assertNotNull(stats)
        assertEquals(2, stats?.aggregateStats?.totalHabits ?: 0)
        assertEquals(5, stats?.aggregateStats?.longestStreak ?: 0)
    }

    @Test
    fun loadStats_calculatesCategoryBreakdown() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        
        val fitnessHabit = createHabit("habit-1", "Workout", category = HabitCategory.FITNESS)
        val wellnessHabit = createHabit("habit-2", "Meditation", category = HabitCategory.WELLNESS)
        
        habitDao.insertHabit(fitnessHabit)
        habitDao.insertHabit(wellnessHabit)
        
        viewModel = ProfileStatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        val stats = viewModel.statsData.getOrAwaitValue()
        assertNotNull(stats)
        val categoryBreakdown = stats?.categoryBreakdown ?: emptyList()
        assertTrue(categoryBreakdown.any { it.category == HabitCategory.FITNESS })
        assertTrue(categoryBreakdown.any { it.category == HabitCategory.WELLNESS })
    }

    @Test
    fun loadStats_calculatesInsights() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val habitDao = db.habitDao()
        val completionDao = db.completionDao()
        
        val habit1 = createHabit("habit-1", "Consistent Habit", streakCount = 10)
        val habit2 = createHabit("habit-2", "Other Habit", streakCount = 2)
        
        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)
        
        // Add completions for habit1
        val now = System.currentTimeMillis()
        for (i in 0..9) {
            val completion = Completion(
                id = UUID.randomUUID().toString(),
                habitId = "habit-1",
                completedAt = now - (i * 24 * 60 * 60 * 1000L),
                autoCompleted = false
            )
            completionDao.insertCompletion(completion)
        }
        
        viewModel = ProfileStatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        val stats = viewModel.statsData.getOrAwaitValue()
        assertNotNull(stats)
        assertNotNull(stats?.insights?.mostConsistentHabit)
        assertEquals("Consistent Habit", stats?.insights?.mostConsistentHabit?.habitName)
    }

    @Test
    fun loadStats_handlesError() = runTest {
        // Error handling would be tested with mocked database that throws exceptions
        viewModel = ProfileStatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        // Should not crash
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertTrue(!isLoading)
    }

    @Test
    fun refresh_reloadsStats() = runTest {
        viewModel = ProfileStatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        val initialStats = viewModel.statsData.getOrAwaitValue()
        
        viewModel.refresh()
        advanceUntilIdle()
        
        val refreshedStats = viewModel.statsData.getOrAwaitValue()
        assertNotNull(refreshedStats)
    }

    @Test
    fun clearError_clearsError() = runTest {
        viewModel = ProfileStatsViewModel(application)
        viewModel.clearError()
        
        val error = viewModel.error.getOrAwaitValue()
        assertNull(error)
    }

    private fun createHabit(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit",
        category: HabitCategory = HabitCategory.GENERAL,
        streakCount: Int = 0
    ) = Habit(
        id = id,
        name = name,
        category = category,
        type = HabitType.TIME,
        reminderTimes = listOf(LocalTime.NOON),
        reminderDays = listOf(1),
        streakCount = streakCount,
        createdAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L),
        isActive = true
    )
}

