package com.cs388.microhabitcoach.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a habit configuration.
 * Keep fields minimal and serializable-friendly for Room.
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    /** Type as string for simplicity: TIME / MOVEMENT / LOCATION */
    val type: String,

    /** Generic integer target (minutes, count, pages) — nullable for non-applicable types */
    val target: Int? = null,

    /** Reminder time as ISO-8601 string (e.g., "08:30") or null */
    val reminderTime: String? = null,

    /** Place label for location-based habits (optional) */
    val locationName: String? = null,

    /** Motion threshold (e.g., minutes of activity) for movement habits */
    val motionThresholdMinutes: Int? = null,

    /** Created/updated timestamps stored as epoch millis */
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    /** Soft-delete / archive flag */
    val archived: Boolean = false
)
