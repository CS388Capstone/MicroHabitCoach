package com.microhabitcoach.notification

import android.content.Context

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
    }
    
    /**
     * Cancels all notification workers.
     */
    fun cancelAll(context: Context) {
        androidx.work.WorkManager.getInstance(context).cancelAllWork()
    }
}

