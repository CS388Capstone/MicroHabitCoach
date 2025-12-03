package com.microhabitcoach.activity

import android.content.Context
import android.content.SharedPreferences
import com.microhabitcoach.data.model.MotionState

/**
 * Tracks activity durations for motion-based habits.
 * Persists state to SharedPreferences to survive app restarts.
 */
object ActivityDurationTracker {
    
    private const val PREFS_NAME = "activity_duration_tracker"
    private const val KEY_PREFIX_START_TIME = "activity_start_"
    private const val KEY_CURRENT_ACTIVITY = "current_activity"
    
    private var sharedPreferences: SharedPreferences? = null
    
    /**
     * Initialize the tracker with application context.
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Starts tracking an activity.
     * 
     * @param motionType The type of motion (e.g., "walk", "run", "stationary")
     * @param timestamp The start timestamp in milliseconds
     */
    fun startActivity(motionType: String, timestamp: Long = System.currentTimeMillis()) {
        val prefs = sharedPreferences ?: return
        prefs.edit()
            .putLong("${KEY_PREFIX_START_TIME}$motionType", timestamp)
            .putString(KEY_CURRENT_ACTIVITY, motionType)
            .apply()
    }
    
    /**
     * Gets the duration of an ongoing activity in minutes.
     * 
     * @param motionType The type of motion
     * @param currentTimestamp Current timestamp (defaults to System.currentTimeMillis())
     * @return Duration in minutes, or 0 if activity not started or start time is invalid
     */
    fun getActivityDuration(motionType: String, currentTimestamp: Long = System.currentTimeMillis()): Long {
        val prefs = sharedPreferences ?: return 0
        val startTime = prefs.getLong("${KEY_PREFIX_START_TIME}$motionType", 0)
        if (startTime == 0L) return 0
        
        // Check if start time is in the future (device time changed) or too old (more than 24 hours)
        if (startTime > currentTimestamp) {
            // Device time changed backwards, clear invalid start time
            clearActivity(motionType)
            return 0
        }
        
        val durationMillis = currentTimestamp - startTime
        val maxDurationMillis = 24 * 60 * 60 * 1000L // 24 hours max
        
        if (durationMillis > maxDurationMillis) {
            // Start time is too old (likely from previous day), clear it
            clearActivity(motionType)
            return 0
        }
        
        return durationMillis / (60 * 1000) // Convert to minutes
    }
    
    /**
     * Clears tracking for an activity.
     * 
     * @param motionType The type of motion to clear
     */
    fun clearActivity(motionType: String) {
        val prefs = sharedPreferences ?: return
        val editor = prefs.edit()
        editor.remove("${KEY_PREFIX_START_TIME}$motionType")
        
        // Clear current activity if it matches
        val currentActivity = prefs.getString(KEY_CURRENT_ACTIVITY, null)
        if (currentActivity == motionType) {
            editor.remove(KEY_CURRENT_ACTIVITY)
        }
        
        editor.apply()
    }
    
    /**
     * Gets the current activity type being tracked.
     * 
     * @return Motion type string (e.g., "walk", "run"), or null if no activity
     */
    fun getCurrentActivity(): String? {
        val prefs = sharedPreferences ?: return null
        return prefs.getString(KEY_CURRENT_ACTIVITY, null)
    }
    
    /**
     * Gets the current motion state.
     * 
     * @return MotionState enum, or MotionState.UNKNOWN if no activity
     */
    fun getCurrentMotionState(): MotionState {
        val currentActivity = getCurrentActivity() ?: return MotionState.UNKNOWN
        return when (currentActivity.lowercase()) {
            "walk" -> MotionState.WALKING
            "run", "running" -> MotionState.RUNNING
            "stationary", "still" -> MotionState.STATIONARY
            "vehicle", "in_vehicle" -> MotionState.IN_VEHICLE
            else -> MotionState.UNKNOWN
        }
    }
    
    /**
     * Clears all tracked activities.
     */
    fun clearAll() {
        val prefs = sharedPreferences ?: return
        prefs.edit().clear().apply()
    }
}

