package com.microhabitcoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.repository.HabitRepository
import kotlinx.coroutines.launch
import java.util.UUID

class AddEditHabitViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: HabitRepository
    
    init {
        val database = DatabaseModule.getDatabase(application)
        repository = HabitRepository(database)
    }
    
    // Current habit being edited (null if creating new)
    private val _currentHabit = MutableLiveData<Habit?>()
    val currentHabit: LiveData<Habit?> = _currentHabit
    
    // Form validation state
    private val _isFormValid = MutableLiveData<Boolean>(false)
    val isFormValid: LiveData<Boolean> = _isFormValid
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error state
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Success state (for navigation)
    private val _saveSuccess = MutableLiveData<Boolean>(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
    /**
     * Load an existing habit for editing
     */
    fun loadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habit = repository.getHabitById(habitId)
                _currentHabit.value = habit
                _isFormValid.value = habit != null && validateHabit(habit)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load habit: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Save habit (create new or update existing)
     */
    fun saveHabit(
        name: String,
        category: HabitCategory,
        type: HabitType,
        motionType: String? = null,
        targetDuration: Int? = null,
        location: com.microhabitcoach.data.model.LocationData? = null,
        geofenceRadius: Float? = null,
        reminderTimes: List<java.time.LocalTime>? = null,
        reminderDays: List<Int>? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Validate form
                if (name.isBlank()) {
                    _errorMessage.value = "Habit name cannot be empty"
                    _isFormValid.value = false
                    return@launch
                }
                
                val currentHabit = _currentHabit.value
                val habit = if (currentHabit != null) {
                    // Update existing habit
                    currentHabit.copy(
                        name = name.trim(),
                        category = category,
                        type = type,
                        motionType = motionType,
                        targetDuration = targetDuration,
                        location = location,
                        geofenceRadius = geofenceRadius,
                        reminderTimes = reminderTimes,
                        reminderDays = reminderDays,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    // Create new habit
                    Habit(
                        id = UUID.randomUUID().toString(),
                        name = name.trim(),
                        category = category,
                        type = type,
                        motionType = motionType,
                        targetDuration = targetDuration,
                        location = location,
                        geofenceRadius = geofenceRadius,
                        reminderTimes = reminderTimes,
                        reminderDays = reminderDays,
                        streakCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        isActive = true
                    )
                }
                
                if (currentHabit != null) {
                    repository.updateHabit(habit)
                } else {
                    repository.insertHabit(habit)
                }
                
                _saveSuccess.value = true
                _isFormValid.value = true
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save habit: ${e.message}"
                _isFormValid.value = false
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Validate habit data
     */
    fun validateHabit(habit: Habit?): Boolean {
        if (habit == null) return false
        if (habit.name.isBlank()) return false
        
        // Type-specific validation
        when (habit.type) {
            HabitType.MOTION -> {
                if (habit.motionType.isNullOrBlank() || habit.targetDuration == null) {
                    return false
                }
            }
            HabitType.LOCATION -> {
                if (habit.location == null) {
                    return false
                }
            }
            HabitType.TIME -> {
                if (habit.reminderTimes.isNullOrEmpty()) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Validate form fields
     */
    fun validateForm(
        name: String,
        type: HabitType,
        motionType: String? = null,
        targetDuration: Int? = null,
        location: com.microhabitcoach.data.model.LocationData? = null,
        reminderTimes: List<java.time.LocalTime>? = null
    ): Boolean {
        if (name.isBlank()) {
            _isFormValid.value = false
            return false
        }
        
        when (type) {
            HabitType.MOTION -> {
                if (motionType.isNullOrBlank() || targetDuration == null || targetDuration <= 0) {
                    _isFormValid.value = false
                    return false
                }
            }
            HabitType.LOCATION -> {
                if (location == null) {
                    _isFormValid.value = false
                    return false
                }
            }
            HabitType.TIME -> {
                if (reminderTimes.isNullOrEmpty()) {
                    _isFormValid.value = false
                    return false
                }
            }
        }
        
        _isFormValid.value = true
        return true
    }
    
    /**
     * Reset form state (for creating new habit)
     */
    fun resetForm() {
        _currentHabit.value = null
        _isFormValid.value = false
        _errorMessage.value = null
        _saveSuccess.value = false
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Reset save success flag (after navigation)
     */
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}


