package com.cs388.microhabitcoach.data.repository

import com.cs388.microhabitcoach.data.AppDatabase
import com.cs388.microhabitcoach.data.entities.CompletionLog
import com.cs388.microhabitcoach.data.entities.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Small repository that wraps DAOs and exposes coroutine-friendly APIs.
 * Keep logic minimal — business rules (e.g., auto-complete heuristics) belong in a higher layer.
 */
class HabitRepository(private val db: AppDatabase) {
    private val habitDao = db.habitDao()
    private val logDao = db.completionLogDao()

    // Habit operations
    suspend fun insertHabit(habit: Habit): Long = habitDao.insert(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.update(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.delete(habit)

    fun observeActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()
    fun observeHabit(id: Long): Flow<Habit?> = habitDao.getHabitById(id)

    // Completion logs
    suspend fun addCompletion(habitId: Long, auto: Boolean = false): Long {
        val log = CompletionLog(habitId = habitId, auto = auto)
        return logDao.insert(log)
    }

    fun observeLogsForHabit(habitId: Long): Flow<List<CompletionLog>> = logDao.getLogsForHabit(habitId)

    suspend fun getLogsForHabitBetween(habitId: Long, startMs: Long, endMs: Long) =
        logDao.getLogsForHabitBetween(habitId, startMs, endMs)

    suspend fun clearLogsForHabit(habitId: Long) = logDao.deleteLogsForHabit(habitId)
}
