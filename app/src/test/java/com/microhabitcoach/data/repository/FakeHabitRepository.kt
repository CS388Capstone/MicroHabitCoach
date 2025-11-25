package com.microhabitcoach.data.repository

import com.microhabitcoach.data.database.entity.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeHabitRepository : HabitRepository {

    private val habitStore = linkedMapOf<String, Habit>()
    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())

    var shouldFailComplete = false
    var lastSavedHabit: Habit? = null

    override fun observeHabits(): Flow<List<Habit>> = habitsFlow

    override suspend fun getHabitById(id: String): Habit? = habitStore[id]

    override suspend fun saveHabit(habit: Habit) {
        habitStore[habit.id] = habit
        lastSavedHabit = habit
        emitSnapshot()
    }

    override suspend fun completeHabit(habitId: String) {
        if (shouldFailComplete) throw IllegalStateException("Complete failed")
        val habit = habitStore[habitId] ?: throw IllegalArgumentException("Habit not found")
        habitStore[habitId] = habit.copy(streakCount = habit.streakCount + 1)
        emitSnapshot()
    }

    suspend fun seedHabits(habits: List<Habit>) {
        habitStore.clear()
        habits.forEach { habitStore[it.id] = it }
        emitSnapshot()
    }

    fun snapshot(): List<Habit> = habitStore.values.toList()

    private fun emitSnapshot() {
        habitsFlow.update { habitStore.values.toList() }
    }
}

