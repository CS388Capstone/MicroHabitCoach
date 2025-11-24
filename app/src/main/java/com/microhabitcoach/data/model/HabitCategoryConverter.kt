package com.microhabitcoach.data.model

import androidx.room.TypeConverter

object HabitCategoryConverter {
    @TypeConverter
    @JvmStatic
    fun fromHabitCategory(value: HabitCategory?): String? {
        return value?.name
    }

    @TypeConverter
    @JvmStatic
    fun toHabitCategory(value: String?): HabitCategory? {
        return value?.let { HabitCategory.valueOf(it) }
    }
}

