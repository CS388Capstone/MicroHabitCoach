package com.microhabitcoach.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microhabitcoach.data.database.entity.Completion
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    
    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun getCompletionsForHabit(habitId: String): Flow<List<Completion>>
    
    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY completedAt DESC")
    suspend fun getCompletionsForHabitSync(habitId: String): List<Completion>
    
    @Query("SELECT * FROM completions WHERE habitId = :habitId AND completedAt >= :startTime AND completedAt <= :endTime")
    suspend fun getCompletionsInRange(habitId: String, startTime: Long, endTime: Long): List<Completion>
    
    @Query("SELECT * FROM completions WHERE completedAt >= :startTime AND completedAt <= :endTime")
    suspend fun getAllCompletionsInRange(startTime: Long, endTime: Long): List<Completion>
    
    @Query("SELECT * FROM completions WHERE habitId = :habitId AND completedAt >= :dayStart AND completedAt < :dayEnd LIMIT 1")
    suspend fun getCompletionForDay(habitId: String, dayStart: Long, dayEnd: Long): Completion?
    
    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun getCompletionCount(habitId: String): Int
    
    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId AND completedAt >= :startTime AND completedAt <= :endTime")
    suspend fun getCompletionCountInRange(habitId: String, startTime: Long, endTime: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: Completion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<Completion>)
    
    @Delete
    suspend fun deleteCompletion(completion: Completion)
    
    @Query("DELETE FROM completions WHERE id = :id")
    suspend fun deleteCompletionById(id: String)
    
    @Query("DELETE FROM completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: String)
    
    @Query("DELETE FROM completions WHERE completedAt < :beforeTime")
    suspend fun deleteOldCompletions(beforeTime: Long)
}

