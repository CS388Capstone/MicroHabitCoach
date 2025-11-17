package com.cs388.microhabitcoach.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cs388.microhabitcoach.data.dao.CompletionLogDao
import com.cs388.microhabitcoach.data.dao.HabitDao
import com.cs388.microhabitcoach.data.entities.CompletionLog
import com.cs388.microhabitcoach.data.entities.Habit

@Database(
    entities = [Habit::class, CompletionLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionLogDao(): CompletionLogDao
}
