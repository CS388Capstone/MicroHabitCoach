package com.microhabitcoach.data.database

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
            INSTANCE = instance
            instance
        }
    }
}

