package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class UserContextTest {

    @Test
    fun userContext_creation_withAllFields_setsCorrectly() {
        val preferredCategories = setOf(HabitCategory.FITNESS, HabitCategory.WELLNESS)
        val currentTime = LocalTime.of(9, 0)
        val weather = Weather(condition = WeatherCondition.SUNNY, temperature = 25.0)
        // Note: Android Location class requires Android runtime, so we test without it in unit tests
        val motionState = MotionState.WALKING

        val context = UserContext(
            preferredCategories = preferredCategories,
            currentTime = currentTime,
            currentWeather = weather,
            currentLocation = null, // Skip Android Location in unit tests
            recentMotionState = motionState
        )

        assertEquals(preferredCategories, context.preferredCategories)
        assertEquals(currentTime, context.currentTime)
        assertEquals(weather, context.currentWeather)
        assertNull(context.currentLocation) // Location requires Android runtime
        assertEquals(motionState, context.recentMotionState)
    }

    @Test
    fun userContext_creation_withNullFields_handlesCorrectly() {
        val context = UserContext(
            preferredCategories = emptySet(),
            currentTime = LocalTime.now(),
            currentWeather = null,
            currentLocation = null,
            recentMotionState = MotionState.UNKNOWN
        )

        assertNull(context.currentWeather)
        assertNull(context.currentLocation)
        assertEquals(MotionState.UNKNOWN, context.recentMotionState)
    }

    @Test
    fun userContext_creation_withDefaults_usesDefaults() {
        val context = UserContext(
            preferredCategories = emptySet(),
            currentTime = LocalTime.now()
        )

        assertEquals(MotionState.UNKNOWN, context.recentMotionState)
        assertNull(context.currentLocation)
        assertNull(context.currentWeather)
    }

    @Test
    fun userContext_copy_updatesFields() {
        val original = UserContext(
            preferredCategories = setOf(HabitCategory.FITNESS),
            currentTime = LocalTime.of(9, 0),
            currentWeather = null,
            currentLocation = null,
            recentMotionState = MotionState.STATIONARY
        )

        val updated = original.copy(
            preferredCategories = setOf(HabitCategory.WELLNESS),
            currentTime = LocalTime.of(12, 0),
            recentMotionState = MotionState.WALKING
        )

        assertEquals(setOf(HabitCategory.WELLNESS), updated.preferredCategories)
        assertEquals(LocalTime.of(12, 0), updated.currentTime)
        assertEquals(MotionState.WALKING, updated.recentMotionState)
    }
}

