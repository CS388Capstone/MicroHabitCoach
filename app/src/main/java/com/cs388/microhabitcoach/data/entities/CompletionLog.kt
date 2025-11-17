package com.cs388.microhabitcoach.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents a single completion event for a Habit.
 */
@Entity(
    tableName = "completion_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CompletionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val habitId: Long,

    /** Timestamp in epoch millis when the habit was completed */
    val timestamp: Long = System.currentTimeMillis(),

    /** Whether this completion was recorded automatically (sensors/geofence) */
    val auto: Boolean = false
)
