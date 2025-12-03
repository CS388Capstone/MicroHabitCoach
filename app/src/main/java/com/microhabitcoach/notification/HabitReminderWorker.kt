package com.microhabitcoach.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.repository.DefaultHabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for sending time-based habit reminder notifications.
 */
class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val habitId = inputData.getString(KEY_HABIT_ID) ?: return@withContext Result.failure()
                val originalReminderTimeHour = inputData.getInt(KEY_REMINDER_TIME_HOUR, -1)
                val originalReminderTimeMinute = inputData.getInt(KEY_REMINDER_TIME_MINUTE, -1)
                
                // Check if this is a snooze (no reminder time provided)
                val isSnooze = originalReminderTimeHour == -1 || originalReminderTimeMinute == -1
                
                val repository = DefaultHabitRepository(applicationContext)
                val database = DatabaseModule.getDatabase(applicationContext)
                val habitDao = database.habitDao()
                
                // Load habit from database
                val habit = habitDao.getHabitById(habitId) ?: return@withContext Result.success()
                
                // Check if habit is still active and has reminder times
                if (!habit.isActive || habit.reminderTimes == null || habit.reminderTimes.isEmpty()) {
                    return@withContext Result.success()
                }
                
                // If reminder time not provided (e.g., from snooze), use first reminder time from habit
                var reminderTimeHour = originalReminderTimeHour
                var reminderTimeMinute = originalReminderTimeMinute
                if (isSnooze) {
                    val firstReminderTime = habit.reminderTimes.first()
                    reminderTimeHour = firstReminderTime.hour
                    reminderTimeMinute = firstReminderTime.minute
                }
                
                // Check if habit is already completed today
                if (repository.isHabitCompletedToday(habitId)) {
                    return@withContext Result.success()
                }
                
                // Show notification
                val notificationManager = HabitNotificationManager.getInstance(applicationContext)
                notificationManager.showReminderNotification(habit)
                
                // Schedule next occurrence if this is a recurring reminder (not a snooze)
                if (!isSnooze) {
                    ReminderScheduler.scheduleNextReminder(applicationContext, habit, reminderTimeHour, reminderTimeMinute)
                }
                
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
    
    companion object {
        private const val WORK_NAME_PREFIX = "habit_reminder_"
        private const val WORK_NAME_SNOOZE_PREFIX = "habit_reminder_snooze_"
        
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_REMINDER_TIME_HOUR = "reminder_time_hour"
        const val KEY_REMINDER_TIME_MINUTE = "reminder_time_minute"
        
        /**
         * Schedules a snoozed reminder notification for 15 minutes later.
         */
        fun scheduleSnoozedReminder(context: Context, habitId: String) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .build()
            
            val inputData = workDataOf(KEY_HABIT_ID to habitId)
            
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setConstraints(constraints)
                .setInitialDelay(15, java.util.concurrent.TimeUnit.MINUTES)
                .setInputData(inputData)
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME_SNOOZE_PREFIX}$habitId",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}

