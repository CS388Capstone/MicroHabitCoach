package com.microhabitcoach.ui.habitdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.repository.DefaultHabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HabitDetailViewModel(
    application: Application,
    private val habitId: String
) : AndroidViewModel(application) {

    private val database = DatabaseModule.getDatabase(application)
    private val repository = DefaultHabitRepository(application)
    private val completionDao = database.completionDao()

    // LiveData observables
    private val _habit = MutableLiveData<Habit?>()
    val habit: LiveData<Habit?> = _habit

    private val _detailData = MutableLiveData<HabitDetailData?>()
    val detailData: LiveData<HabitDetailData?> = _detailData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    /**
     * Loads habit data and calculates all analytics.
     */
    fun loadHabit() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load habit
                val habit = withContext(Dispatchers.IO) {
                    repository.getHabitById(habitId)
                }
                
                if (habit == null) {
                    _error.value = "Habit not found"
                    _isLoading.value = false
                    return@launch
                }
                
                _habit.value = habit
                
                // Load completion history
                val completions = withContext(Dispatchers.IO) {
                    completionDao.getCompletionsForHabitSync(habitId)
                }
                
                // Calculate analytics
                val analyticsData = withContext(Dispatchers.Default) {
                    calculateAnalytics(habit, completions)
                }
                
                _detailData.value = analyticsData
                _isLoading.value = false
                
            } catch (e: Exception) {
                _error.value = "Failed to load habit: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculates all analytics for the habit.
     */
    private fun calculateAnalytics(
        habit: Habit,
        completions: List<Completion>
    ): HabitDetailData {
        // Calculate streaks
        val streakInfo = HabitAnalytics.calculateStreaks(completions)
        
        // Calculate completion stats
        val completionStats = HabitAnalytics.calculateCompletionStats(
            completions,
            habit.createdAt
        )
        
        // Analyze trend
        val trendAnalysis = HabitAnalytics.analyzeTrend(completions)
        
        // Find best day
        val bestDayInfo = HabitAnalytics.findBestDay(completions)
        
        // Format calendar data
        val calendarData = HabitAnalytics.createCalendarData(completions)
        
        // Format history items
        val historyItems = HabitAnalytics.formatCompletionHistory(completions)
        
        return HabitDetailData(
            habitId = habit.id,
            habitName = habit.name,
            streakInfo = streakInfo,
            completionStats = completionStats,
            trendAnalysis = trendAnalysis,
            bestDayInfo = bestDayInfo,
            calendarData = calendarData,
            historyItems = historyItems
        )
    }

    /**
     * Refreshes the habit data.
     */
    fun refresh() {
        loadHabit()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
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

