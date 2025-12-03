package com.microhabitcoach.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.microhabitcoach.activity.ActivityDurationTracker
import com.microhabitcoach.data.model.MotionState
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
     * Checks if user has been inactive (stationary) for 30+ minutes.
     *
     * Uses ActivityDurationTracker, which is updated by ActivityTransitionReceiver /
     * ActivityRecognitionService. If the current motion state is STATIONARY and the
     * tracked duration for \"stationary\" is >= threshold, we trigger an inactivity nudge.
     */
    private suspend fun checkInactivity(): Boolean {
        // Get current motion state (WALKING, RUNNING, STATIONARY, etc.)
        val motionState = ActivityDurationTracker.getCurrentMotionState()

        if (motionState != MotionState.STATIONARY) {
            // User is not stationary; no inactivity nudge needed
            return false
        }

        // Get how long they've been stationary (in minutes)
        val stationaryMinutes = ActivityDurationTracker.getActivityDuration("stationary")

        // Threshold: 30 minutes of being stationary (demo-friendly)
        val thresholdMinutes = 30L

        return stationaryMinutes >= thresholdMinutes
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

