package com.microhabitcoach.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.dao.CompletionDao
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.launch

class StatsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val habitDao: HabitDao = DatabaseModule.getDatabase(application).habitDao()
    private val completionDao: CompletionDao = DatabaseModule.getDatabase(application).completionDao()
    private val analytics = StatsAnalytics(habitDao, completionDao)

    private val _totalHabits = MutableLiveData<Int>()
    val totalHabits: LiveData<Int> = _totalHabits

    private val _longestStreak = MutableLiveData<Int>()
    val longestStreak: LiveData<Int> = _longestStreak

    private val _completionRate7Days = MutableLiveData<Double>()
    val completionRate7Days: LiveData<Double> = _completionRate7Days

    private val _completionRate30Days = MutableLiveData<Double>()
    val completionRate30Days: LiveData<Double> = _completionRate30Days

    private val _totalCompletions = MutableLiveData<Int>()
    val totalCompletions: LiveData<Int> = _totalCompletions

    private val _mostConsistentHabit = MutableLiveData<MostConsistentHabit?>()
    val mostConsistentHabit: LiveData<MostConsistentHabit?> = _mostConsistentHabit

    private val _bestDayOfWeek = MutableLiveData<BestDayInfo?>()
    val bestDayOfWeek: LiveData<BestDayInfo?> = _bestDayOfWeek

    private val _currentStreakLeader = MutableLiveData<Habit?>()
    val currentStreakLeader: LiveData<Habit?> = _currentStreakLeader

    private val _completionTrendData = MutableLiveData<List<Pair<Long, Int>>>()
    val completionTrendData: LiveData<List<Pair<Long, Int>>> = _completionTrendData

    private val _categoryBreakdownData = MutableLiveData<Map<HabitCategory, Int>>()
    val categoryBreakdownData: LiveData<Map<HabitCategory, Int>> = _categoryBreakdownData

    private val _weeklyHeatmapData = MutableLiveData<Array<Array<Int>>>()
    val weeklyHeatmapData: LiveData<Array<Array<Int>>> = _weeklyHeatmapData

    private val _totalDaysActive = MutableLiveData<Int>()
    val totalDaysActive: LiveData<Int> = _totalDaysActive

    private val _perfectDays = MutableLiveData<Int>()
    val perfectDays: LiveData<Int> = _perfectDays

    private val _topPerformingHabits = MutableLiveData<List<HabitPerformance>>()
    val topPerformingHabits: LiveData<List<HabitPerformance>> = _topPerformingHabits

    private val _weeklyComparison = MutableLiveData<WeeklyComparison?>()
    val weeklyComparison: LiveData<WeeklyComparison?> = _weeklyComparison

    private val _consistencyScore = MutableLiveData<ConsistencyScore?>()
    val consistencyScore: LiveData<ConsistencyScore?> = _consistencyScore

    private val _categoryPerformance = MutableLiveData<List<CategoryPerformance>>()
    val categoryPerformance: LiveData<List<CategoryPerformance>> = _categoryPerformance

    private val _motivationalMessage = MutableLiveData<String>()
    val motivationalMessage: LiveData<String> = _motivationalMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load aggregate statistics
                _totalHabits.value = analytics.getTotalHabits()
                _longestStreak.value = analytics.getLongestStreak()
                _totalCompletions.value = analytics.getTotalCompletions()
                _completionRate7Days.value = analytics.getOverallCompletionRate(7)
                _completionRate30Days.value = analytics.getOverallCompletionRate(30)

                // Load insights
                _mostConsistentHabit.value = analytics.getMostConsistentHabit(30)
                _bestDayOfWeek.value = analytics.getBestDayOfWeek()
                _currentStreakLeader.value = analytics.getCurrentStreakLeader()

                // Load chart data
                _categoryBreakdownData.value = analytics.getCategoryBreakdownData()
                _weeklyHeatmapData.value = analytics.getWeeklyHeatmapData()
                
                // Load additional metrics
                _totalDaysActive.value = analytics.getTotalDaysActive(30)
                _perfectDays.value = analytics.getPerfectDays(30)
                
                // Load new engaging stats
                _topPerformingHabits.value = analytics.getTopPerformingHabits(3)
                _weeklyComparison.value = analytics.getWeeklyComparison()
                _consistencyScore.value = analytics.getConsistencyScore()
                _categoryPerformance.value = analytics.getCategoryPerformance()
                
                // Generate motivational message
                _motivationalMessage.value = generateMotivationalMessage()
            } catch (e: Exception) {
                // Handle error silently or show error message
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMotivationalMessage(): String {
        val totalHabits = _totalHabits.value ?: 0
        val longestStreak = _longestStreak.value ?: 0
        val completionRate = _completionRate30Days.value ?: 0.0
        val perfectDays = _perfectDays.value ?: 0
        
        return when {
            totalHabits == 0 -> "Start your journey! Create your first habit to begin tracking your progress."
            longestStreak >= 30 -> "🔥 Incredible! You're building amazing consistency. Keep it up!"
            longestStreak >= 14 -> "🌟 Great work! You're building strong habits. Keep the momentum going!"
            longestStreak >= 7 -> "💪 Nice streak! You're on the right track. Keep pushing forward!"
            completionRate >= 80 -> "🎯 Excellent consistency! You're completing most of your habits regularly."
            completionRate >= 50 -> "📈 Good progress! You're building consistency. Try to increase your completion rate."
            perfectDays >= 5 -> "✨ You've had $perfectDays perfect days! Consistency is key."
            perfectDays > 0 -> "⭐ You've had $perfectDays perfect day${if (perfectDays > 1) "s" else ""}! Aim for more!"
            else -> "🚀 Every journey starts with a single step. Keep going and build your consistency!"
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                return StatsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

