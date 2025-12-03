package com.microhabitcoach.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for detecting inactivity and sending nudges.
 */
class InactivityDetectionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Simplified inactivity detection
                // In production, you'd track activity transitions over time using ActivityTransition API
                // For now, we'll check if we should send a notification based on a simple heuristic
                val shouldNotify = checkInactivity()
                
                if (shouldNotify) {
                    val notificationManager = HabitNotificationManager.getInstance(applicationContext)
                    notificationManager.showInactivityNotification()
                }
                
                Result.success()
            } catch (e: Exception) {
                // If activity recognition fails, don't retry (might be permission issue)
                Result.success() // Don't retry on permission errors
            }
        }
    }
    
    /**
     * Checks if user has been inactive.
     * This is a simplified implementation - in production, you'd track activity transitions
     * using ActivityTransitionRequest and store the last activity time.
     */
    private suspend fun checkInactivity(): Boolean {
        // TODO: Implement proper activity tracking with ActivityTransition API
        // For now, this is a placeholder
        // In production, you would:
        // 1. Track activity transitions using ActivityTransitionRequest
        // 2. Store last activity time in SharedPreferences or database
        // 3. Check if last activity was STATIONARY and was 2+ hours ago
        return false
    }
    
    companion object {
        private const val WORK_NAME = "inactivity_detection"
        
        /**
         * Schedules periodic work to check for inactivity.
         */
        fun schedulePeriodicCheck(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .build()
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<InactivityDetectionWorker>(
                2, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}

