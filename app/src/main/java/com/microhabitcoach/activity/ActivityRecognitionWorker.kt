package com.microhabitcoach.activity

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker for periodic activity recognition checks.
 * Runs periodically to check for auto-completion opportunities.
 */
class ActivityRecognitionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Initialize tracker if not already initialized
                ActivityDurationTracker.initialize(applicationContext)
                
                // Get current activity
                val currentActivity = ActivityDurationTracker.getCurrentActivity()
                
                if (currentActivity != null) {
                    // Check for auto-completion
                    ActivityRecognitionService.checkAndAutoComplete(applicationContext, currentActivity)
                }
                
                Result.success()
            } catch (e: Exception) {
                // Don't retry on errors (might be permission issues)
                Result.success()
            }
        }
    }
    
    companion object {
        private const val WORK_NAME = "activity_recognition_check"
        
        /**
         * Schedules periodic work to check for auto-completion.
         */
        fun schedulePeriodicCheck(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<ActivityRecognitionWorker>(
                10, java.util.concurrent.TimeUnit.MINUTES
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

