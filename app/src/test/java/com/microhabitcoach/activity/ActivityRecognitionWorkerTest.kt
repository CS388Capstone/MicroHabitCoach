package com.microhabitcoach.activity

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.microhabitcoach.activity.ActivityDurationTracker
import com.microhabitcoach.activity.ActivityRecognitionService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
class ActivityRecognitionWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var worker: ActivityRecognitionWorker

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        worker = ActivityRecognitionWorker(context, workerParams)

        mockkObject(ActivityDurationTracker)
        mockkObject(ActivityRecognitionService)

        every { context.applicationContext } returns context
        every { ActivityDurationTracker.initialize(any()) } returns Unit
        every { ActivityDurationTracker.getCurrentActivity() } returns null
    }

    @Test
    fun doWork_noCurrentActivity_returnsSuccess() = runTest {
        every { ActivityDurationTracker.getCurrentActivity() } returns null

        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { ActivityDurationTracker.initialize(context) }
    }

    @Test
    fun doWork_withCurrentActivity_checksAutoComplete() = runTest {
        val motionType = "walk"
        every { ActivityDurationTracker.getCurrentActivity() } returns motionType
        coEvery { ActivityRecognitionService.checkAndAutoComplete(any(), any()) } returns Unit

        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { ActivityDurationTracker.initialize(context) }
        verify { ActivityDurationTracker.getCurrentActivity() }
        coVerify { ActivityRecognitionService.checkAndAutoComplete(context, motionType) }
    }

    @Test
    fun doWork_exception_returnsSuccess() = runTest {
        every { ActivityDurationTracker.getCurrentActivity() } throws RuntimeException("Test error")

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

        ActivityRecognitionWorker.schedulePeriodicCheck(context)

        verify { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
}

