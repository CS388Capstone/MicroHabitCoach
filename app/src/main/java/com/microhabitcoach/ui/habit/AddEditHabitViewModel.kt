package com.microhabitcoach.ui.habit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.data.repository.DefaultHabitRepository
import com.microhabitcoach.data.repository.HabitRepository
import com.microhabitcoach.geofence.GeofenceService
import com.microhabitcoach.notification.ReminderScheduler
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID

class AddEditHabitViewModel(
    application: Application,
    private val repository: HabitRepository = DefaultHabitRepository(application)
) : AndroidViewModel(application) {

    private val _habit = MutableLiveData<Habit?>()
    val habit: LiveData<Habit?> = _habit

    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState

    private val _formState = MutableLiveData<FormState>(FormState.Idle)
    val formState: LiveData<FormState> = _formState

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadHabit(id: String) {
        viewModelScope.launch {
            try {
                _formState.value = FormState.Loading
                val result = repository.getHabitById(id)
                _habit.value = result
                _formState.value = FormState.Loaded(result)
            } catch (t: Throwable) {
                _formState.value = FormState.Error(t.message ?: "Failed to load habit")
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
                _saveState.value = SaveState.Saving
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

                repository.saveHabit(habit)
                _habit.value = habit
                
                // Schedule reminder notifications for time-based habits
                ReminderScheduler.rescheduleHabitReminders(getApplication(), habit)
                
                // Handle geofencing for location-based habits
                when {
                    // New location-based habit - add geofence
                    habit.type == HabitType.LOCATION && habit.location != null && habit.geofenceRadius != null 
                    && existingHabit == null -> {
                        GeofenceService.addGeofence(getApplication(), habit)
                    }
                    // Editing location-based habit - remove old, add new (in case location/radius changed)
                    habit.type == HabitType.LOCATION && habit.location != null && habit.geofenceRadius != null 
                    && existingHabit != null -> {
                        // Always remove old geofence first when editing (handles location/radius changes)
                        GeofenceService.removeGeofence(getApplication(), habit.id)
                        // Add updated geofence
                        GeofenceService.addGeofence(getApplication(), habit)
                    }
                    // Habit type changed from LOCATION to something else - remove geofence
                    existingHabit?.type == HabitType.LOCATION && habit.type != HabitType.LOCATION -> {
                        GeofenceService.removeGeofence(getApplication(), habit.id)
                    }
                    // Location-based habit but missing location or radius - remove geofence
                    existingHabit?.type == HabitType.LOCATION && 
                    (habit.location == null || habit.geofenceRadius == null) -> {
                        GeofenceService.removeGeofence(getApplication(), habit.id)
                    }
                }
                
                _saveState.value = SaveState.Success
            } catch (t: Throwable) {
                _saveState.value = SaveState.Error(t.message ?: "Failed to save habit")
                _error.value = t.message
            }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving
                
                // Cancel reminder notifications before deleting
                ReminderScheduler.cancelHabitReminders(getApplication(), id)
                
                // Remove geofence before deleting
                GeofenceService.removeGeofence(getApplication(), id)
                
                repository.deleteHabit(id)
                _habit.value = null
                _saveState.value = SaveState.Success
            } catch (t: Throwable) {
                _saveState.value = SaveState.Error(t.message ?: "Failed to delete habit")
                _error.value = t.message
            }
        }
    }

    data class ValidationResult(val isValid: Boolean, val errors: List<String>)

    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        data class Loaded(val habit: Habit?) : FormState()
        data class Error(val message: String) : FormState()
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    class Factory(
        private val application: Application,
        private val repository: HabitRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddEditHabitViewModel::class.java)) {
                return if (repository != null) {
                    AddEditHabitViewModel(application, repository) as T
                } else {
                    AddEditHabitViewModel(application) as T
                }
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

