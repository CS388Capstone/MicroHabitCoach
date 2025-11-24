package com.microhabitcoach.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): Habit?
    
    @Query("SELECT * FROM habits WHERE isActive = 1 AND type = :type ORDER BY createdAt DESC")
    fun getHabitsByType(type: HabitType): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE isActive = 1 AND category = :category ORDER BY createdAt DESC")
    fun getHabitsByCategory(category: HabitCategory): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE isActive = 1 AND type = :type AND motionType = :motionType")
    suspend fun getMotionHabitsByType(type: HabitType, motionType: String): List<Habit>
    
    @Query("SELECT * FROM habits WHERE isActive = 1 AND type = :type AND location IS NOT NULL")
    suspend fun getLocationHabits(type: HabitType): List<Habit>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)
    
    @Update
    suspend fun updateHabit(habit: Habit)
    
    @Delete
    suspend fun deleteHabit(habit: Habit)
    
    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)
    
    @Query("UPDATE habits SET streakCount = :streakCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStreakCount(id: String, streakCount: Int, updatedAt: Long)
    
    @Query("UPDATE habits SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivateHabit(id: String, updatedAt: Long)
}

