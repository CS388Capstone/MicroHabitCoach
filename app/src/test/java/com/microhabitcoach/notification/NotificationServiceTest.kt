package com.microhabitcoach.notification

import android.content.Context
import com.microhabitcoach.activity.ActivityRecognitionService
import com.microhabitcoach.activity.ActivityRecognitionWorker
import com.microhabitcoach.geofence.GeofenceService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
class NotificationServiceTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockkObject(HabitNotificationManager)
        mockkObject(StreakCountdownWorker)
        mockkObject(InactivityDetectionWorker)
        mockkObject(ActivityRecognitionWorker)
        mockkObject(ActivityRecognitionService)
        mockkObject(GeofenceService)

        every { HabitNotificationManager.getInstance(any()) } returns mockk()
        every { StreakCountdownWorker.schedulePeriodicCheck(any()) } returns Unit
        every { InactivityDetectionWorker.schedulePeriodicCheck(any()) } returns Unit
        every { ActivityRecognitionWorker.schedulePeriodicCheck(any()) } returns Unit
        every { ActivityRecognitionService.startMonitoring(any()) } returns Unit
        every { GeofenceService.updateGeofences(any()) } returns Unit
    }

    @Test
    fun initialize_initializesAllServices() {
        NotificationService.initialize(context)

        verify { HabitNotificationManager.getInstance(context) }
        verify { StreakCountdownWorker.schedulePeriodicCheck(context) }
        verify { InactivityDetectionWorker.schedulePeriodicCheck(context) }
        verify { ActivityRecognitionWorker.schedulePeriodicCheck(context) }
        verify { ActivityRecognitionService.startMonitoring(context) }
        verify { GeofenceService.updateGeofences(context) }
    }

    @Test
    fun cancelAll_cancelsAllWork() {
        val workManager = mockk<androidx.work.WorkManager>(relaxed = true)
        mockkStatic(androidx.work.WorkManager::class)
        every { androidx.work.WorkManager.getInstance(context) } returns workManager
        every { workManager.cancelAllWork() } returns mockk()

        NotificationService.cancelAll(context)

        verify { workManager.cancelAllWork() }
        verify { ActivityRecognitionService.stopMonitoring(context) }
        verify { GeofenceService.removeAllGeofences(context) }
    }
}

