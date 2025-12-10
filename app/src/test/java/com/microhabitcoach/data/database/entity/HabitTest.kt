package com.microhabitcoach.data.database.entity

import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.util.UUID

class HabitTest {

    @Test
    fun habit_creation_withDefaults_setsCorrectValues() {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME
        )

        assertEquals("Test Habit", habit.name)
        assertEquals(HabitCategory.FITNESS, habit.category)
        assertEquals(HabitType.TIME, habit.type)
        assertEquals(0, habit.streakCount)
        assertTrue(habit.isActive)
        assertTrue(habit.createdAt > 0)
        assertTrue(habit.updatedAt > 0)
    }

    @Test
    fun habit_timeBased_hasCorrectFields() {
        val times = listOf(LocalTime.of(9, 0), LocalTime.of(18, 0))
        val days = listOf(1, 3, 5)

        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = "Morning Water",
            category = HabitCategory.WELLNESS,
            type = HabitType.TIME,
            reminderTimes = times,
            reminderDays = days
        )

        assertEquals(HabitType.TIME, habit.type)
        assertEquals(times, habit.reminderTimes)
        assertEquals(days, habit.reminderDays)
        assertNull(habit.motionType)
        assertNull(habit.targetDuration)
        assertNull(habit.location)
    }

    @Test
    fun habit_motionBased_hasCorrectFields() {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = "Morning Walk",
            category = HabitCategory.FITNESS,
            type = HabitType.MOTION,
            motionType = "walk",
            targetDuration = 30
        )

        assertEquals(HabitType.MOTION, habit.type)
        assertEquals("walk", habit.motionType)
        assertEquals(30, habit.targetDuration)
        assertNull(habit.reminderTimes)
        assertNull(habit.location)
    }

    @Test
    fun habit_locationBased_hasCorrectFields() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Gym"
        )

        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = "Hydrate at Gym",
            category = HabitCategory.WELLNESS,
            type = HabitType.LOCATION,
            location = location,
            geofenceRadius = 100f
        )

        assertEquals(HabitType.LOCATION, habit.type)
        assertEquals(location, habit.location)
        assertEquals(100f, habit.geofenceRadius)
        assertNull(habit.motionType)
        assertNull(habit.reminderTimes)
    }

    @Test
    fun habit_copy_updatesFields() {
        val original = Habit(
            id = UUID.randomUUID().toString(),
            name = "Original",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            streakCount = 5
        )

        val updated = original.copy(
            name = "Updated",
            streakCount = 10,
            updatedAt = original.updatedAt + 1000
        )

        assertEquals("Updated", updated.name)
        assertEquals(10, updated.streakCount)
        assertEquals(original.id, updated.id)
        assertEquals(original.category, updated.category)
        assertTrue(updated.updatedAt > original.updatedAt)
    }

    @Test
    fun habit_equals_sameIdAndFields_returnsTrue() {
        val id = UUID.randomUUID().toString()
        val createdAt = System.currentTimeMillis()
        val habit1 = Habit(
            id = id,
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            createdAt = createdAt
        )
        val habit2 = Habit(
            id = id,
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            createdAt = createdAt
        )

        assertEquals(habit1, habit2)
    }
    
    @Test
    fun habit_equals_sameIdDifferentFields_returnsFalse() {
        val id = UUID.randomUUID().toString()
        val habit1 = Habit(
            id = id,
            name = "Habit 1",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME
        )
        val habit2 = Habit(
            id = id,
            name = "Habit 2",
            category = HabitCategory.WELLNESS,
            type = HabitType.MOTION
        )

        // Kotlin data classes compare ALL fields, not just ID
        assertFalse(habit1 == habit2)
    }

    @Test
    fun habit_equals_differentId_returnsFalse() {
        val habit1 = Habit(
            id = UUID.randomUUID().toString(),
            name = "Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME
        )
        val habit2 = Habit(
            id = UUID.randomUUID().toString(),
            name = "Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME
        )

        assertFalse(habit1 == habit2)
    }

    @Test
    fun habit_hashCode_sameFields_returnsSameHash() {
        val id = UUID.randomUUID().toString()
        val createdAt = System.currentTimeMillis()
        val habit1 = Habit(
            id = id,
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            createdAt = createdAt
        )
        val habit2 = Habit(
            id = id,
            name = "Test Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            createdAt = createdAt
        )

        assertEquals(habit1.hashCode(), habit2.hashCode())
    }

    @Test
    fun habit_deactivate_setsIsActiveToFalse() {
        val habit = Habit(
            id = UUID.randomUUID().toString(),
            name = "Test",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            isActive = true
        )

        val deactivated = habit.copy(isActive = false)
        assertFalse(deactivated.isActive)
    }
}

