package com.microhabitcoach.ui.habit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.data.repository.HabitRepository
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID

class AddEditHabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository(application)

    private val _habit = MutableLiveData<Habit?>()
    val habit: LiveData<Habit?> = _habit

    private val _saveState = MutableLiveData<SaveState>()
    val saveState: LiveData<SaveState> = _saveState

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadHabit(id: String) {
        viewModelScope.launch {
            try {
                _habit.value = repository.getHabitById(id)
            } catch (t: Throwable) {
                _error.value = t.message
            }
        }
    }

    fun validateForm(
        name: String,
        type: HabitType,
        reminderTimes: List<LocalTime>,
        reminderDays: List<Int>,
        motionType: String?,
        duration: Int?,
        hasLocation: Boolean,
        radius: Float?
    ): ValidationResult {
        val result = HabitFormValidator.validate(
            name = name,
            type = type,
            times = reminderTimes,
            days = reminderDays,
            motionType = motionType,
            duration = duration,
            hasLocation = hasLocation,
            radius = radius
        )
        return ValidationResult(result.valid, result.errors)
    }

    fun saveHabit(
        existingHabit: Habit?,
        name: String,
        category: HabitCategory,
        type: HabitType,
        reminderTimes: List<LocalTime>?,
        reminderDays: List<Int>?,
        motionType: String?,
        targetDuration: Int?,
        location: LocationData?,
        geofenceRadius: Float?
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val id = existingHabit?.id ?: UUID.randomUUID().toString()
                val habit = Habit(
                    id = id,
                    name = name,
                    category = category,
                    type = type,
                    motionType = if (type == HabitType.MOTION) motionType else null,
                    targetDuration = if (type == HabitType.MOTION) targetDuration else null,
                    location = if (type == HabitType.LOCATION) location else null,
                    geofenceRadius = if (type == HabitType.LOCATION) geofenceRadius else null,
                    reminderTimes = if (type == HabitType.TIME) reminderTimes else null,
                    reminderDays = if (type == HabitType.TIME) reminderDays else null,
                    streakCount = existingHabit?.streakCount ?: 0,
                    createdAt = existingHabit?.createdAt ?: now,
                    updatedAt = now,
                    isActive = existingHabit?.isActive ?: true
                )

                if (existingHabit == null) {
                    repository.insertHabit(habit)
                } else {
                    repository.updateHabit(habit)
                }

                _saveState.value = SaveState(success = true, message = null)
            } catch (t: Throwable) {
                _saveState.value = SaveState(success = false, message = t.message ?: "Failed to save habit")
            }
        }
    }

    data class ValidationResult(val isValid: Boolean, val errors: List<String>)

    data class SaveState(val success: Boolean, val message: String?)
}

