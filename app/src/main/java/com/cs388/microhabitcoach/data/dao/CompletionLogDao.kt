package com.cs388.microhabitcoach.data.dao

import androidx.room.*
import com.cs388.microhabitcoach.data.entities.CompletionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CompletionLog): Long

    @Query("SELECT * FROM completion_logs WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<CompletionLog>>

    @Query("SELECT * FROM completion_logs WHERE habitId = :habitId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getLogsForHabitBetween(habitId: Long, start: Long, end: Long): List<CompletionLog>

    @Query("DELETE FROM completion_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
