package com.microhabitcoach.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalTime

object LocalTimeListConverter {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun fromLocalTimeList(value: List<LocalTime>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toLocalTimeList(value: String?): List<LocalTime>? {
        return value?.let {
            val type = object : TypeToken<List<LocalTime>>() {}.type
            gson.fromJson(it, type)
        }
    }
}

