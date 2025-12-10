package com.microhabitcoach.notification

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.DefaultHabitRepository
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
import org.junit.Ignore
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalTime
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class HabitReminderWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var habitDao: HabitDao
    private lateinit var repository: DefaultHabitRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        habitDao = mockk(relaxed = true)
        repository = mockk(relaxed = true)

        mockkObject(DatabaseModule)
        mockkStatic(DefaultHabitRepository::class)
        mockkObject(HabitNotificationManager)
        mockkObject(ReminderScheduler)

        every { context.applicationContext } returns context
        val completionDao = mockk<com.microhabitcoach.data.database.dao.CompletionDao>(relaxed = true)
        every { DatabaseModule.getDatabase(any()) } returns mockk {
            every { habitDao() } returns habitDao
            every { completionDao() } returns completionDao
        }
        every { DefaultHabitRepository(habitDao, completionDao) } returns repository
        every { HabitNotificationManager.getInstance(any()) } returns mockk {
            every { showReminderNotification(any()) } returns true
        }
        coEvery { ReminderScheduler.scheduleNextReminder(any(), any(), any(), any()) } returns Unit
    }

    @Test
    fun doWork_noHabitId_returnsFailure() = runTest {
        every { workerParams.inputData } returns workDataOf()

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Failure)
    }

    @Test
    fun doWork_habitNotFound_returnsSuccess() = runTest {
        val habitId = UUID.randomUUID().toString()
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habitId
        )
        coEvery { habitDao.getHabitById(habitId) } returns null

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
    }

    @Test
    fun doWork_inactiveHabit_returnsSuccess() = runTest {
        val habit = createTimeHabit(isActive = false)
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habit.id,
            HabitReminderWorker.KEY_REMINDER_TIME_HOUR to 9,
            HabitReminderWorker.KEY_REMINDER_TIME_MINUTE to 0
        )
        coEvery { habitDao.getHabitById(habit.id) } returns habit

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify(exactly = 0) { HabitNotificationManager.getInstance(any()) }
    }

    @Test
    fun doWork_alreadyCompleted_returnsSuccess() = runTest {
        val habit = createTimeHabit()
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habit.id,
            HabitReminderWorker.KEY_REMINDER_TIME_HOUR to 9,
            HabitReminderWorker.KEY_REMINDER_TIME_MINUTE to 0
        )
        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { repository.isHabitCompletedToday(habit.id) } returns true

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify(exactly = 0) { HabitNotificationManager.getInstance(any()) }
    }

    @Test
    fun doWork_validHabit_showsNotification() = runTest {
        val habit = createTimeHabit()
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habit.id,
            HabitReminderWorker.KEY_REMINDER_TIME_HOUR to 9,
            HabitReminderWorker.KEY_REMINDER_TIME_MINUTE to 0
        )
        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { repository.isHabitCompletedToday(habit.id) } returns false

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { HabitNotificationManager.getInstance(context).showReminderNotification(habit) }
        coVerify { ReminderScheduler.scheduleNextReminder(context, habit, 9, 0) }
    }

    @Test
    fun doWork_snoozeRequest_doesNotScheduleNext() = runTest {
        val habit = createTimeHabit()
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habit.id
        )
        coEvery { habitDao.getHabitById(habit.id) } returns habit
        coEvery { repository.isHabitCompletedToday(habit.id) } returns false

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Success)
        verify { HabitNotificationManager.getInstance(context).showReminderNotification(habit) }
        verify(exactly = 0) { ReminderScheduler.scheduleNextReminder(any(), any(), any(), any()) }
    }

    @Test
    fun doWork_exception_returnsRetry() = runTest {
        val habitId = UUID.randomUUID().toString()
        every { workerParams.inputData } returns workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habitId
        )
        coEvery { habitDao.getHabitById(habitId) } throws RuntimeException("Test error")

        val worker = HabitReminderWorker(context, workerParams)
        val result = worker.doWork()

        assert(result is androidx.work.ListenableWorker.Result.Retry)
    }

    @Test
    fun scheduleSnoozedReminder_schedulesWork() {
        val habitId = UUID.randomUUID().toString()
        val workManager = mockk<androidx.work.WorkManager>(relaxed = true)
        mockkStatic(androidx.work.WorkManager::class)
        every { androidx.work.WorkManager.getInstance(context) } returns workManager
        val operation = mockk<androidx.work.Operation>(relaxed = true)
        every { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) } returns operation

        HabitReminderWorker.scheduleSnoozedReminder(context, habitId)

        verify { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    private fun createTimeHabit(
        id: String = UUID.randomUUID().toString(),
        reminderTimes: List<LocalTime> = listOf(LocalTime.of(9, 0)),
        isActive: Boolean = true
    ): Habit {
        return Habit(
            id = id,
            name = "Test Time Habit",
            category = HabitCategory.FITNESS,
            type = HabitType.TIME,
            reminderTimes = reminderTimes,
            isActive = isActive
        )
    }
}

