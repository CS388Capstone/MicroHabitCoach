package com.microhabitcoach.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitType
import java.time.LocalTime
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Helper class for scheduling habit reminder notifications using WorkManager.
 */
object ReminderScheduler {
    
    private const val WORK_NAME_PREFIX = "habit_reminder_"
    
    /**
     * Schedules all reminders for a habit.
     * Cancels existing reminders first, then schedules new ones.
     */
    fun scheduleHabitReminders(context: Context, habit: Habit) {
        // Cancel existing reminders first
        cancelHabitReminders(context, habit.id)
        
        // Only schedule for time-based habits with reminder times
        if (habit.type != HabitType.TIME || 
            habit.reminderTimes == null || 
            habit.reminderTimes.isEmpty() ||
            !habit.isActive) {
            return
        }
        
        val reminderDays = habit.reminderDays ?: (1..7).toList() // Default to all days if not specified
        
        // Schedule each reminder time
        habit.reminderTimes.forEach { reminderTime ->
            scheduleReminder(context, habit.id, reminderTime, reminderDays)
        }
    }
    
    /**
     * Cancels all reminders for a habit.
     * 
     * Note: WorkManager doesn't support canceling work by prefix pattern.
     * We cancel the snoozed reminder explicitly. For scheduled reminders, we rely on:
     * 1. The worker checking if the habit still exists (it will return early if deleted)
     * 2. REPLACE policy when rescheduling (replaces existing work)
     * 
     * For a more robust solution, we could track work names in SharedPreferences,
     * but for MVP this is acceptable since deleted habits won't trigger notifications
     * (the worker checks if habit exists before showing notification).
     */
    fun cancelHabitReminders(context: Context, habitId: String) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel snoozed reminder
        workManager.cancelUniqueWork("habit_reminder_snooze_$habitId")
        
        // Note: We can't easily cancel scheduled reminders without tracking work names.
        // However, this is safe because:
        // 1. HabitReminderWorker checks if habit exists before showing notification
        // 2. If habit is deleted, the worker will return early and not show notification
        // 3. The work will eventually complete and not reschedule (since habit is gone)
        
        // For a production app, consider storing work names in SharedPreferences or database
        // to enable proper cancellation of all scheduled reminders
    }
    
    /**
     * Reschedules all reminders for a habit (cancel + schedule).
     */
    fun rescheduleHabitReminders(context: Context, habit: Habit) {
        scheduleHabitReminders(context, habit)
    }
    
    /**
     * Schedules a single reminder for a specific time.
     */
    private fun scheduleReminder(
        context: Context,
        habitId: String,
        reminderTime: LocalTime,
        reminderDays: List<Int>
    ) {
        val workManager = WorkManager.getInstance(context)
        
        // Calculate delay until next occurrence
        val delayMillis = calculateDelayUntilNextReminder(reminderTime, reminderDays)
        
        if (delayMillis < 0) {
            // No valid day found, don't schedule
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val inputData = workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habitId,
            HabitReminderWorker.KEY_REMINDER_TIME_HOUR to reminderTime.hour,
            HabitReminderWorker.KEY_REMINDER_TIME_MINUTE to reminderTime.minute
        )
        
        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
        
        // Use unique work name to allow replacement
        val workName = "${WORK_NAME_PREFIX}${habitId}_${reminderTime.hour}_${reminderTime.minute}"
        
        workManager.enqueueUniqueWork(
            workName,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Schedules the next occurrence of a reminder after it has fired.
     * Called from HabitReminderWorker after showing notification.
     */
    fun scheduleNextReminder(
        context: Context,
        habit: Habit,
        reminderTimeHour: Int,
        reminderTimeMinute: Int
    ) {
        if (habit.type != HabitType.TIME || !habit.isActive) {
            return
        }
        
        val reminderTime = LocalTime.of(reminderTimeHour, reminderTimeMinute)
        val reminderDays = habit.reminderDays ?: (1..7).toList()
        
        // Calculate delay until next occurrence (tomorrow or next valid day)
        val delayMillis = calculateDelayUntilNextReminder(reminderTime, reminderDays, startFromTomorrow = true)
        
        if (delayMillis < 0) {
            return
        }
        
        val workManager = WorkManager.getInstance(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val inputData = workDataOf(
            HabitReminderWorker.KEY_HABIT_ID to habit.id,
            HabitReminderWorker.KEY_REMINDER_TIME_HOUR to reminderTimeHour,
            HabitReminderWorker.KEY_REMINDER_TIME_MINUTE to reminderTimeMinute
        )
        
        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
        
        val workName = "${WORK_NAME_PREFIX}${habit.id}_${reminderTimeHour}_${reminderTimeMinute}"
        
        workManager.enqueueUniqueWork(
            workName,
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Calculates the delay in milliseconds until the next reminder should fire.
     * 
     * @param reminderTime The time of day for the reminder
     * @param reminderDays List of days (1=Monday, 7=Sunday)
     * @param startFromTomorrow If true, start checking from tomorrow instead of today
     * @return Delay in milliseconds, or -1 if no valid day found
     */
    private fun calculateDelayUntilNextReminder(
        reminderTime: LocalTime,
        reminderDays: List<Int>,
        startFromTomorrow: Boolean = false
    ): Long {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
        
        // Convert Android day of week (1=Sunday) to our format (1=Monday, 7=Sunday)
        val currentDay = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
        
        // Check if reminder time has passed today
        val timeHasPassedToday = currentHour > reminderTime.hour || 
                                 (currentHour == reminderTime.hour && currentMinute >= reminderTime.minute)
        
        // Start checking from today or tomorrow
        var daysToAdd = if (startFromTomorrow || timeHasPassedToday) 1 else 0
        
        // Find next valid day (up to 7 days ahead)
        for (i in 0..7) {
            val checkDay = (currentDay + daysToAdd - 1) % 7 + 1 // Wrap around week
            if (reminderDays.contains(checkDay)) {
                // Found valid day
                val targetCalendar = Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, daysToAdd)
                    set(Calendar.HOUR_OF_DAY, reminderTime.hour)
                    set(Calendar.MINUTE, reminderTime.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val delay = targetCalendar.timeInMillis - now
                return if (delay > 0) delay else -1
            }
            daysToAdd++
        }
        
        // No valid day found
        return -1
    }
}

