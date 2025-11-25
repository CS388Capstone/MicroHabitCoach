package com.microhabitcoach.ui.habit

import com.microhabitcoach.data.model.HabitType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HabitFormValidatorTest {

    @Test
    fun timeHabit_valid() {
        val result = HabitFormValidator.validate(
            name = "Drink Water",
            type = HabitType.TIME,
            times = listOf(LocalTime.of(9,0)),
            days = listOf(1,3,5),
            motionType = null,
            duration = null,
            hasLocation = false,
            radius = null
        )
        assertTrue(result.valid)
    }

    @Test
    fun timeHabit_missingFields() {
        val result = HabitFormValidator.validate(
            name = "",
            type = HabitType.TIME,
            times = emptyList(),
            days = emptyList(),
            motionType = null,
            duration = null,
            hasLocation = false,
            radius = null
        )
        assertFalse(result.valid)
        assertTrue(result.errors.contains("Name required"))
        assertTrue(result.errors.contains("At least one time"))
        assertTrue(result.errors.contains("Select days"))
    }

    @Test
    fun motionHabit_invalidDuration() {
        val result = HabitFormValidator.validate(
            name = "Morning Walk",
            type = HabitType.MOTION,
            times = emptyList(),
            days = emptyList(),
            motionType = "walk",
            duration = 0,
            hasLocation = false,
            radius = null
        )
        assertFalse(result.valid)
        assertTrue(result.errors.contains("Duration must be >0"))
    }

    @Test
    fun locationHabit_missingRadius() {
        val result = HabitFormValidator.validate(
            name = "Arrive at Gym",
            type = HabitType.LOCATION,
            times = emptyList(),
            days = emptyList(),
            motionType = null,
            duration = null,
            hasLocation = true,
            radius = 0f
        )
        assertFalse(result.valid)
        assertTrue(result.errors.contains("Radius must be >0"))
    }
}
