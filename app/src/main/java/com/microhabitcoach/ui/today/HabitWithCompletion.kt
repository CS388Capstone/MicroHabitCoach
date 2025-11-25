package com.microhabitcoach.ui.today

import com.microhabitcoach.data.database.entity.Habit

data class HabitWithCompletion(
    val habit: Habit,
    val isCompletedToday: Boolean
)

