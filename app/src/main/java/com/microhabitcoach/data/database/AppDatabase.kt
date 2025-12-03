package com.microhabitcoach.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.microhabitcoach.data.database.dao.ApiSuggestionDao
import com.microhabitcoach.data.database.dao.CompletionDao
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.dao.SavedArticleDao
import com.microhabitcoach.data.database.dao.UserPreferencesDao
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.database.entity.UserPreferences

@Database(
    entities = [
        Habit::class,
        Completion::class,
        ApiSuggestion::class,
        SavedArticle::class,
        UserPreferences::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(
    com.microhabitcoach.data.model.HabitCategoryConverter::class,
    com.microhabitcoach.data.model.HabitTypeConverter::class,
    com.microhabitcoach.data.model.LocalTimeListConverter::class,
    com.microhabitcoach.data.model.LocationDataConverter::class,
    com.microhabitcoach.data.model.IntListConverter::class,
    com.microhabitcoach.data.database.entity.HabitCategorySetConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao
    abstract fun apiSuggestionDao(): ApiSuggestionDao
    abstract fun savedArticleDao(): SavedArticleDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    companion object {
        const val DATABASE_NAME = "microhabit_database"
    }
}

