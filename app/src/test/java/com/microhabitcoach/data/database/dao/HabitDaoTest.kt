package com.microhabitcoach.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.microhabitcoach.data.database.AppDatabase
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
class HabitDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        habitDao = database.habitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertHabit_insertsSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals(habit.id, retrieved?.id)
        assertEquals(habit.name, retrieved?.name)
    }

    @Test
    fun insertHabits_insertsMultipleSuccessfully() = runTest {
        val habits = listOf(
            createTestHabit(id = "1"),
            createTestHabit(id = "2"),
            createTestHabit(id = "3")
        )
        habitDao.insertHabits(habits)

        val allHabits = habitDao.getAllHabitsSync()
        assertEquals(3, allHabits.size)
    }

    @Test
    fun getAllHabits_returnsOnlyActiveHabits() = runTest {
        val activeHabit = createTestHabit(id = "active", isActive = true)
        val inactiveHabit = createTestHabit(id = "inactive", isActive = false)
        
        habitDao.insertHabits(listOf(activeHabit, inactiveHabit))

        val allHabits = habitDao.getAllHabitsSync()
        assertEquals(1, allHabits.size)
        assertEquals("active", allHabits.first().id)
    }

    @Test
    fun getAllHabits_ordersByCreatedAtDesc() = runTest {
        val habit1 = createTestHabit(id = "1", createdAt = 1000L)
        val habit2 = createTestHabit(id = "2", createdAt = 2000L)
        val habit3 = createTestHabit(id = "3", createdAt = 3000L)
        
        habitDao.insertHabits(listOf(habit1, habit2, habit3))

        val allHabits = habitDao.getAllHabitsSync()
        assertEquals("3", allHabits[0].id)
        assertEquals("2", allHabits[1].id)
        assertEquals("1", allHabits[2].id)
    }

    @Test
    fun getHabitById_existingHabit_returnsHabit() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals(habit.id, retrieved?.id)
    }

    @Test
    fun getHabitById_nonExistent_returnsNull() = runTest {
        val retrieved = habitDao.getHabitById("non-existent")
        assertNull(retrieved)
    }

    @Test
    fun getHabitsByType_returnsOnlyMatchingType() = runTest {
        val timeHabit = createTestHabit(id = "time", type = HabitType.TIME)
        val motionHabit = createTestHabit(id = "motion", type = HabitType.MOTION)
        
        habitDao.insertHabits(listOf(timeHabit, motionHabit))

        val timeHabits = habitDao.getHabitsByType(HabitType.TIME).first()
        assertEquals(1, timeHabits.size)
        assertEquals("time", timeHabits.first().id)
    }

    @Test
    fun getHabitsByCategory_returnsOnlyMatchingCategory() = runTest {
        val fitnessHabit = createTestHabit(id = "fitness", category = HabitCategory.FITNESS)
        val wellnessHabit = createTestHabit(id = "wellness", category = HabitCategory.WELLNESS)
        
        habitDao.insertHabits(listOf(fitnessHabit, wellnessHabit))

        val fitnessHabits = habitDao.getHabitsByCategory(HabitCategory.FITNESS).first()
        assertEquals(1, fitnessHabits.size)
        assertEquals("fitness", fitnessHabits.first().id)
    }

    @Test
    fun getMotionHabitsByType_returnsMatchingMotionHabits() = runTest {
        val walkingHabit = createTestHabit(
            id = "walking",
            type = HabitType.MOTION,
            motionType = "walking"
        )
        val runningHabit = createTestHabit(
            id = "running",
            type = HabitType.MOTION,
            motionType = "running"
        )
        
        habitDao.insertHabits(listOf(walkingHabit, runningHabit))

        val walkingHabits = habitDao.getMotionHabitsByType(HabitType.MOTION, "walking")
        assertEquals(1, walkingHabits.size)
        assertEquals("walking", walkingHabits.first().id)
    }

    @Test
    fun getLocationHabits_returnsOnlyLocationHabits() = runTest {
        val locationHabit = createTestHabit(
            id = "location",
            type = HabitType.LOCATION,
            location = com.microhabitcoach.data.model.LocationData(0.0, 0.0, "Test Address")
        )
        val timeHabit = createTestHabit(id = "time", type = HabitType.TIME)
        
        habitDao.insertHabits(listOf(locationHabit, timeHabit))

        val locationHabits = habitDao.getLocationHabits(HabitType.LOCATION)
        assertEquals(1, locationHabits.size)
        assertEquals("location", locationHabits.first().id)
    }

    @Test
    fun updateHabit_updatesSuccessfully() = runTest {
        val habit = createTestHabit(name = "Original")
        habitDao.insertHabit(habit)

        val updated = habit.copy(name = "Updated")
        habitDao.updateHabit(updated)

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals("Updated", retrieved?.name)
    }

    @Test
    fun deleteHabit_deletesSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        habitDao.deleteHabit(habit)

        val retrieved = habitDao.getHabitById(habit.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteHabitById_deletesSuccessfully() = runTest {
        val habit = createTestHabit()
        habitDao.insertHabit(habit)
        habitDao.deleteHabitById(habit.id)

        val retrieved = habitDao.getHabitById(habit.id)
        assertNull(retrieved)
    }

    @Test
    fun updateStreakCount_updatesStreak() = runTest {
        val habit = createTestHabit(streakCount = 5)
        habitDao.insertHabit(habit)

        habitDao.updateStreakCount(habit.id, 10, System.currentTimeMillis())

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals(10, retrieved?.streakCount)
    }

    @Test
    fun deactivateHabit_deactivatesSuccessfully() = runTest {
        val habit = createTestHabit(isActive = true)
        habitDao.insertHabit(habit)

        habitDao.deactivateHabit(habit.id, System.currentTimeMillis())

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals(false, retrieved?.isActive)
        
        val allHabits = habitDao.getAllHabitsSync()
        assertEquals(0, allHabits.size) // Should not appear in active habits
    }

    @Test
    fun insertHabit_withConflict_replacesExisting() = runTest {
        val habit = createTestHabit(name = "Original")
        habitDao.insertHabit(habit)

        val updated = habit.copy(name = "Replaced")
        habitDao.insertHabit(updated)

        val retrieved = habitDao.getHabitById(habit.id)
        assertEquals("Replaced", retrieved?.name)
    }

    private fun createTestHabit(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit",
        category: HabitCategory = HabitCategory.FITNESS,
        type: HabitType = HabitType.TIME,
        isActive: Boolean = true,
        createdAt: Long = System.currentTimeMillis(),
        streakCount: Int = 0,
        motionType: String? = null,
        location: com.microhabitcoach.data.model.LocationData? = null
    ): Habit {
        return Habit(
            id = id,
            name = name,
            category = category,
            type = type,
            reminderTimes = listOf(LocalTime.of(9, 0)),
            reminderDays = listOf(1, 2, 3, 4, 5, 6, 7),
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = createdAt,
            streakCount = streakCount,
            motionType = motionType,
            targetDuration = null,
            location = location,
            geofenceRadius = null
        )
    }
}

