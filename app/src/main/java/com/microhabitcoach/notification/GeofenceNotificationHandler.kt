package com.microhabitcoach.notification

import android.content.Context
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles geofence-triggered notifications.
 */
object GeofenceNotificationHandler {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Called when a geofence is triggered.
     * 
     * @param context Application context
     * @param locationId The location identifier that was triggered
     */
    fun onGeofenceTriggered(context: Context, locationId: String) {
        scope.launch {
            val database = DatabaseModule.getDatabase(context)
            val habitDao = database.habitDao()
            
            // Find habits with this location
            val locationHabits = habitDao.getLocationHabits(HabitType.LOCATION)
            
            locationHabits.forEach { habit ->
                // Check if this habit's location matches the triggered location
                if (habit.location != null && matchesLocation(habit.location, locationId)) {
                    val notificationManager = HabitNotificationManager.getInstance(context)
                    notificationManager.showGeofenceNotification(habit)
                }
            }
        }
    }
    
    /**
     * Checks if a habit's location matches the triggered location.
     * This is a simplified check - in production, you'd compare coordinates.
     */
    private fun matchesLocation(
        habitLocation: com.microhabitcoach.data.model.LocationData,
        locationId: String
    ): Boolean {
        // Simplified: compare by address or coordinates
        // In production, you'd use proper distance calculation
        return habitLocation.address?.contains(locationId, ignoreCase = true) == true
    }
}

