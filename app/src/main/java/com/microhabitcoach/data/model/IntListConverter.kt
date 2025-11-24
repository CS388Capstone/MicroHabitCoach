package com.microhabitcoach.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object IntListConverter {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toIntList(value: String?): List<Int>? {
        return value?.let {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, type)
        }
    }
}

