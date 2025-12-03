package com.microhabitcoach.notification

import android.content.Context
import android.util.Log
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles geofence-triggered notifications.
 */
object GeofenceNotificationHandler {
    
    private const val TAG = "GeofenceNotificationHandler"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Called when a geofence is triggered.
     * The habitId parameter is the geofence request ID, which equals the habit ID.
     * 
     * @param context Application context
     * @param habitId The habit ID that triggered the geofence (from geofence request ID)
     */
    fun onGeofenceTriggered(context: Context, habitId: String) {
        scope.launch {
            try {
                val database = DatabaseModule.getDatabase(context)
                val habitDao = database.habitDao()
                
                // Query habit by ID (geofence request ID = habit ID)
                val habit = habitDao.getHabitById(habitId)
                
                if (habit == null) {
                    Log.w(TAG, "Habit $habitId not found - may have been deleted")
                    return@launch
                }
                
                // Check if habit is still active
                if (!habit.isActive) {
                    Log.d(TAG, "Habit $habitId is inactive, skipping notification")
                    return@launch
                }
                
                // Check if habit is location-based
                if (habit.location == null) {
                    Log.w(TAG, "Habit $habitId is not location-based")
                    return@launch
                }
                
                // Show notification
                val notificationManager = HabitNotificationManager.getInstance(context)
                notificationManager.showGeofenceNotification(habit)
                
                Log.d(TAG, "Geofence notification shown for habit: ${habit.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling geofence trigger for habit: $habitId", e)
            }
        }
    }
}

