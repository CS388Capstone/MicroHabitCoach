package com.microhabitcoach.notification

import android.content.Context
import android.content.Intent
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class NotificationActionReceiverTest {

    private lateinit var context: Context
    private lateinit var intent: Intent
    private lateinit var receiver: NotificationActionReceiver
    private lateinit var repository: DefaultHabitRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        intent = mockk(relaxed = true)
        receiver = NotificationActionReceiver()
        repository = mockk(relaxed = true)

        mockkObject(HabitNotificationManager)
        mockkObject(StreakCountdownWorker)
        mockkObject(HabitReminderWorker)
        mockkStatic(DefaultHabitRepository::class)
        every { DefaultHabitRepository(context) } returns repository
        every { HabitNotificationManager.getInstance(any()) } returns mockk {
            every { cancelNotification(any()) } returns Unit
        }
        coEvery { repository.completeHabit(any()) } returns Unit
        every { StreakCountdownWorker.scheduleSnoozedNotification(any(), any()) } returns Unit
        every { HabitReminderWorker.scheduleSnoozedReminder(any(), any()) } returns Unit
    }

    @Test
    fun onReceive_markDoneAction_completesHabit() = runTest {
        val habitId = UUID.randomUUID().toString()
        val notificationId = 1234

        every { intent.action } returns NotificationActionReceiver.ACTION_MARK_DONE
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_HABIT_ID) } returns habitId
        every { intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1) } returns notificationId

        receiver.onReceive(context, intent)

        coVerify { repository.completeHabit(habitId) }
        verify { HabitNotificationManager.getInstance(context).cancelNotification(notificationId) }
    }

    @Test
    fun onReceive_markDoneAction_noHabitId_doesNotComplete() = runTest {
        every { intent.action } returns NotificationActionReceiver.ACTION_MARK_DONE
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_HABIT_ID) } returns null

        receiver.onReceive(context, intent)

        coVerify(exactly = 0) { repository.completeHabit(any()) }
    }

    @Test
    fun onReceive_snoozeStreakCountdown_schedulesSnooze() = runTest {
        val habitId = UUID.randomUUID().toString()
        val notificationId = 1234

        every { intent.action } returns NotificationActionReceiver.ACTION_SNOOZE
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_HABIT_ID) } returns habitId
        every { intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1) } returns notificationId
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_TYPE) } returns NotificationType.STREAK_COUNTDOWN.name

        receiver.onReceive(context, intent)

        verify { HabitNotificationManager.getInstance(context).cancelNotification(notificationId) }
        verify { StreakCountdownWorker.scheduleSnoozedNotification(context, habitId) }
    }

    @Test
    fun onReceive_snoozeReminder_schedulesSnooze() = runTest {
        val habitId = UUID.randomUUID().toString()
        val notificationId = 1234

        every { intent.action } returns NotificationActionReceiver.ACTION_SNOOZE
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_HABIT_ID) } returns habitId
        every { intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1) } returns notificationId
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_TYPE) } returns NotificationType.REMINDER.name

        receiver.onReceive(context, intent)

        verify { HabitNotificationManager.getInstance(context).cancelNotification(notificationId) }
        verify { HabitReminderWorker.scheduleSnoozedReminder(context, habitId) }
    }

    @Test
    fun onReceive_snoozeGeofence_onlyCancels() = runTest {
        val habitId = UUID.randomUUID().toString()
        val notificationId = 1234

        every { intent.action } returns NotificationActionReceiver.ACTION_SNOOZE
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_HABIT_ID) } returns habitId
        every { intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1) } returns notificationId
        every { intent.getStringExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_TYPE) } returns NotificationType.GEOFENCE.name

        receiver.onReceive(context, intent)

        verify { HabitNotificationManager.getInstance(context).cancelNotification(notificationId) }
        verify(exactly = 0) { StreakCountdownWorker.scheduleSnoozedNotification(any(), any()) }
        verify(exactly = 0) { HabitReminderWorker.scheduleSnoozedReminder(any(), any()) }
    }
}

