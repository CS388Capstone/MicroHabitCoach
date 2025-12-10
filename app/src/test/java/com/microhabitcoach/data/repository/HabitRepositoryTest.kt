package com.microhabitcoach.data.repository

import com.microhabitcoach.data.database.dao.CompletionDao
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class HabitRepositoryTest {

    private lateinit var habitDao: HabitDao
    private lateinit var completionDao: CompletionDao
    private lateinit var repository: DefaultHabitRepository

    @Before
    fun setup() {
        habitDao = mockk()
        completionDao = mockk()
        repository = DefaultHabitRepository(habitDao, completionDao)
    }

    @Test
    fun observeHabits_delegatesToDao() = runTest {
        val habits = listOf(createTestHabit())
        every { habitDao.getAllHabits() } returns flowOf(habits)

        val result = repository.observeHabits()
        val collected = mutableListOf<List<Habit>>()
        result.collect { collected.add(it) }

        assertEquals(habits, collected.first())
    }

    @Test
    fun getHabitById_existingHabit_returnsHabit() = runTest {
        val habit = createTestHabit()
        coEvery { habitDao.getHabitById(habit.id) } returns habit

        val result = repository.getHabitById(habit.id)

        assertEquals(habit, result)
    }

    @Test
    fun getHabitById_nonExistent_returnsNull() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { habitDao.getHabitById(id) } returns null

        val result = repository.getHabitById(id)

        assertNull(result)
    }

    @Test
    fun saveHabit_delegatesToDao() = runTest {
        val habit = createTestHabit()
        coEvery { habitDao.insertHabit(habit) } returns Unit

        repository.saveHabit(habit)

        coVerify { habitDao.insertHabit(habit) }
    }

    @Test
    fun completeHabit_notCompletedToday_createsCompletionAndUpdatesStreak() = runTest {
        val habit = createTestHabit(streakCount = 5)
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { completionDao.getCompletionForDay(habit.id, todayStart, todayEnd) } returns null
        coEvery { completionDao.insertCompletion(any()) } returns Unit
        coEvery { habitDao.updateHabit(any()) } returns Unit

        repository.completeHabit(habit.id)

        coVerify(exactly = 1) { completionDao.insertCompletion(any()) }
        coVerify(exactly = 1) { habitDao.updateHabit(any()) }
    }

    @Test
    fun completeHabit_alreadyCompletedToday_doesNotCreateDuplicate() = runTest {
        val habit = createTestHabit()
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000
        val existingCompletion = Completion(
            id = UUID.randomUUID().toString(),
            habitId = habit.id,
            completedAt = now
        )

        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { completionDao.getCompletionForDay(habit.id, todayStart, todayEnd) } returns existingCompletion

        repository.completeHabit(habit.id)

        coVerify(exactly = 0) { completionDao.insertCompletion(any()) }
        coVerify(exactly = 0) { habitDao.updateHabit(any()) }
    }

    @Test
    fun completeHabit_habitNotFound_doesNothing() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { habitDao.getHabitById(id) } returns null

        repository.completeHabit(id)

        coVerify(exactly = 0) { completionDao.insertCompletion(any()) }
        coVerify(exactly = 0) { habitDao.updateHabit(any()) }
    }

    @Test
    fun autoCompleteMotionHabit_notCompletedToday_createsAutoCompletion() = runTest {
        val habit = createTestHabit(type = HabitType.MOTION, streakCount = 3)
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { completionDao.getCompletionForDay(habit.id, todayStart, todayEnd) } returns null
        coEvery { completionDao.insertCompletion(any()) } returns Unit
        coEvery { habitDao.updateHabit(any()) } returns Unit

        repository.autoCompleteMotionHabit(habit.id)

        coVerify(exactly = 1) { completionDao.insertCompletion(any()) }
        coVerify(exactly = 1) { habitDao.updateHabit(any()) }
    }

    @Test
    fun autoCompleteLocationHabit_notCompletedToday_createsAutoCompletion() = runTest {
        val habit = createTestHabit(type = HabitType.LOCATION, streakCount = 7)
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { completionDao.getCompletionForDay(habit.id, todayStart, todayEnd) } returns null
        coEvery { completionDao.insertCompletion(any()) } returns Unit
        coEvery { habitDao.updateHabit(any()) } returns Unit

        repository.autoCompleteLocationHabit(habit.id)

        coVerify(exactly = 1) { completionDao.insertCompletion(any()) }
        coVerify(exactly = 1) { habitDao.updateHabit(any()) }
    }

    @Test
    fun deleteHabit_delegatesToDao() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { habitDao.deleteHabitById(id) } returns Unit

        repository.deleteHabit(id)

        coVerify { habitDao.deleteHabitById(id) }
    }

    @Test
    fun isHabitCompletedToday_completed_returnsTrue() = runTest {
        val habitId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000
        val completion = Completion(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            completedAt = now
        )

        coEvery { completionDao.getCompletionForDay(habitId, todayStart, todayEnd) } returns completion

        val result = repository.isHabitCompletedToday(habitId)

        assertTrue(result)
    }

    @Test
    fun isHabitCompletedToday_notCompleted_returnsFalse() = runTest {
        val habitId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val todayStart = getTodayStartTimestamp(now)
        val todayEnd = todayStart + 24 * 60 * 60 * 1000

        coEvery { completionDao.getCompletionForDay(habitId, todayStart, todayEnd) } returns null

        val result = repository.isHabitCompletedToday(habitId)

        assertFalse(result)
    }

    @Test
    fun clearAllCompletions_delegatesToDao() = runTest {
        coEvery { completionDao.deleteAllCompletions() } returns Unit

        repository.clearAllCompletions()

        coVerify { completionDao.deleteAllCompletions() }
    }

    @Test
    fun resetAllStreaks_resetsAllHabits() = runTest {
        val habits = listOf(
            createTestHabit(streakCount = 5),
            createTestHabit(streakCount = 10),
            createTestHabit(streakCount = 3)
        )

        coEvery { habitDao.getAllHabitsSync() } returns habits
        coEvery { habitDao.updateStreakCount(any(), 0, any()) } returns Unit

        repository.resetAllStreaks()

        coVerify(exactly = habits.size) { habitDao.updateStreakCount(any(), 0, any()) }
    }

    // Helper functions
    private fun createTestHabit(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit",
        category: HabitCategory = HabitCategory.FITNESS,
        type: HabitType = HabitType.TIME,
        streakCount: Int = 0
    ): Habit {
        return Habit(
            id = id,
            name = name,
            category = category,
            type = type,
            streakCount = streakCount
        )
    }

    private fun getTodayStartTimestamp(now: Long = System.currentTimeMillis()): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

