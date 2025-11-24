package com.microhabitcoach.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.microhabitcoach.data.database.entity.UserPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    suspend fun getUserPreferences(userId: String = "default_user"): UserPreferences?
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    fun getUserPreferencesFlow(userId: String = "default_user"): Flow<UserPreferences?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)
    
    @Update
    suspend fun updateUserPreferences(preferences: UserPreferences)
    
    @Query("UPDATE user_preferences SET preferredCategories = :categories, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updatePreferredCategories(userId: String, categories: String, updatedAt: Long)
    
    @Query("UPDATE user_preferences SET notificationsEnabled = :enabled, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean, updatedAt: Long)
    
    @Query("UPDATE user_preferences SET quietHoursStart = :start, quietHoursEnd = :end, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateQuietHours(userId: String, start: String?, end: String?, updatedAt: Long)
}

