package com.microhabitcoach.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service manager for Geofencing.
 * Handles creating, removing, and updating geofences for location-based habits.
 */
object GeofenceService {
    
    private const val TAG = "GeofenceService"
    private const val MAX_GEOFENCES = 100 // Android limit
    private const val MIN_RADIUS_METERS = 10f
    private const val MAX_RADIUS_METERS = 10000f
    
    private var geofencingClient: GeofencingClient? = null
    private var pendingIntent: PendingIntent? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Initializes the geofencing client.
     */
    private fun initializeClient(context: Context) {
        if (geofencingClient == null) {
            geofencingClient = LocationServices.getGeofencingClient(context.applicationContext)
        }
    }
    
    /**
     * Creates a PendingIntent for geofence transitions.
     */
    private fun createPendingIntent(context: Context): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent!!
        }
        
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
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
        return pendingIntent!!
    }
    
    /**
     * Adds a geofence for a location-based habit.
     * 
     * @param context Application context
     * @param habit The habit to create a geofence for
     */
    fun addGeofence(context: Context, habit: Habit) {
        if (!PermissionHelper.hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted, cannot add geofence")
            return
        }
        
        if (habit.type != HabitType.LOCATION) {
            Log.w(TAG, "Habit ${habit.id} is not location-based")
            return
        }
        
        val location = habit.location ?: run {
            Log.w(TAG, "Habit ${habit.id} has no location data")
            return
        }
        
        val radius = habit.geofenceRadius ?: run {
            Log.w(TAG, "Habit ${habit.id} has no geofence radius")
            return
        }
        
        // Validate radius
        if (radius < MIN_RADIUS_METERS || radius > MAX_RADIUS_METERS) {
            Log.e(TAG, "Invalid radius $radius for habit ${habit.id}. Must be between $MIN_RADIUS_METERS and $MAX_RADIUS_METERS meters")
            return
        }
        
        // Validate coordinates
        if (location.latitude < -90 || location.latitude > 90 ||
            location.longitude < -180 || location.longitude > 180) {
            Log.e(TAG, "Invalid coordinates for habit ${habit.id}")
            return
        }
        
        initializeClient(context)
        val client = geofencingClient ?: return
        
        val geofence = Geofence.Builder()
            .setRequestId(habit.id) // Use habit ID as request ID
            .setCircularRegion(
                location.latitude,
                location.longitude,
                radius
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
        
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        
        val pendingIntent = createPendingIntent(context)
        
        client.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added successfully for habit: ${habit.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add geofence for habit: ${habit.id}", e)
                handleGeofenceError(e)
            }
    }
    
    /**
     * Removes a geofence for a habit.
     * 
     * @param context Application context
     * @param habitId The ID of the habit whose geofence should be removed
     */
    fun removeGeofence(context: Context, habitId: String) {
        initializeClient(context)
        val client = geofencingClient ?: return
        
        val pendingIntent = createPendingIntent(context)
        
        // Remove geofence by request ID (habit ID)
        client.removeGeofences(listOf(habitId))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence removed successfully for habit: $habitId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove geofence for habit: $habitId", e)
            }
    }
    
    /**
     * Updates all geofences based on current location habits in database.
     * Removes all existing geofences and recreates them.
     * 
     * @param context Application context
     */
    fun updateGeofences(context: Context) {
        if (!PermissionHelper.hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted, cannot update geofences")
            return
        }
        
        scope.launch {
            try {
                val database = DatabaseModule.getDatabase(context)
                val habitDao = database.habitDao()
                
                // Get all active location-based habits
                val locationHabits = habitDao.getLocationHabits(HabitType.LOCATION)
                    .filter { it.isActive && it.location != null && it.geofenceRadius != null }
                
                // Check geofence limit
                if (locationHabits.size > MAX_GEOFENCES) {
                    Log.w(TAG, "Too many location habits (${locationHabits.size}). Android limit is $MAX_GEOFENCES. Only first $MAX_GEOFENCES will have geofences.")
                }
                
                // Remove all existing geofences first
                removeAllGeofences(context)
                
                // Add geofences for active location habits (up to limit)
                locationHabits.take(MAX_GEOFENCES).forEach { habit ->
                    addGeofence(context, habit)
                }
                
                Log.d(TAG, "Updated ${locationHabits.size.coerceAtMost(MAX_GEOFENCES)} geofences")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating geofences", e)
            }
        }
    }
    
    /**
     * Removes all geofences.
     * 
     * @param context Application context
     */
    fun removeAllGeofences(context: Context) {
        initializeClient(context)
        val client = geofencingClient ?: return
        
        val pendingIntent = createPendingIntent(context)
        
        client.removeGeofences(pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "All geofences removed successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to remove all geofences", e)
            }
    }
    
    /**
     * Handles geofence errors and logs appropriate messages.
     */
    private fun handleGeofenceError(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("GEOFENCE_NOT_AVAILABLE", ignoreCase = true) == true -> {
                "Location services are not available"
            }
            exception.message?.contains("GEOFENCE_TOO_MANY_GEOFENCES", ignoreCase = true) == true -> {
                "Too many geofences (limit: $MAX_GEOFENCES)"
            }
            exception.message?.contains("GEOFENCE_TOO_MANY_PENDING_INTENTS", ignoreCase = true) == true -> {
                "Too many pending intents"
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                "Location permission denied"
            }
            else -> {
                "Unknown geofence error: ${exception.message}"
            }
        }
        Log.e(TAG, errorMessage)
    }
}

