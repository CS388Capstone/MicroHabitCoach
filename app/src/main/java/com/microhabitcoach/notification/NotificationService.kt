package com.microhabitcoach.notification

import android.content.Context
import com.microhabitcoach.activity.ActivityRecognitionService
import com.microhabitcoach.activity.ActivityRecognitionWorker

/**
 * Service to initialize and manage all notification-related workers.
 */
object NotificationService {
    
    /**
     * Initializes all notification workers.
     * Call this from Application.onCreate() or MainActivity.onCreate().
     */
    fun initialize(context: Context) {
        // Initialize notification channels
        HabitNotificationManager.getInstance(context)
        
        // Schedule periodic workers
        StreakCountdownWorker.schedulePeriodicCheck(context)
        InactivityDetectionWorker.schedulePeriodicCheck(context)
        ActivityRecognitionWorker.schedulePeriodicCheck(context)
        
        // Start activity recognition monitoring
        ActivityRecognitionService.startMonitoring(context)
    }
    
    /**
     * Cancels all notification workers.
     */
    fun cancelAll(context: Context) {
        androidx.work.WorkManager.getInstance(context).cancelAllWork()
    }
}

