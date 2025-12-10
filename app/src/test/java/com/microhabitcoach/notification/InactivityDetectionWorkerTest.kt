package com.microhabitcoach.notification

import android.content.Context
import androidx.work.WorkerParameters
import com.microhabitcoach.activity.ActivityDurationTracker
import com.microhabitcoach.data.model.MotionState
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class InactivityDetectionWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)

        mockkObject(ActivityDurationTracker)
        mockkObject(HabitNotificationManager)

        every { context.applicationContext } returns context
        every { HabitNotificationManager.getInstance(any()) } returns mockk {
            every { showInactivityNotification() } returns true
        }
    }

    @Test
    fun doWork_notStationary_doesNotNotify() = runTest {
        every { ActivityDurationTracker.getCurrentMotionState() } returns MotionState.WALKING

        val worker = InactivityDetectionWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify(exactly = 0) { HabitNotificationManager.getInstance(any()) }
    }

    @Test
    fun doWork_stationaryLessThan30Minutes_doesNotNotify() = runTest {
        every { ActivityDurationTracker.getCurrentMotionState() } returns MotionState.STATIONARY
        every { ActivityDurationTracker.getActivityDuration("stationary") } returns 20L

        val worker = InactivityDetectionWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify(exactly = 0) { HabitNotificationManager.getInstance(any()) }
    }

    @Test
    fun doWork_stationary30MinutesOrMore_notifies() = runTest {
        every { ActivityDurationTracker.getCurrentMotionState() } returns MotionState.STATIONARY
        every { ActivityDurationTracker.getActivityDuration("stationary") } returns 30L

        val worker = InactivityDetectionWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { HabitNotificationManager.getInstance(context).showInactivityNotification() }
    }

    @Test
    fun doWork_stationaryMoreThan30Minutes_notifies() = runTest {
        every { ActivityDurationTracker.getCurrentMotionState() } returns MotionState.STATIONARY
        every { ActivityDurationTracker.getActivityDuration("stationary") } returns 60L

        val worker = InactivityDetectionWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { HabitNotificationManager.getInstance(context).showInactivityNotification() }
    }

    @Test
    fun doWork_exception_returnsSuccess() = runTest {
        every { ActivityDurationTracker.getCurrentMotionState() } throws RuntimeException("Test error")

        val worker = InactivityDetectionWorker(context, workerParams)
        val result = worker.doWork()

        // Worker returns success even on errors (as per implementation)
        assert(result is androidx.work.ListenableWorker.Result.Success)
    }

    @Test
    fun schedulePeriodicCheck_schedulesWork() {
        val workManager = mockk<androidx.work.WorkManager>(relaxed = true)
        mockkStatic(androidx.work.WorkManager::class)
        every { androidx.work.WorkManager.getInstance(context) } returns workManager
        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        InactivityDetectionWorker.schedulePeriodicCheck(context)

        verify { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
}

