package com.microhabitcoach.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.microhabitcoach.notification.GeofenceNotificationHandler

/**
 * BroadcastReceiver for handling geofence transition events.
 * Receives GeofencingEvent from GeofencingClient.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent?.hasError() == true) {
            val errorMessage = getGeofenceErrorString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }
        
        val geofenceTransition = geofencingEvent?.geofenceTransition ?: return
        
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                handleGeofenceEnter(context, geofencingEvent.triggeringGeofences)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                // Exit transitions not currently used, but handle gracefully
                Log.d(TAG, "Geofence exit detected (not handled)")
            }
            else -> {
                Log.d(TAG, "Unknown geofence transition: $geofenceTransition")
            }
        }
    }
    
    private fun handleGeofenceEnter(context: Context, triggeringGeofences: List<Geofence>?) {
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            return
        }
        
        // Process each triggered geofence
        triggeringGeofences.forEach { geofence ->
            val habitId = geofence.requestId
            Log.d(TAG, "Geofence entered for habit: $habitId")
            
            // Notify handler - it will check if habit exists and is active
            GeofenceNotificationHandler.onGeofenceTriggered(context, habitId)
        }
    }
    
    private fun getGeofenceErrorString(errorCode: Int): String {
        return when (errorCode) {
            1000 -> "Geofence not available" // GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE
            1001 -> "Too many geofences" // GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES
            1002 -> "Too many pending intents" // GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS
            else -> "Unknown geofence error: $errorCode"
        }
    }
    
    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}

