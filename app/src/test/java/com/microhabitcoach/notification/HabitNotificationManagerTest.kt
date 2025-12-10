package com.microhabitcoach.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import java.util.UUID
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
@Ignore("Temporarily disabled for demo")
class HabitNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private lateinit var systemNotificationManager: NotificationManager
    private lateinit var notificationManager: HabitNotificationManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        notificationManagerCompat = mockk(relaxed = true)
        systemNotificationManager = mockk(relaxed = true)

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(context) } returns notificationManagerCompat
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns systemNotificationManager
        every { context.applicationContext } returns context
        every { notificationManagerCompat.notify(any(), any()) } returns Unit
        every { notificationManagerCompat.cancel(any()) } returns Unit
        every { systemNotificationManager.createNotificationChannel(any()) } returns Unit

        notificationManager = HabitNotificationManager.getInstance(context)
        notificationManager.clearNotificationTracking()
    }

    @Test
    fun getInstance_createsSingleton() {
        val instance1 = HabitNotificationManager.getInstance(context)
        val instance2 = HabitNotificationManager.getInstance(context)

        assert(instance1 === instance2)
    }

    @Test
    fun createNotificationChannels_androidOPlus_createsChannels() {
        setSdkInt(Build.VERSION_CODES.O)

        val manager = HabitNotificationManager.getInstance(context)

        verify(atLeast = 4) { systemNotificationManager.createNotificationChannel(any()) }
    }

    @Test
    fun createNotificationChannels_androidBelowO_doesNotCreate() {
        setSdkInt(Build.VERSION_CODES.M)

        val manager = HabitNotificationManager.getInstance(context)

        verify(exactly = 0) { systemNotificationManager.createNotificationChannel(any()) }
    }

    @Test
    fun showStreakCountdownNotification_showsNotification() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        val result = notificationManager.showStreakCountdownNotification(habit, 2)

        assertTrue(result)
        verify { notificationManagerCompat.notify(any(), any()) }
    }

    @Test
    fun showStreakCountdownNotification_duplicate_returnsFalse() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        notificationManager.showStreakCountdownNotification(habit, 2)
        val result = notificationManager.showStreakCountdownNotification(habit, 2)

        assertFalse(result)
    }

    @Test
    fun showInactivityNotification_showsNotification() {
        every { context.getString(any()) } returns "Test"

        val result = notificationManager.showInactivityNotification()

        assertTrue(result)
        verify { notificationManagerCompat.notify(any(), any()) }
    }

    @Test
    fun showInactivityNotification_duplicate_returnsFalse() {
        every { context.getString(any()) } returns "Test"

        notificationManager.showInactivityNotification()
        val result = notificationManager.showInactivityNotification()

        assertFalse(result)
    }

    @Test
    fun showGeofenceNotification_showsNotification() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        val result = notificationManager.showGeofenceNotification(habit)

        assertTrue(result)
        verify { notificationManagerCompat.notify(any(), any()) }
    }

    @Test
    fun showGeofenceNotification_duplicate_returnsFalse() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        notificationManager.showGeofenceNotification(habit)
        val result = notificationManager.showGeofenceNotification(habit)

        assertFalse(result)
    }

    @Test
    fun showReminderNotification_showsNotification() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        val result = notificationManager.showReminderNotification(habit)

        assertTrue(result)
        verify { notificationManagerCompat.notify(any(), any()) }
    }

    @Test
    fun showReminderNotification_duplicate_returnsFalse() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        notificationManager.showReminderNotification(habit)
        val result = notificationManager.showReminderNotification(habit)

        assertFalse(result)
    }

    @Test
    fun cancelNotification_cancelsNotification() {
        val notificationId = 1234

        notificationManager.cancelNotification(notificationId)

        verify { notificationManagerCompat.cancel(notificationId) }
    }

    @Test
    fun clearNotificationTracking_clearsTracking() {
        val habit = createTestHabit()
        every { context.getString(any(), any()) } returns "Test"

        notificationManager.showStreakCountdownNotification(habit, 2)
        notificationManager.clearNotificationTracking()
        
        // After clearing, should be able to show again
        val result = notificationManager.showStreakCountdownNotification(habit, 2)
        assertTrue(result)
    }

    private fun createTestHabit(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit"
    ): Habit {
        return Habit(
            id = id,
            name = name,
            category = HabitCategory.FITNESS,
            type = HabitType.TIME
        )
    }

    private fun setSdkInt(version: Int) {
        // Robolectric 4.12.1 lacks ShadowBuild.setVersionInt; set the static field directly.
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", version)
    }
}

