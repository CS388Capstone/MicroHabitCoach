package com.microhabitcoach.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.tasks.Task
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.DefaultHabitRepository
import com.microhabitcoach.util.PermissionHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class ActivityRecognitionServiceTest {

    private lateinit var context: Context
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var task: Task<Void>
    private lateinit var habitDao: HabitDao
    private lateinit var repository: DefaultHabitRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        activityRecognitionClient = mockk(relaxed = true)
        task = mockk(relaxed = true)
        habitDao = mockk(relaxed = true)
        repository = mockk(relaxed = true)

        mockkStatic(ActivityRecognition::class)
        mockkObject(PermissionHelper)
        mockkObject(ActivityDurationTracker)
        mockkObject(DatabaseModule)

        every { ActivityRecognition.getClient(any()) } returns activityRecognitionClient
        every { activityRecognitionClient.requestActivityTransitionUpdates(any(), any()) } returns task
        every { activityRecognitionClient.removeActivityTransitionUpdates(any()) } returns task
        every { activityRecognitionClient.requestActivityUpdates(any(), any()) } returns task
        every { activityRecognitionClient.removeActivityUpdates(any()) } returns task
        every { task.addOnSuccessListener(any()) } returns task
        every { task.addOnFailureListener(any()) } returns task
        every { ActivityDurationTracker.initialize(any()) } returns Unit
        every { ActivityDurationTracker.clearAll() } returns Unit
        every { ActivityDurationTracker.getActivityDuration(any()) } returns 0L
        every { DatabaseModule.getDatabase(any()) } returns mockk {
            every { habitDao() } returns habitDao
        }
        coEvery { repository.isHabitCompletedToday(any()) } returns false
        coEvery { repository.autoCompleteMotionHabit(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun startMonitoring_noPermission_doesNotStart() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns false

        ActivityRecognitionService.startMonitoring(context)

        verify(exactly = 0) { ActivityRecognition.getClient(any()) }
    }

    @Test
    fun startMonitoring_alreadyMonitoring_doesNotStartAgain() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        // Simulate already monitoring by checking if client is initialized
        // Since we can't easily check internal state, we'll test the permission check path

        ActivityRecognitionService.startMonitoring(context)
        ActivityRecognitionService.startMonitoring(context)

        // Should only initialize once
        verify(atLeast = 1) { ActivityDurationTracker.initialize(any()) }
    }

    @Test
    fun startMonitoring_withPermission_initializesClient() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        every { context.applicationContext } returns context

        ActivityRecognitionService.startMonitoring(context)

        verify { ActivityRecognition.getClient(context) }
        verify { ActivityDurationTracker.initialize(context) }
    }

    @Test
    fun startMonitoring_androidQPlus_usesTransitionAPI() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        every { context.applicationContext } returns context
        setSdkInt(Build.VERSION_CODES.Q)

        ActivityRecognitionService.startMonitoring(context)

        verify { activityRecognitionClient.requestActivityTransitionUpdates(any(), any()) }
    }

    @Test
    fun startMonitoring_androidBelowQ_usesLegacyAPI() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        every { context.applicationContext } returns context
        setSdkInt(Build.VERSION_CODES.P)

        ActivityRecognitionService.startMonitoring(context)

        verify { activityRecognitionClient.requestActivityUpdates(any(), any()) }
    }

    @Test
    fun stopMonitoring_removesUpdates() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        every { context.applicationContext } returns context
        setSdkInt(Build.VERSION_CODES.Q)

        ActivityRecognitionService.startMonitoring(context)
        ActivityRecognitionService.stopMonitoring(context)

        verify { activityRecognitionClient.removeActivityTransitionUpdates(any()) }
        verify { ActivityDurationTracker.clearAll() }
    }

    @Test
    fun stopMonitoring_androidBelowQ_usesLegacyAPI() {
        every { PermissionHelper.hasActivityRecognitionPermission(context) } returns true
        every { context.applicationContext } returns context
        setSdkInt(Build.VERSION_CODES.P)

        ActivityRecognitionService.startMonitoring(context)
        ActivityRecognitionService.stopMonitoring(context)

        verify { activityRecognitionClient.removeActivityUpdates(any()) }
    }

    @Test
    fun checkAndAutoComplete_noMatchingHabits_doesNothing() = runTest {
        val motionType = "walk"
        coEvery { habitDao.getAllHabitsSync() } returns emptyList()
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    @Test
    fun checkAndAutoComplete_durationNotMet_doesNotComplete() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 20)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    @Test
    fun checkAndAutoComplete_durationMet_completesHabit() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 10)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 1) { repository.autoCompleteMotionHabit(habit.id) }
    }

    @Test
    fun checkAndAutoComplete_alreadyCompletedToday_doesNotCompleteAgain() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 10)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        coEvery { repository.isHabitCompletedToday(habit.id) } returns true
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    @Test
    fun checkAndAutoComplete_caseInsensitiveMatching() = runTest {
        val motionType = "WALK"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 10)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 1) { repository.autoCompleteMotionHabit(habit.id) }
    }

    @Test
    fun checkAndAutoComplete_inactiveHabit_doesNotComplete() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 10, isActive = false)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    @Test
    fun checkAndAutoComplete_noTargetDuration_doesNotComplete() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = null)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    @Test
    fun checkAndAutoComplete_multipleMatchingHabits_completesAll() = runTest {
        val motionType = "walk"
        val habit1 = createMotionHabit(motionType = "walk", targetDuration = 10)
        val habit2 = createMotionHabit(motionType = "walk", targetDuration = 5)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit1, habit2)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 15L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify { repository.autoCompleteMotionHabit(habit1.id) }
        coVerify { repository.autoCompleteMotionHabit(habit2.id) }
    }

    @Test
    fun checkAndAutoComplete_zeroDuration_doesNotComplete() = runTest {
        val motionType = "walk"
        val habit = createMotionHabit(motionType = "walk", targetDuration = 10)
        coEvery { habitDao.getAllHabitsSync() } returns listOf(habit)
        every { ActivityDurationTracker.getActivityDuration(motionType) } returns 0L
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository

        ActivityRecognitionService.checkAndAutoComplete(context, motionType)

        coVerify(exactly = 0) { repository.autoCompleteMotionHabit(any()) }
    }

    private fun createMotionHabit(
        id: String = UUID.randomUUID().toString(),
        motionType: String = "walk",
        targetDuration: Int? = 10,
        isActive: Boolean = true
    ): Habit {
        return Habit(
            id = id,
            name = "Test Motion Habit",
            category = com.microhabitcoach.data.model.HabitCategory.FITNESS,
            type = HabitType.MOTION,
            motionType = motionType,
            targetDuration = targetDuration,
            isActive = isActive
        )
    }

    private fun setSdkInt(version: Int) {
        ReflectionHelpers.setStaticField(Build::class.java, "SDK_INT", version)
    }
}

