package com.microhabitcoach.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.repository.DefaultHabitRepository
import com.microhabitcoach.data.repository.HabitRepository
import com.microhabitcoach.geofence.GeofenceService
import com.microhabitcoach.notification.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TodayViewModel(
    application: Application,
    private val repository: HabitRepository = DefaultHabitRepository(application)
) : AndroidViewModel(application) {

    private val _habits = MutableLiveData<List<HabitWithCompletion>>(emptyList())
    val habits: LiveData<List<HabitWithCompletion>> = _habits

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var habitsJob: Job? = null

    fun loadHabits() {
        // Cancel existing job if running
        habitsJob?.cancel()
        
        _isLoading.value = true
        habitsJob = viewModelScope.launch {
            repository.observeHabits()
                .catch { throwable ->
                    _error.value = throwable.message ?: "Unable to load habits"
                    _isLoading.value = false
                }
                .collect { list ->
                    // Check completion status for each habit in parallel
                    val habitsWithCompletion = list.map { habit ->
                        async {
                            val isCompleted = repository.isHabitCompletedToday(habit.id)
                            HabitWithCompletion(habit, isCompleted)
                        }
                    }.awaitAll()
                    // Sort: incomplete first, completed below
                    val sorted = habitsWithCompletion.sortedBy { it.isCompletedToday }
                    _habits.value = sorted
                    // Only set loading to false on first load, not on every update
                    if (_isLoading.value == true) {
                        _isLoading.value = false
                    }
                }
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.completeHabit(habitId)
                // Refresh habits to update completion status
                refreshHabitsCompletionStatus()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Failed to complete habit"
            }
        }
    }

    fun refreshHabitsCompletionStatus() {
        viewModelScope.launch {
            val currentHabits = _habits.value
            if (currentHabits != null) {
                val updated = currentHabits.map { habitWithCompletion ->
                    val isCompleted = repository.isHabitCompletedToday(habitWithCompletion.habit.id)
                    HabitWithCompletion(habitWithCompletion.habit, isCompleted)
                }
                val sorted = updated.sortedBy { it.isCompletedToday }
                _habits.value = sorted
            }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            try {
                // Cancel reminder notifications before deleting
                ReminderScheduler.cancelHabitReminders(getApplication(), id)
                
                // Remove geofence before deleting
                GeofenceService.removeGeofence(getApplication(), id)
                
                repository.deleteHabit(id)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Failed to delete habit"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        habitsJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val application: Application,
        private val repository: HabitRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
                return if (repository != null) {
                    TodayViewModel(application, repository) as T
                } else {
                    TodayViewModel(application) as T
                }
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

