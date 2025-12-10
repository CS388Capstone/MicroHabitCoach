package com.microhabitcoach.data.database.entity

import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun userPreferences_creation_withDefaults_setsCorrectValues() {
        val prefs = UserPreferences()

        assertEquals("default_user", prefs.userId)
        assertTrue(prefs.preferredCategories.isEmpty())
        assertNull(prefs.quietHoursStart)
        assertNull(prefs.quietHoursEnd)
        assertTrue(prefs.notificationsEnabled)
        assertFalse(prefs.batteryOptimizationMode)
        assertFalse(prefs.hasCompletedOnboarding)
        assertTrue(prefs.createdAt > 0)
        assertTrue(prefs.updatedAt > 0)
    }

    @Test
    fun userPreferences_withAllFields_setsCorrectly() {
        val categories = setOf(HabitCategory.FITNESS, HabitCategory.WELLNESS)
        val prefs = UserPreferences(
            userId = "user123",
            preferredCategories = categories,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            notificationsEnabled = false,
            batteryOptimizationMode = true,
            hasCompletedOnboarding = true,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        assertEquals("user123", prefs.userId)
        assertEquals(categories, prefs.preferredCategories)
        assertEquals("22:00", prefs.quietHoursStart)
        assertEquals("08:00", prefs.quietHoursEnd)
        assertFalse(prefs.notificationsEnabled)
        assertTrue(prefs.batteryOptimizationMode)
        assertTrue(prefs.hasCompletedOnboarding)
        assertEquals(1000L, prefs.createdAt)
        assertEquals(2000L, prefs.updatedAt)
    }

    @Test
    fun userPreferences_copy_updatesFields() {
        val original = UserPreferences(
            userId = "user123",
            notificationsEnabled = true
        )

        val updated = original.copy(
            notificationsEnabled = false,
            quietHoursStart = "22:00",
            updatedAt = original.updatedAt + 1000
        )

        assertFalse(updated.notificationsEnabled)
        assertEquals("22:00", updated.quietHoursStart)
        assertTrue(updated.updatedAt > original.updatedAt)
        assertEquals(original.userId, updated.userId)
    }

    @Test
    fun habitCategorySetConverter_roundTrip_preservesValue() {
        val categories = setOf(
            HabitCategory.FITNESS,
            HabitCategory.WELLNESS,
            HabitCategory.PRODUCTIVITY
        )

        val json = HabitCategorySetConverter.fromHabitCategorySet(categories)
        val convertedBack = HabitCategorySetConverter.toHabitCategorySet(json)

        assertEquals(categories, convertedBack)
    }

    @Test
    fun habitCategorySetConverter_null_returnsEmptySet() {
        val result = HabitCategorySetConverter.toHabitCategorySet(null)
        assertTrue(result!!.isEmpty())
    }

    @Test
    fun habitCategorySetConverter_emptySet_handlesCorrectly() {
        val emptySet = emptySet<HabitCategory>()
        val json = HabitCategorySetConverter.fromHabitCategorySet(emptySet)
        val convertedBack = HabitCategorySetConverter.toHabitCategorySet(json)

        assertTrue(convertedBack!!.isEmpty())
    }
}

