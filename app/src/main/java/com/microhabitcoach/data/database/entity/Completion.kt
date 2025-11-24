package com.microhabitcoach.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"]), Index(value = ["completedAt"])]
)
data class Completion(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val completedAt: Long, // Timestamp
    val autoCompleted: Boolean = false, // true if sensor-triggered
    val notes: String? = null
)

