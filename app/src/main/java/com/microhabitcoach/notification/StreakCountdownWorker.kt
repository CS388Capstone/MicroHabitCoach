package com.microhabitcoach.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.repository.DefaultHabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * WorkManager worker for checking and sending streak countdown notifications.
 */
class StreakCountdownWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val repository = DefaultHabitRepository(applicationContext)
                val database = DatabaseModule.getDatabase(applicationContext)
                val habitDao = database.habitDao()
                
                // Get all active habits (using suspend function)
                val habits = habitDao.getAllHabitsSync()
                
                habits.forEach { habit ->
                    if (habit.isActive && habit.streakCount > 0) {
                        checkAndNotifyStreak(habit, repository)
                    }
                }
                
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
    
    private suspend fun checkAndNotifyStreak(
        habit: com.microhabitcoach.data.database.entity.Habit,
        repository: DefaultHabitRepository
    ) {
        // Check if habit is already completed today
        if (repository.isHabitCompletedToday(habit.id)) {
            return
        }
        
        // Calculate time remaining until end of day
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfDay = calendar.timeInMillis
        val hoursRemaining = ((endOfDay - now) / (60 * 60 * 1000)).toInt()
        
        // Send notification if less than 2 hours remaining
        if (hoursRemaining < 2 && hoursRemaining >= 0) {
                val notificationManager = HabitNotificationManager.getInstance(applicationContext)
            notificationManager.showStreakCountdownNotification(habit, hoursRemaining)
        }
    }
    
    companion object {
        private const val WORK_NAME_PREFIX = "streak_countdown_"
        private const val WORK_NAME_SNOOZE_PREFIX = "streak_snooze_"
        
        /**
         * Schedules periodic work to check for streak countdowns.
         */
        fun schedulePeriodicCheck(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .build()
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<StreakCountdownWorker>(
                1, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "${WORK_NAME_PREFIX}periodic",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
        
        /**
         * Schedules a snoozed notification for 30 minutes later.
         */
        fun scheduleSnoozedNotification(context: Context, habitId: String) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .build()
            
            val inputData = workDataOf("habit_id" to habitId)
            
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<StreakCountdownWorker>()
                .setConstraints(constraints)
                .setInitialDelay(30, java.util.concurrent.TimeUnit.MINUTES)
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

