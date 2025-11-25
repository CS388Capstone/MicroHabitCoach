package com.microhabitcoach.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitRepository(private val database: AppDatabase) {
    
    private val habitDao = database.habitDao()
    private val completionDao = database.completionDao()
    
    // LiveData observables
    fun getAllHabits(): LiveData<List<Habit>> {
        return habitDao.getAllHabits().asLiveData()
    }
    
    suspend fun getHabitById(id: String): Habit? {
        return withContext(Dispatchers.IO) {
            try {
                habitDao.getHabitById(id)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun insertHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            try {
                habitDao.insertHabit(habit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun updateHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            try {
                habitDao.updateHabit(habit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun deleteHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            try {
                habitDao.deleteHabit(habit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getCompletionsForHabit(habitId: String): LiveData<List<Completion>> {
        return completionDao.getCompletionsForHabit(habitId).asLiveData()
    }
    
    suspend fun completeHabit(habitId: String, autoCompleted: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                // Get the habit
                val habit = habitDao.getHabitById(habitId) ?: return@withContext
                
                val currentTime = System.currentTimeMillis()
                
                // Check if already completed today
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val dayStart = calendar.timeInMillis
                
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val dayEnd = calendar.timeInMillis
                
                val existingCompletion = completionDao.getCompletionForDay(habitId, dayStart, dayEnd)
                
                // Only update streak if not already completed today
                var newStreakCount = habit.streakCount
                if (existingCompletion == null) {
                    // Increment streak if this is first completion today
                    newStreakCount = habit.streakCount + 1
                }
                
                // Create completion record
                val completion = Completion(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    completedAt = currentTime,
                    autoCompleted = autoCompleted
                )
                completionDao.insertCompletion(completion)
                
                // Update habit with new streak and timestamp
                val updatedHabit = habit.copy(
                    streakCount = newStreakCount,
                    updatedAt = currentTime
                )
                habitDao.updateHabit(updatedHabit)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

    override suspend fun saveHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    override suspend fun completeHabit(habitId: String) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val updatedHabit = habit.copy(
            streakCount = habit.streakCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        habitDao.updateHabit(updatedHabit)
    }
}
