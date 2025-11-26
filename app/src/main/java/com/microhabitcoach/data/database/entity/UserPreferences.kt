package com.microhabitcoach.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microhabitcoach.data.model.HabitCategory

@Entity(tableName = "user_preferences")
@TypeConverters(HabitCategorySetConverter::class)
data class UserPreferences(
    @PrimaryKey
    val userId: String = "default_user",
    val preferredCategories: Set<HabitCategory> = emptySet(),
    val quietHoursStart: String? = null, // HH:mm format
    val quietHoursEnd: String? = null, // HH:mm format
    val notificationsEnabled: Boolean = true,
    val batteryOptimizationMode: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object HabitCategorySetConverter {
    private val gson = Gson()

    @androidx.room.TypeConverter
    @JvmStatic
    fun fromHabitCategorySet(value: Set<HabitCategory>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @androidx.room.TypeConverter
    @JvmStatic
    fun toHabitCategorySet(value: String?): Set<HabitCategory>? {
        return value?.let {
            val type = object : TypeToken<Set<HabitCategory>>() {}.type
            gson.fromJson(it, type) ?: emptySet()
        } ?: emptySet()
    }
}

