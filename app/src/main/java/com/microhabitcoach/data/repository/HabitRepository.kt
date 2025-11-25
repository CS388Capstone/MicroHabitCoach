package com.microhabitcoach.data.repository

import android.content.Context
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit

class HabitRepository(context: Context) {

    private val habitDao: HabitDao = DatabaseModule.getDatabase(context).habitDao()

    suspend fun getHabitById(id: String): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
}

