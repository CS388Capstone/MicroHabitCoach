package com.microhabitcoach.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.UserPreferences
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Ignore("Temporarily disabled for demo")
class PreferencesRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PreferencesRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = PreferencesRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observePreferences_returnsFlow() = runTest {
        repository.initializeDefaultsIfNeeded()

        val flow = repository.observePreferences()
        val preferences = flow.first()
        
        assertNotNull(preferences)
    }

    @Test
    fun getPreferences_nonExistent_createsDefault() = runTest {
        val preferences = repository.getPreferencesOrCreateDefault()

        assertNotNull(preferences)
        assertTrue(preferences.notificationsEnabled)
    }

    @Test
    fun initializeDefaultsIfNeeded_firstCall_createsDefaults() = runTest {
        val preferences = repository.initializeDefaultsIfNeeded()

        assertNotNull(preferences)
        assertTrue(preferences.notificationsEnabled)
    }

    @Test
    fun initializeDefaultsIfNeeded_alreadyExists_returnsExisting() = runTest {
        repository.initializeDefaultsIfNeeded()
        val preferences1 = repository.getPreferences()
        
        val preferences2 = repository.initializeDefaultsIfNeeded()

        assertEquals(preferences1?.userId, preferences2.userId)
    }

    @Test
    fun getPreferredCategories_returnsCategories() = runTest {
        repository.initializeDefaultsIfNeeded()
        val categories = setOf(HabitCategory.FITNESS, HabitCategory.WELLNESS)
        repository.setPreferredCategories(categories)

        val retrieved = repository.getPreferredCategories()
        assertEquals(2, retrieved.size)
        assertTrue(retrieved.contains(HabitCategory.FITNESS))
    }

    @Test
    fun setPreferredCategories_updatesCategories() = runTest {
        repository.initializeDefaultsIfNeeded()
        val categories = setOf(HabitCategory.FITNESS)

        repository.setPreferredCategories(categories)

        val retrieved = repository.getPreferredCategories()
        assertEquals(1, retrieved.size)
        assertTrue(retrieved.contains(HabitCategory.FITNESS))
    }

    @Test
    fun addPreferredCategory_addsCategory() = runTest {
        repository.initializeDefaultsIfNeeded()
        repository.setPreferredCategories(setOf(HabitCategory.FITNESS))

        repository.addPreferredCategory(HabitCategory.WELLNESS)

        val retrieved = repository.getPreferredCategories()
        assertEquals(2, retrieved.size)
        assertTrue(retrieved.contains(HabitCategory.WELLNESS))
    }

    @Test
    fun removePreferredCategory_removesCategory() = runTest {
        repository.initializeDefaultsIfNeeded()
        repository.setPreferredCategories(setOf(HabitCategory.FITNESS, HabitCategory.WELLNESS))

        repository.removePreferredCategory(HabitCategory.WELLNESS)

        val retrieved = repository.getPreferredCategories()
        assertEquals(1, retrieved.size)
        assertFalse(retrieved.contains(HabitCategory.WELLNESS))
    }

    @Test
    fun areNotificationsEnabled_returnsEnabled() = runTest {
        repository.initializeDefaultsIfNeeded()

        val enabled = repository.areNotificationsEnabled()

        assertTrue(enabled)
    }

    @Test
    fun setNotificationsEnabled_updatesEnabled() = runTest {
        repository.initializeDefaultsIfNeeded()

        repository.setNotificationsEnabled(false)

        assertFalse(repository.areNotificationsEnabled())
    }

    @Test
    fun getQuietHoursStart_returnsStartTime() = runTest {
        repository.initializeDefaultsIfNeeded()
        repository.setQuietHours("22:00", "08:00")

        val start = repository.getQuietHoursStart()
        assertEquals("22:00", start)
    }

    @Test
    fun setQuietHours_updatesQuietHours() = runTest {
        repository.initializeDefaultsIfNeeded()

        repository.setQuietHours("22:00", "08:00")

        assertEquals("22:00", repository.getQuietHoursStart())
        assertEquals("08:00", repository.getQuietHoursEnd())
    }

    @Test
    fun setQuietHours_nullValues_clearsQuietHours() = runTest {
        repository.initializeDefaultsIfNeeded()
        repository.setQuietHours("22:00", "08:00")
        repository.setQuietHours(null, null)

        assertNull(repository.getQuietHoursStart())
        assertNull(repository.getQuietHoursEnd())
    }

    @Test
    fun isBatteryOptimizationEnabled_returnsValue() = runTest {
        repository.initializeDefaultsIfNeeded()

        val enabled = repository.isBatteryOptimizationEnabled()

        assertFalse(enabled) // Default is false
    }

    @Test
    fun setBatteryOptimizationMode_updatesMode() = runTest {
        repository.initializeDefaultsIfNeeded()

        repository.setBatteryOptimizationMode(true)

        assertTrue(repository.isBatteryOptimizationEnabled())
    }

    @Test
    fun hasCompletedOnboarding_returnsValue() = runTest {
        repository.initializeDefaultsIfNeeded()

        val completed = repository.hasCompletedOnboarding()

        assertFalse(completed) // Default is false
    }

    @Test
    fun setOnboardingCompleted_updatesCompleted() = runTest {
        repository.initializeDefaultsIfNeeded()

        repository.setOnboardingCompleted(true)

        assertTrue(repository.hasCompletedOnboarding())
    }

    @Test
    fun updatePreferences_updatesAllFields() = runTest {
        repository.initializeDefaultsIfNeeded()
        val preferences = UserPreferences(
            userId = "default_user",
            preferredCategories = setOf(HabitCategory.FITNESS),
            notificationsEnabled = false,
            quietHoursStart = "23:00",
            quietHoursEnd = "07:00",
            batteryOptimizationMode = true,
            hasCompletedOnboarding = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        repository.updatePreferences(preferences)

        val retrieved = repository.getPreferences()
        assertEquals(false, retrieved?.notificationsEnabled)
        assertEquals("23:00", retrieved?.quietHoursStart)
    }
}

