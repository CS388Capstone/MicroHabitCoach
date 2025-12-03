package com.microhabitcoach.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.repository.DefaultHabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver for handling notification actions (Mark Done, Snooze).
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_MARK_DONE = "com.microhabitcoach.action.MARK_DONE"
        const val ACTION_SNOOZE = "com.microhabitcoach.action.SNOOZE"
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_DONE -> {
                val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                
                // Mark habit as done
                scope.launch {
                    val repository = DefaultHabitRepository(context)
                    repository.completeHabit(habitId)
                }
                
                // Cancel the notification
                if (notificationId != -1) {
                    HabitNotificationManager.getInstance(context).cancelNotification(notificationId)
                }
            }
            
            ACTION_SNOOZE -> {
                val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE)
                
                // Cancel the current notification
                if (notificationId != -1) {
                    HabitNotificationManager.getInstance(context).cancelNotification(notificationId)
                }
                
                // Schedule a new notification
                scope.launch {
                    when (type) {
                        NotificationType.STREAK_COUNTDOWN.name -> {
                            StreakCountdownWorker.scheduleSnoozedNotification(context, habitId)
                        }
                        NotificationType.REMINDER.name -> {
                            HabitReminderWorker.scheduleSnoozedReminder(context, habitId)
                        }
                        NotificationType.GEOFENCE.name -> {
                            // For geofence, we don't snooze - just cancel
                            // The geofence will trigger again if user re-enters
                        }
                    }
                }
            }
        }
    }
}

