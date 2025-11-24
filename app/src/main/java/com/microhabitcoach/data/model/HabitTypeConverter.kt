package com.microhabitcoach.data.model

import androidx.room.TypeConverter

object HabitTypeConverter {
    @TypeConverter
    @JvmStatic
    fun fromHabitType(value: HabitType?): String? {
        return value?.name
    }

    @TypeConverter
    @JvmStatic
    fun toHabitType(value: String?): HabitType? {
        return value?.let { HabitType.valueOf(it) }
    }
}

