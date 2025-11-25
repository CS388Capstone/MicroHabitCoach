package com.microhabitcoach.data.repository

import android.content.Context
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: String): Habit?
    suspend fun saveHabit(habit: Habit)
    suspend fun completeHabit(habitId: String)
    suspend fun deleteHabit(id: String)
}

class DefaultHabitRepository(
    private val habitDao: HabitDao
) : HabitRepository {

    constructor(context: Context) : this(DatabaseModule.getDatabase(context).habitDao())

    override fun observeHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    override suspend fun getHabitById(id: String): Habit? = habitDao.getHabitById(id)

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

    override suspend fun deleteHabit(id: String) {
        habitDao.deleteHabitById(id)
    }
}
