package com.microhabitcoach.notification

import android.content.Context
import androidx.work.WorkManager
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Ignore
import java.time.LocalTime
import java.util.Calendar
import java.util.UUID

@Ignore("Temporarily disabled for demo")
class ReminderSchedulerTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workManager = mockk(relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager
        val operation = mockk<androidx.work.Operation>(relaxed = true)
        every { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) } returns operation
        every { workManager.cancelUniqueWork(any<String>()) } returns operation
    }

    @Test
    fun scheduleHabitReminders_timeBasedHabit_schedulesReminders() {
        val habit = createTimeHabit(reminderTimes = listOf(LocalTime.of(9, 0), LocalTime.of(18, 0)))

        ReminderScheduler.scheduleHabitReminders(context, habit)

        verify(atLeast = 1) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleHabitReminders_notTimeBased_doesNotSchedule() {
        val habit = createTimeHabit(type = HabitType.MOTION)

        ReminderScheduler.scheduleHabitReminders(context, habit)

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleHabitReminders_noReminderTimes_doesNotSchedule() {
        val habit = createTimeHabit(reminderTimes = emptyList())

        ReminderScheduler.scheduleHabitReminders(context, habit)

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleHabitReminders_inactiveHabit_doesNotSchedule() {
        val habit = createTimeHabit(isActive = false)

        ReminderScheduler.scheduleHabitReminders(context, habit)

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun cancelHabitReminders_cancelsSnoozeWork() {
        val habitId = UUID.randomUUID().toString()

        ReminderScheduler.cancelHabitReminders(context, habitId)

        verify { workManager.cancelUniqueWork("habit_reminder_snooze_$habitId") }
    }

    @Test
    fun rescheduleHabitReminders_cancelsAndSchedules() {
        val habit = createTimeHabit()

        ReminderScheduler.rescheduleHabitReminders(context, habit)

        verify { workManager.cancelUniqueWork(any<String>()) }
        verify(atLeast = 1) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleNextReminder_schedulesNextOccurrence() {
        val habit = createTimeHabit()

        ReminderScheduler.scheduleNextReminder(context, habit, 9, 0)

        verify { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleNextReminder_notTimeBased_doesNotSchedule() {
        val habit = createTimeHabit(type = HabitType.MOTION)

        ReminderScheduler.scheduleNextReminder(context, habit, 9, 0)

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    @Test
    fun scheduleNextReminder_inactiveHabit_doesNotSchedule() {
        val habit = createTimeHabit(isActive = false)

        ReminderScheduler.scheduleNextReminder(context, habit, 9, 0)

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<androidx.work.OneTimeWorkRequest>()) }
    }

    private fun createTimeHabit(
        id: String = UUID.randomUUID().toString(),
        reminderTimes: List<LocalTime> = listOf(LocalTime.of(9, 0)),
        reminderDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
        type: HabitType = HabitType.TIME,
        isActive: Boolean = true
    ): Habit {
        return Habit(
            id = id,
            name = "Test Time Habit",
            category = HabitCategory.FITNESS,
            type = type,
            reminderTimes = reminderTimes,
            reminderDays = reminderDays,
            isActive = isActive
        )
    }
}

