package com.microhabitcoach.data.model

import androidx.room.TypeConverter
import java.time.LocalTime

object LocalTimeListConverter {
    @TypeConverter
    @JvmStatic
    fun fromLocalTimeList(value: List<LocalTime>?): String? {
        return value?.let {
            if (it.isEmpty()) "[]" else it.joinToString(separator = ",") { time -> time.toString() }
        }
    }

    @TypeConverter
    @JvmStatic
    fun toLocalTimeList(value: String?): List<LocalTime>? {
        return value
            ?.takeIf { it.isNotBlank() }
            ?.let { raw ->
                if (raw == "[]") emptyList()
                else raw.split(",").map { LocalTime.parse(it) }
            }
    }
}

