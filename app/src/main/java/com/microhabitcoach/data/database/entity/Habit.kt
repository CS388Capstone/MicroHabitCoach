package com.microhabitcoach.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitCategoryConverter
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.HabitTypeConverter
import com.microhabitcoach.data.model.IntListConverter
import com.microhabitcoach.data.model.LocalTimeListConverter
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.data.model.LocationDataConverter
import java.time.LocalTime

@Entity(tableName = "habits")
@TypeConverters(
    HabitCategoryConverter::class,
    HabitTypeConverter::class,
    LocalTimeListConverter::class,
    LocationDataConverter::class,
    IntListConverter::class
)
data class Habit(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: HabitCategory,
    val type: HabitType,
    
    // For motion-based habits
    val motionType: String? = null, // "walk", "run", "stationary"
    val targetDuration: Int? = null, // Duration in minutes
    
    // For location-based habits
    val location: LocationData? = null,
    val geofenceRadius: Float? = null, // Radius in meters
    
    // For time-based habits
    val reminderTimes: List<LocalTime>? = null,
    val reminderDays: List<Int>? = null, // Days of week (1=Monday, 7=Sunday)
    
    // Common fields
    val streakCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

