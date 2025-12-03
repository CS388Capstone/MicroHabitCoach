package com.microhabitcoach.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.DefaultHabitRepository
import com.microhabitcoach.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service manager for Activity Recognition.
 * Handles starting/stopping activity monitoring and auto-completion logic.
 */
object ActivityRecognitionService {
    
    private const val TAG = "ActivityRecognitionService"
    private var activityRecognitionClient: ActivityRecognitionClient? = null
    private var pendingIntent: PendingIntent? = null
    private var isMonitoring = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Starts monitoring activity transitions.
     * Uses ActivityTransitionRequest API (Android 10+) for battery efficiency.
     * Falls back to requestActivityUpdates for older Android versions.
     */
    fun startMonitoring(context: Context) {
        if (!PermissionHelper.hasActivityRecognitionPermission(context)) {
            Log.w(TAG, "Activity recognition permission not granted")
            return
        }
        
        // Don't start if already monitoring
        if (isMonitoring) {
            Log.d(TAG, "Activity recognition already monitoring")
            return
        }
        
        // Initialize ActivityDurationTracker
        ActivityDurationTracker.initialize(context.applicationContext)
        
        // Initialize client
        activityRecognitionClient = ActivityRecognition.getClient(context.applicationContext)
        
        // Create pending intent for receiver
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        
        // Create activity transition request
        val transitions = mutableListOf<ActivityTransition>()
        
        // Monitor walking
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        
        // Monitor running
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        
        // Monitor still/stationary
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        
        val request = ActivityTransitionRequest(transitions)
        
        // Request activity transitions
        val client = activityRecognitionClient ?: return
        val pendingIntent = this.pendingIntent ?: return
        
        // Check if ActivityTransitionRequest is available (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            client.requestActivityTransitionUpdates(request, pendingIntent)
                .addOnSuccessListener {
                    isMonitoring = true
                    Log.d(TAG, "Activity transition monitoring started")
                }
                .addOnFailureListener { e ->
                    isMonitoring = false
                    Log.e(TAG, "Failed to start activity transition monitoring", e)
                    // Fallback to older API for older devices or if transition API fails
                    startMonitoringLegacy(context, client, pendingIntent)
                }
        } else {
            // Use legacy API for Android < 10
            startMonitoringLegacy(context, client, pendingIntent)
        }
    }
    
    /**
     * Legacy monitoring using requestActivityUpdates (for Android < 10).
     */
    private fun startMonitoringLegacy(
        context: Context,
        client: ActivityRecognitionClient,
        pendingIntent: PendingIntent
    ) {
        client.requestActivityUpdates(10000L, pendingIntent) // 10 second intervals
            .addOnSuccessListener {
                isMonitoring = true
                Log.d(TAG, "Activity recognition monitoring started (legacy API)")
            }
            .addOnFailureListener { e ->
                isMonitoring = false
                Log.e(TAG, "Failed to start activity recognition monitoring", e)
            }
    }
    
    /**
     * Stops monitoring activity transitions.
     */
    fun stopMonitoring(context: Context) {
        val client = activityRecognitionClient ?: return
        val pendingIntent = this.pendingIntent ?: return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            client.removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener {
                    isMonitoring = false
                    Log.d(TAG, "Activity transition monitoring stopped")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to stop activity transition monitoring", e)
                }
        } else {
            // Use legacy API for Android < 10
            client.removeActivityUpdates(pendingIntent)
                .addOnSuccessListener {
                    isMonitoring = false
                    Log.d(TAG, "Activity recognition monitoring stopped (legacy API)")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to stop activity recognition monitoring", e)
                }
        }
        
        ActivityDurationTracker.clearAll()
    }
    
    /**
     * Checks if any motion-based habits should be auto-completed.
     * 
     * @param context Application context
     * @param motionType The type of motion (e.g., "walk", "run")
     */
    suspend fun checkAndAutoComplete(context: Context, motionType: String) {
        try {
            // Ensure tracker is initialized
            ActivityDurationTracker.initialize(context.applicationContext)
            
            val repository = DefaultHabitRepository(context)
            val database = DatabaseModule.getDatabase(context)
            val habitDao = database.habitDao()
            
            // Normalize motion type for case-insensitive matching
            val normalizedMotionType = motionType.lowercase()
            
            // Get all active motion-based habits
            // Note: We get all motion habits and filter by normalized motion type in memory
            // since the database might store "Walk", "walk", "WALK", etc.
            // For MVP, this is acceptable as users typically have few habits.
            val allMotionHabits = habitDao.getAllHabitsSync()
                .filter { it.type == HabitType.MOTION && it.isActive && it.motionType != null }
            
            // Filter by matching motion type (case-insensitive)
            val matchingHabits = allMotionHabits.filter { 
                it.motionType?.lowercase() == normalizedMotionType 
            }
            
            if (matchingHabits.isEmpty()) {
                return
            }
            
            // Get current activity duration
            val durationMinutes = ActivityDurationTracker.getActivityDuration(motionType)
            
            if (durationMinutes <= 0) {
                return
            }
            
            // Check each habit
            matchingHabits.forEach { habit ->
                // Double-check habit is still active (might have been deactivated)
                if (!habit.isActive) {
                    return@forEach
                }
                
                // Check if habit has target duration
                val targetDuration = habit.targetDuration ?: return@forEach
                
                // Check if duration threshold is met
                if (durationMinutes >= targetDuration) {
                    // Check if not already completed today
                    if (!repository.isHabitCompletedToday(habit.id)) {
                        // Auto-complete the habit
                        repository.autoCompleteMotionHabit(habit.id)
                        Log.d(TAG, "Auto-completed habit: ${habit.name} (${durationMinutes} min of $motionType, target: ${targetDuration} min)")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for auto-completion", e)
        }
    }
}

