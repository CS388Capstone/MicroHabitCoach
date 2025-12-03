package com.microhabitcoach.data.repository

import android.content.Context
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.CompletionDao
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: String): Habit?
    suspend fun saveHabit(habit: Habit)
    suspend fun completeHabit(habitId: String)
    suspend fun deleteHabit(id: String)
    suspend fun isHabitCompletedToday(habitId: String): Boolean
    suspend fun clearAllCompletions()
    suspend fun resetAllStreaks()
}

class DefaultHabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao
) : HabitRepository {

    constructor(context: Context) : this(
        DatabaseModule.getDatabase(context).habitDao(),
        DatabaseModule.getDatabase(context).completionDao()
    )

    override fun observeHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    override suspend fun getHabitById(id: String): Habit? = habitDao.getHabitById(id)

    override suspend fun saveHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    override suspend fun completeHabit(habitId: String) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val now = System.currentTimeMillis()
        
        // Check if already completed today
        val todayStart = getTodayStartTimestamp()
        val todayEnd = todayStart + 24 * 60 * 60 * 1000
        val existingCompletion = completionDao.getCompletionForDay(habitId, todayStart, todayEnd)
        
        if (existingCompletion == null) {
            // Create completion record
            val completion = Completion(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                completedAt = now,
                autoCompleted = false
            )
            completionDao.insertCompletion(completion)
            
            // Update streak count
            val updatedHabit = habit.copy(
                streakCount = habit.streakCount + 1,
                updatedAt = now
            )
            habitDao.updateHabit(updatedHabit)
        }
    }

    override suspend fun deleteHabit(id: String) {
        habitDao.deleteHabitById(id)
    }

    override suspend fun isHabitCompletedToday(habitId: String): Boolean {
        val todayStart = getTodayStartTimestamp()
        val todayEnd = todayStart + 24 * 60 * 60 * 1000
        val completion = completionDao.getCompletionForDay(habitId, todayStart, todayEnd)
        return completion != null
    }

    override suspend fun clearAllCompletions() {
        completionDao.deleteAllCompletions()
    }

    override suspend fun resetAllStreaks() {
        val now = System.currentTimeMillis()
        val allHabits = habitDao.getAllHabitsSync()
        allHabits.forEach { habit ->
            habitDao.updateStreakCount(habit.id, 0, now)
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
