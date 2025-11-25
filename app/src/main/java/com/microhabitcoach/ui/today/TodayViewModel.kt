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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TodayViewModel(
    application: Application,
    private val repository: HabitRepository = DefaultHabitRepository(application)
) : AndroidViewModel(application) {

    private val _habits = MutableLiveData<List<Habit>>(emptyList())
    val habits: LiveData<List<Habit>> = _habits

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var habitsJob: Job? = null

    fun loadHabits() {
        if (habitsJob != null) return
        _isLoading.value = true
        habitsJob = viewModelScope.launch {
            repository.observeHabits()
                .catch { throwable ->
                    _error.value = throwable.message ?: "Unable to load habits"
                    _isLoading.value = false
                }
                .collect { list ->
                    _habits.value = list
                    _isLoading.value = false
                }
        }
    }

    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            try {
                repository.completeHabit(habitId)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Failed to complete habit"
            }
        }
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

