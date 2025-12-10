package com.microhabitcoach.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalTime
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class CompletionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var completionDao: CompletionDao
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        completionDao = database.completionDao()
        habitDao = database.habitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCompletion_insertsSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completion = createTestCompletion(habitId = habit.id)
        completionDao.insertCompletion(completion)

        val completions = completionDao.getCompletionsForHabitSync(habit.id)
        assertEquals(1, completions.size)
        assertEquals(completion.id, completions.first().id)
    }

    @Test
    fun insertCompletions_insertsMultipleSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completions = listOf(
            createTestCompletion(habitId = habit.id, completedAt = 1000L),
            createTestCompletion(habitId = habit.id, completedAt = 2000L),
            createTestCompletion(habitId = habit.id, completedAt = 3000L)
        )
        completionDao.insertCompletions(completions)

        val retrieved = completionDao.getCompletionsForHabitSync(habit.id)
        assertEquals(3, retrieved.size)
    }

    @Test
    fun getCompletionsForHabit_ordersByCompletedAtDesc() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completion1 = createTestCompletion(habitId = habit.id, completedAt = 1000L)
        val completion2 = createTestCompletion(habitId = habit.id, completedAt = 2000L)
        val completion3 = createTestCompletion(habitId = habit.id, completedAt = 3000L)
        
        completionDao.insertCompletions(listOf(completion1, completion2, completion3))

        val completions = completionDao.getCompletionsForHabitSync(habit.id)
        assertEquals(3, completions.size)
        assertEquals(3000L, completions[0].completedAt)
        assertEquals(2000L, completions[1].completedAt)
        assertEquals(1000L, completions[2].completedAt)
    }

    @Test
    fun getCompletionsInRange_returnsOnlyInRange() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val startTime = 1000L
        val endTime = 2000L
        
        val inRange1 = createTestCompletion(habitId = habit.id, completedAt = 1500L)
        val inRange2 = createTestCompletion(habitId = habit.id, completedAt = 1800L)
        val outOfRange = createTestCompletion(habitId = habit.id, completedAt = 2500L)
        
        completionDao.insertCompletions(listOf(inRange1, inRange2, outOfRange))

        val completions = completionDao.getCompletionsInRange(habit.id, startTime, endTime)
        assertEquals(2, completions.size)
    }

    @Test
    fun getAllCompletionsInRange_returnsAllHabitsCompletions() = runTest {
        val habit1 = createTestHabit(id = "habit1")
        val habit2 = createTestHabit(id = "habit2")
        habitDao.insertHabits(listOf(habit1, habit2))
        
        val startTime = 1000L
        val endTime = 2000L
        
        val completion1 = createTestCompletion(habitId = habit1.id, completedAt = 1500L)
        val completion2 = createTestCompletion(habitId = habit2.id, completedAt = 1800L)
        val outOfRange = createTestCompletion(habitId = habit1.id, completedAt = 2500L)
        
        completionDao.insertCompletions(listOf(completion1, completion2, outOfRange))

        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        assertEquals(2, completions.size)
    }

    @Test
    fun getCompletionForDay_returnsCompletionIfExists() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val dayStart = 1000L
        val dayEnd = 2000L
        
        val completion = createTestCompletion(habitId = habit.id, completedAt = 1500L)
        completionDao.insertCompletion(completion)

        val retrieved = completionDao.getCompletionForDay(habit.id, dayStart, dayEnd)
        assertEquals(completion.id, retrieved?.id)
    }

    @Test
    fun getCompletionForDay_noCompletion_returnsNull() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val dayStart = 1000L
        val dayEnd = 2000L

        val retrieved = completionDao.getCompletionForDay(habit.id, dayStart, dayEnd)
        assertNull(retrieved)
    }

    @Test
    fun getCompletionCount_returnsCorrectCount() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completions = listOf(
            createTestCompletion(habitId = habit.id),
            createTestCompletion(habitId = habit.id),
            createTestCompletion(habitId = habit.id)
        )
        completionDao.insertCompletions(completions)

        val count = completionDao.getCompletionCount(habit.id)
        assertEquals(3, count)
    }

    @Test
    fun getCompletionCountInRange_returnsCorrectCount() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val startTime = 1000L
        val endTime = 2000L
        
        val inRange1 = createTestCompletion(habitId = habit.id, completedAt = 1500L)
        val inRange2 = createTestCompletion(habitId = habit.id, completedAt = 1800L)
        val outOfRange = createTestCompletion(habitId = habit.id, completedAt = 2500L)
        
        completionDao.insertCompletions(listOf(inRange1, inRange2, outOfRange))

        val count = completionDao.getCompletionCountInRange(habit.id, startTime, endTime)
        assertEquals(2, count)
    }

    @Test
    fun deleteCompletion_deletesSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completion = createTestCompletion(habitId = habit.id)
        completionDao.insertCompletion(completion)
        completionDao.deleteCompletion(completion)

        val count = completionDao.getCompletionCount(habit.id)
        assertEquals(0, count)
    }

    @Test
    fun deleteCompletionById_deletesSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completion = createTestCompletion(habitId = habit.id)
        completionDao.insertCompletion(completion)
        completionDao.deleteCompletionById(completion.id)

        val count = completionDao.getCompletionCount(habit.id)
        assertEquals(0, count)
    }

    @Test
    fun deleteCompletionsForHabit_deletesAllForHabit() = runTest {
        val habit1 = createTestHabit(id = "habit1")
        val habit2 = createTestHabit(id = "habit2")
        habitDao.insertHabits(listOf(habit1, habit2))
        
        val completion1 = createTestCompletion(habitId = habit1.id)
        val completion2 = createTestCompletion(habitId = habit1.id)
        val completion3 = createTestCompletion(habitId = habit2.id)
        
        completionDao.insertCompletions(listOf(completion1, completion2, completion3))
        completionDao.deleteCompletionsForHabit(habit1.id)

        assertEquals(0, completionDao.getCompletionCount(habit1.id))
        assertEquals(1, completionDao.getCompletionCount(habit2.id))
    }

    @Test
    fun deleteOldCompletions_deletesOnlyOld() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val oldCompletion = createTestCompletion(habitId = habit.id, completedAt = 1000L)
        val newCompletion = createTestCompletion(habitId = habit.id, completedAt = System.currentTimeMillis())
        
        completionDao.insertCompletions(listOf(oldCompletion, newCompletion))
        completionDao.deleteOldCompletions(5000L) // Delete before 5000L

        val count = completionDao.getCompletionCount(habit.id)
        assertEquals(1, count)
    }

    @Test
    fun deleteAllCompletions_deletesEverything() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        
        val completions = listOf(
            createTestCompletion(habitId = habit.id),
            createTestCompletion(habitId = habit.id),
            createTestCompletion(habitId = habit.id)
        )
        completionDao.insertCompletions(completions)
        completionDao.deleteAllCompletions()

        val allCompletions = completionDao.getAllCompletions()
        assertEquals(0, allCompletions.size)
    }

    private fun createTestHabit(
        id: String = UUID.randomUUID().toString()
    ): Habit {
        return Habit(
            id = id,
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            reminderTimes = listOf(LocalTime.of(9, 0)),
            reminderDays = listOf(1, 2, 3, 4, 5, 6, 7),
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            streakCount = 0
        )
    }

    private fun createTestCompletion(
        id: String = UUID.randomUUID().toString(),
        habitId: String,
        completedAt: Long = System.currentTimeMillis()
    ): Completion {
        return Completion(
            id = id,
            habitId = habitId,
            completedAt = completedAt
        )
    }
}

