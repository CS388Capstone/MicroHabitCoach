package com.microhabitcoach.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling activity recognition events.
 * Handles both ActivityTransitionResult (Android 10+) and ActivityRecognitionResult (legacy).
 */
class ActivityTransitionReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onReceive(context: Context, intent: Intent) {
        // Handle ActivityTransitionResult (Android 10+)
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            
            result?.transitionEvents?.forEach { event ->
                handleActivityTransition(context, event)
            }
            return
        }
        
        // Handle ActivityRecognitionResult (legacy API for Android < 10)
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val detectedActivity = result?.mostProbableActivity
            
            detectedActivity?.let { activity ->
                handleLegacyActivityDetection(context, activity)
            }
        }
    }
    
    /**
     * Handles legacy activity detection (Android < 10).
     * Uses polling-based detection instead of transitions.
     */
    private fun handleLegacyActivityDetection(context: Context, activity: DetectedActivity) {
        val activityType = activity.type
        val confidence = activity.confidence
        
        // Only process high-confidence detections
        if (confidence < MIN_CONFIDENCE_THRESHOLD) {
            return
        }
        
        scope.launch {
            when (activityType) {
                DetectedActivity.WALKING -> {
                    val currentActivity = ActivityDurationTracker.getCurrentActivity()
                    if (currentActivity != "walk") {
                        // Clear previous activity if different
                        if (currentActivity != null) {
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        ActivityDurationTracker.startActivity("walk")
                        Log.d(TAG, "Detected walking (legacy API)")
                    }
                    // Check for auto-completion periodically
                    ActivityRecognitionService.checkAndAutoComplete(context, "walk")
                }
                DetectedActivity.RUNNING -> {
                    val currentActivity = ActivityDurationTracker.getCurrentActivity()
                    if (currentActivity != "run") {
                        // Clear previous activity if different
                        if (currentActivity != null) {
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        ActivityDurationTracker.startActivity("run")
                        Log.d(TAG, "Detected running (legacy API)")
                    }
                    // Check for auto-completion periodically
                    ActivityRecognitionService.checkAndAutoComplete(context, "run")
                }
                DetectedActivity.STILL -> {
                    val currentActivity = ActivityDurationTracker.getCurrentActivity()
                    if (currentActivity != null && currentActivity != "stationary") {
                        // Check for auto-completion when becoming still
                        ActivityRecognitionService.checkAndAutoComplete(context, currentActivity)
                        ActivityDurationTracker.clearActivity(currentActivity)
                    }
                    ActivityDurationTracker.startActivity("stationary")
                    Log.d(TAG, "Detected stationary (legacy API)")
                }
                else -> {
                    // Other activities - not currently used
                }
            }
        }
    }
    
    private fun handleActivityTransition(context: Context, event: ActivityTransition) {
        val activityType = event.activityType
        val transitionType = event.transitionType
        
        scope.launch {
            when (activityType) {
                DetectedActivity.WALKING -> {
                    if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        // Clear any previous activity before starting new one
                        val currentActivity = ActivityDurationTracker.getCurrentActivity()
                        if (currentActivity != null && currentActivity != "walk") {
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        ActivityDurationTracker.startActivity("walk")
                        Log.d(TAG, "Started walking")
                    } else if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        // Check for auto-completion when user stops walking
                        ActivityRecognitionService.checkAndAutoComplete(context, "walk")
                        ActivityDurationTracker.clearActivity("walk")
                        Log.d(TAG, "Stopped walking")
                    }
                }
                DetectedActivity.RUNNING -> {
                    if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        // Clear any previous activity before starting new one
                        val currentActivity = ActivityDurationTracker.getCurrentActivity()
                        if (currentActivity != null && currentActivity != "run") {
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        ActivityDurationTracker.startActivity("run")
                        Log.d(TAG, "Started running")
                    } else if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        // Check for auto-completion when user stops running
                        ActivityRecognitionService.checkAndAutoComplete(context, "run")
                        ActivityDurationTracker.clearActivity("run")
                        Log.d(TAG, "Stopped running")
                    }
                }
                DetectedActivity.STILL -> {
                    if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        // When user becomes still, check if we should auto-complete any ongoing activity
                        val currentActivity = ActivityDurationTracker.getCurrentActivity()
                        if (currentActivity != null && currentActivity != "stationary") {
                            ActivityRecognitionService.checkAndAutoComplete(context, currentActivity)
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        ActivityDurationTracker.startActivity("stationary")
                        Log.d(TAG, "Became stationary")
                    }
                }
                DetectedActivity.IN_VEHICLE -> {
                    if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        // Clear any ongoing activity tracking when in vehicle
                        val currentActivity = ActivityDurationTracker.getCurrentActivity()
                        if (currentActivity != null && currentActivity != "stationary") {
                            ActivityDurationTracker.clearActivity(currentActivity)
                        }
                        Log.d(TAG, "Entered vehicle")
                    }
                }
                else -> {
                    // Other activities (ON_BICYCLE, etc.) - not currently used
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "ActivityTransitionReceiver"
        private const val MIN_CONFIDENCE_THRESHOLD = 50 // Minimum confidence percentage for legacy API
    }
}

