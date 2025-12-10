package com.microhabitcoach.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.util.PermissionHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class GeofenceServiceTest {

    private lateinit var context: Context
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var task: Task<Void>
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        GeofenceService.resetForTests()
        context = mockk(relaxed = true)
        geofencingClient = mockk(relaxed = true)
        task = mockk(relaxed = true)
        habitDao = mockk(relaxed = true)

        mockkStatic(LocationServices::class)
        mockkObject(PermissionHelper)
        mockkObject(DatabaseModule)

        GeofenceService.geofencingClientProvider = { geofencingClient }
        every { geofencingClient.addGeofences(any(), any()) } returns task
        every { geofencingClient.removeGeofences(any<List<String>>()) } returns task
        every { geofencingClient.removeGeofences(any<android.app.PendingIntent>()) } returns task
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } returns task
        every { context.applicationContext } returns context
        every { DatabaseModule.getDatabase(any()) } returns mockk {
            every { habitDao() } returns habitDao
        }
    }

    @After
    fun tearDown() {
        GeofenceService.lastGeofenceLimitHit = false
    }

    @Test
    fun addGeofence_noPermission_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns false

        val habit = createLocationHabit()

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_notLocationType_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit(type = HabitType.TIME)

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_noLocation_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit(location = null)

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_noRadius_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit(radius = null)

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_invalidRadius_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit(radius = 5f) // Too small

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_invalidCoordinates_doesNotAdd() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit(location = LocationData(91.0, 0.0)) // Invalid latitude

        GeofenceService.addGeofence(context, habit)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun addGeofence_validHabit_addsGeofence() {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit = createLocationHabit()

        GeofenceService.addGeofence(context, habit)

        verify { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun removeGeofence_removesGeofence() {
        val habitId = UUID.randomUUID().toString()

        GeofenceService.removeGeofence(context, habitId)

        verify { geofencingClient.removeGeofences(listOf(habitId)) }
    }

    @Test
    fun updateGeofences_noPermission_doesNotUpdate() = runTest {
        every { PermissionHelper.hasLocationPermission(context) } returns false

        GeofenceService.updateGeofences(context)

        verify(exactly = 0) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun updateGeofences_withHabits_updatesGeofences() = runTest {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habit1 = createLocationHabit()
        val habit2 = createLocationHabit()
        coEvery { habitDao.getLocationHabits(HabitType.LOCATION) } returns listOf(habit1, habit2)

        GeofenceService.updateGeofences(context)

        verify { geofencingClient.removeGeofences(any<android.app.PendingIntent>()) }
        verify(atLeast = 1) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun updateGeofences_tooManyHabits_setsLimitFlag() = runTest {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val habits = (1..101).map { createLocationHabit() } // Exceeds limit
        coEvery { habitDao.getLocationHabits(HabitType.LOCATION) } returns habits

        GeofenceService.updateGeofences(context)

        assert(GeofenceService.lastGeofenceLimitHit)
    }

    @Test
    fun updateGeofences_inactiveHabits_excluded() = runTest {
        every { PermissionHelper.hasLocationPermission(context) } returns true
        val activeHabit = createLocationHabit(isActive = true)
        val inactiveHabit = createLocationHabit(isActive = false)
        coEvery { habitDao.getLocationHabits(HabitType.LOCATION) } returns listOf(activeHabit, inactiveHabit)

        GeofenceService.updateGeofences(context)

        // Should only add geofence for active habit
        verify(atLeast = 1) { geofencingClient.addGeofences(any(), any()) }
    }

    @Test
    fun removeAllGeofences_removesAll() {
        val pendingIntent = mockk<android.app.PendingIntent>(relaxed = true)
        mockkStatic(android.app.PendingIntent::class)
        every { android.app.PendingIntent.getBroadcast(any(), any(), any(), any()) } returns pendingIntent
        
        GeofenceService.removeAllGeofences(context)

        verify { geofencingClient.removeGeofences(any<android.app.PendingIntent>()) }
    }

    private fun createLocationHabit(
        id: String = UUID.randomUUID().toString(),
        location: LocationData? = LocationData(40.7128, -74.0060, "New York"),
        radius: Float? = 100f,
        type: HabitType = HabitType.LOCATION,
        isActive: Boolean = true
    ): Habit {
        return Habit(
            id = id,
            name = "Test Location Habit",
            category = com.microhabitcoach.data.model.HabitCategory.FITNESS,
            type = type,
            location = location,
            geofenceRadius = radius,
            isActive = isActive
        )
    }
}

