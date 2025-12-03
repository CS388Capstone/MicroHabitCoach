package com.microhabitcoach.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.ui.MainActivity
import java.util.UUID

/**
 * Central notification manager for the app.
 * Handles creating channels, showing notifications, and preventing duplicates.
 */
class HabitNotificationManager private constructor(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val sentNotifications = mutableSetOf<String>() // Track sent notifications to prevent duplicates
    
    companion object {
        // Notification channels
        const val CHANNEL_STREAK_COUNTDOWN = "streak_countdown"
        const val CHANNEL_INACTIVITY = "inactivity"
        const val CHANNEL_GEOFENCE = "geofence"
        const val CHANNEL_REMINDERS = "reminders"
        
        // Notification IDs
        private const val NOTIFICATION_ID_STREAK_PREFIX = 1000
        private const val NOTIFICATION_ID_INACTIVITY = 2000
        private const val NOTIFICATION_ID_GEOFENCE_PREFIX = 3000
        private const val NOTIFICATION_ID_REMINDER_PREFIX = 4000
        
        @Volatile
        private var INSTANCE: HabitNotificationManager? = null
        
        fun getInstance(context: Context): HabitNotificationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = HabitNotificationManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Creates notification channels for Android O+.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_STREAK_COUNTDOWN,
                    context.getString(R.string.notification_channel_streak),
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_streak_description)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_INACTIVITY,
                    context.getString(R.string.notification_channel_inactivity),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_inactivity_description)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_GEOFENCE,
                    context.getString(R.string.notification_channel_geofence),
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_geofence_description)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.notification_channel_reminders),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_reminders_description)
                    enableVibration(true)
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            channels.forEach { systemNotificationManager.createNotificationChannel(it) }
        }
    }
    
    /**
     * Shows a streak countdown notification.
     * 
     * @param habit The habit with an expiring streak
     * @param hoursRemaining Hours remaining to save the streak
     * @return true if notification was shown, false if duplicate
     */
    fun showStreakCountdownNotification(habit: Habit, hoursRemaining: Int): Boolean {
        val notificationId = NOTIFICATION_ID_STREAK_PREFIX + habit.id.hashCode()
        val notificationKey = "streak_${habit.id}_${hoursRemaining}"
        
        // Prevent duplicate notifications
        if (sentNotifications.contains(notificationKey)) {
            return false
        }
        
        val title = context.getString(R.string.notification_streak_title, habit.name)
        val message = context.getString(R.string.notification_streak_message, hoursRemaining)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habit.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markDoneIntent = createMarkDoneIntent(habit.id, notificationId)
        val snoozeIntent = createSnoozeIntent(habit.id, notificationId, NotificationType.STREAK_COUNTDOWN)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK_COUNTDOWN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_compass, context.getString(R.string.mark_done), markDoneIntent)
            .addAction(android.R.drawable.ic_menu_revert, context.getString(R.string.snooze), snoozeIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
        sentNotifications.add(notificationKey)
        
        return true
    }
    
    /**
     * Shows an inactivity nudge notification.
     * 
     * @return true if notification was shown, false if duplicate
     */
    fun showInactivityNotification(): Boolean {
        val notificationKey = "inactivity_${System.currentTimeMillis() / (2 * 60 * 60 * 1000)}" // Unique per 2-hour window
        
        // Prevent duplicate notifications
        if (sentNotifications.contains(notificationKey)) {
            return false
        }
        
        val title = context.getString(R.string.notification_inactivity_title)
        val message = context.getString(R.string.notification_inactivity_message)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_INACTIVITY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INACTIVITY, notification)
        sentNotifications.add(notificationKey)
        
        return true
    }
    
    /**
     * Shows a geofence-triggered notification.
     * 
     * @param habit The habit triggered by geofence
     * @return true if notification was shown, false if duplicate
     */
    fun showGeofenceNotification(habit: Habit): Boolean {
        val notificationId = NOTIFICATION_ID_GEOFENCE_PREFIX + habit.id.hashCode()
        val notificationKey = "geofence_${habit.id}_${System.currentTimeMillis() / (60 * 60 * 1000)}" // Unique per hour
        
        // Prevent duplicate notifications
        if (sentNotifications.contains(notificationKey)) {
            return false
        }
        
        val title = context.getString(R.string.notification_geofence_title, habit.name)
        val message = context.getString(R.string.notification_geofence_message, habit.name)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habit.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markDoneIntent = createMarkDoneIntent(habit.id, notificationId)
        val snoozeIntent = createSnoozeIntent(habit.id, notificationId, NotificationType.GEOFENCE)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_compass, context.getString(R.string.mark_done), markDoneIntent)
            .addAction(android.R.drawable.ic_menu_revert, context.getString(R.string.snooze), snoozeIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
        sentNotifications.add(notificationKey)
        
        return true
    }
    
    /**
     * Creates a PendingIntent for "Mark Done" action.
     */
    private fun createMarkDoneIntent(habitId: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_DONE
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Creates a PendingIntent for "Snooze" action.
     */
    private fun createSnoozeIntent(
        habitId: String,
        notificationId: Int,
        type: NotificationType
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_TYPE, type.name)
        }
        
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Shows a time-based habit reminder notification.
     * 
     * @param habit The habit to remind about
     * @return true if notification was shown, false if duplicate
     */
    fun showReminderNotification(habit: Habit): Boolean {
        val notificationId = NOTIFICATION_ID_REMINDER_PREFIX + habit.id.hashCode()
        val notificationKey = "reminder_${habit.id}_${System.currentTimeMillis() / (60 * 60 * 1000)}" // Unique per hour
        
        // Prevent duplicate notifications
        if (sentNotifications.contains(notificationKey)) {
            return false
        }
        
        val title = context.getString(R.string.notification_reminder_title, habit.name)
        val message = context.getString(R.string.notification_reminder_message, habit.name)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habit.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markDoneIntent = createMarkDoneIntent(habit.id, notificationId)
        val snoozeIntent = createSnoozeIntent(habit.id, notificationId, NotificationType.REMINDER)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_compass, context.getString(R.string.mark_done), markDoneIntent)
            .addAction(android.R.drawable.ic_menu_revert, context.getString(R.string.snooze), snoozeIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
        sentNotifications.add(notificationKey)
        
        return true
    }
    
    /**
     * Cancels a notification.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Clears the duplicate notification tracking (call periodically to prevent memory issues).
     */
    fun clearNotificationTracking() {
        sentNotifications.clear()
    }
}

enum class NotificationType {
    STREAK_COUNTDOWN,
    INACTIVITY,
    GEOFENCE,
    REMINDER
}

