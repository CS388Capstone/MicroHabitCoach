package com.microhabitcoach.ui.habitdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HabitDetailViewModel(
    application: Application,
    private val habitId: String
) : AndroidViewModel(application) {

    fun loadHabit() {
        viewModelScope.launch {
            // TODO: Load habit detail data
        }
    }

    class Factory(
        private val application: Application,
        private val habitId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HabitDetailViewModel::class.java)) {
                return HabitDetailViewModel(application, habitId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

