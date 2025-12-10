package com.microhabitcoach.activity

import android.content.Context
import android.content.SharedPreferences
import com.microhabitcoach.data.model.MotionState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ActivityDurationTrackerTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("activity_duration_tracker", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.apply() } returns Unit
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.clear() } returns editor

        ActivityDurationTracker.initialize(context)
    }

    @After
    fun tearDown() {
        ActivityDurationTracker.clearAll()
    }

    @Test
    fun initialize_setsSharedPreferences() {
        ActivityDurationTracker.initialize(context)
        verify { context.getSharedPreferences("activity_duration_tracker", Context.MODE_PRIVATE) }
    }

    @Test
    fun startActivity_savesStartTimeAndCurrentActivity() {
        val motionType = "walk"
        val timestamp = 1000L

        every { sharedPreferences.getString("current_activity", null) } returns null
        every { sharedPreferences.getLong("activity_start_walk", 0) } returns 0L

        ActivityDurationTracker.startActivity(motionType, timestamp)

        verify { editor.putLong("activity_start_walk", timestamp) }
        verify { editor.putString("current_activity", motionType) }
        verify { editor.apply() }
    }

    @Test
    fun getActivityDuration_noStartTime_returnsZero() {
        val motionType = "walk"
        every { sharedPreferences.getLong("activity_start_walk", 0) } returns 0L

        val duration = ActivityDurationTracker.getActivityDuration(motionType)

        assertEquals(0L, duration)
    }

    @Test
    fun getActivityDuration_validStartTime_returnsDurationInMinutes() {
        val motionType = "walk"
        val startTime = System.currentTimeMillis() - (30 * 60 * 1000L) // 30 minutes ago
        val currentTime = System.currentTimeMillis()

        every { sharedPreferences.getLong("activity_start_walk", 0) } returns startTime

        val duration = ActivityDurationTracker.getActivityDuration(motionType, currentTime)

        assertEquals(30L, duration)
    }

    @Test
    fun getActivityDuration_startTimeInFuture_clearsAndReturnsZero() {
        val motionType = "walk"
        val futureTime = System.currentTimeMillis() + 1000L
        val currentTime = System.currentTimeMillis()

        every { sharedPreferences.getLong("activity_start_walk", 0) } returns futureTime
        every { sharedPreferences.getString("current_activity", null) } returns motionType

        val duration = ActivityDurationTracker.getActivityDuration(motionType, currentTime)

        assertEquals(0L, duration)
        verify { editor.remove("activity_start_walk") }
    }

    @Test
    fun getActivityDuration_startTimeTooOld_clearsAndReturnsZero() {
        val motionType = "walk"
        val oldTime = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // 25 hours ago
        val currentTime = System.currentTimeMillis()

        every { sharedPreferences.getLong("activity_start_walk", 0) } returns oldTime
        every { sharedPreferences.getString("current_activity", null) } returns motionType

        val duration = ActivityDurationTracker.getActivityDuration(motionType, currentTime)

        assertEquals(0L, duration)
        verify { editor.remove("activity_start_walk") }
    }

    @Test
    fun clearActivity_removesActivityTracking() {
        val motionType = "walk"
        every { sharedPreferences.getString("current_activity", null) } returns motionType

        ActivityDurationTracker.clearActivity(motionType)

        verify { editor.remove("activity_start_walk") }
        verify { editor.remove("current_activity") }
        verify { editor.apply() }
    }

    @Test
    fun clearActivity_differentCurrentActivity_doesNotRemoveCurrentActivity() {
        val motionType = "walk"
        every { sharedPreferences.getString("current_activity", null) } returns "run"

        ActivityDurationTracker.clearActivity(motionType)

        verify { editor.remove("activity_start_walk") }
        verify(exactly = 0) { editor.remove("current_activity") }
    }

    @Test
    fun getCurrentActivity_noActivity_returnsNull() {
        every { sharedPreferences.getString("current_activity", null) } returns null

        val result = ActivityDurationTracker.getCurrentActivity()

        assertNull(result)
    }

    @Test
    fun getCurrentActivity_hasActivity_returnsActivity() {
        val motionType = "walk"
        every { sharedPreferences.getString("current_activity", null) } returns motionType

        val result = ActivityDurationTracker.getCurrentActivity()

        assertEquals(motionType, result)
    }

    @Test
    fun getCurrentMotionState_walk_returnsWalking() {
        every { sharedPreferences.getString("current_activity", null) } returns "walk"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.WALKING, result)
    }

    @Test
    fun getCurrentMotionState_run_returnsRunning() {
        every { sharedPreferences.getString("current_activity", null) } returns "run"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.RUNNING, result)
    }

    @Test
    fun getCurrentMotionState_running_returnsRunning() {
        every { sharedPreferences.getString("current_activity", null) } returns "running"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.RUNNING, result)
    }

    @Test
    fun getCurrentMotionState_stationary_returnsStationary() {
        every { sharedPreferences.getString("current_activity", null) } returns "stationary"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.STATIONARY, result)
    }

    @Test
    fun getCurrentMotionState_still_returnsStationary() {
        every { sharedPreferences.getString("current_activity", null) } returns "still"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.STATIONARY, result)
    }

    @Test
    fun getCurrentMotionState_vehicle_returnsInVehicle() {
        every { sharedPreferences.getString("current_activity", null) } returns "vehicle"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.IN_VEHICLE, result)
    }

    @Test
    fun getCurrentMotionState_inVehicle_returnsInVehicle() {
        every { sharedPreferences.getString("current_activity", null) } returns "in_vehicle"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.IN_VEHICLE, result)
    }

    @Test
    fun getCurrentMotionState_unknown_returnsUnknown() {
        every { sharedPreferences.getString("current_activity", null) } returns "unknown"

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.UNKNOWN, result)
    }

    @Test
    fun getCurrentMotionState_noActivity_returnsUnknown() {
        every { sharedPreferences.getString("current_activity", null) } returns null

        val result = ActivityDurationTracker.getCurrentMotionState()

        assertEquals(MotionState.UNKNOWN, result)
    }

    @Test
    fun clearAll_clearsAllPreferences() {
        ActivityDurationTracker.clearAll()

        verify { editor.clear() }
        verify { editor.apply() }
    }
}

