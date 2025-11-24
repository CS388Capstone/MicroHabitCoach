package com.microhabitcoach.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

object LocationDataConverter {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun fromLocationData(value: LocationData?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toLocationData(value: String?): LocationData? {
        return value?.let {
            val type = object : TypeToken<LocationData>() {}.type
            gson.fromJson(it, type)
        }
    }
}

