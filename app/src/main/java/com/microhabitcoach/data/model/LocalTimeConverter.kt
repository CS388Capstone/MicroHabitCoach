package com.microhabitcoach.data.model

import androidx.room.TypeConverter
import java.time.LocalTime

object LocalTimeConverter {
    @TypeConverter
    @JvmStatic
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
}

