package com.microhabitcoach.ui.stats

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
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ProfileStatsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = DatabaseModule.getDatabase(application)
    private val habitDao = database.habitDao()
    private val completionDao = database.completionDao()

    // LiveData observables
    private val _statsData = MutableLiveData<ProfileStatsData?>()
    val statsData: LiveData<ProfileStatsData?> = _statsData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    /**
     * Loads all statistics and calculates analytics.
     */
    fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load all habits and completions
                val habitsList = withContext(Dispatchers.IO) {
                    habitDao.getAllHabits().first()
                }

                val completions = withContext(Dispatchers.IO) {
                    completionDao.getAllCompletions()
                }

                // Calculate all statistics
                val stats = withContext(Dispatchers.Default) {
                    calculateProfileStats(habitsList, completions)
                }

                _statsData.value = stats
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Failed to load statistics: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculates all profile statistics from habits and completions.
     */
    private fun calculateProfileStats(
        habits: List<Habit>,
        allCompletions: List<Completion>
    ): ProfileStatsData {
        if (habits.isEmpty()) {
            return createEmptyStats()
        }

        // Calculate aggregate statistics
        val aggregateStats = calculateAggregateStats(habits, allCompletions)

        // Calculate category breakdown
        val categoryBreakdown = calculateCategoryBreakdown(habits, allCompletions)

        // Calculate weekly heatmap data
        val weeklyHeatmap = calculateWeeklyHeatmap(habits, allCompletions)

        // Calculate insights
        val insights = calculateInsights(habits, allCompletions)

        // Format chart data
        val categoryChartData = formatCategoryChartData(categoryBreakdown)
        val weeklyTrendChartData = formatWeeklyTrendChartData(weeklyHeatmap)

        return ProfileStatsData(
            aggregateStats = aggregateStats,
            categoryBreakdown = categoryBreakdown,
            weeklyHeatmap = weeklyHeatmap,
            insights = insights,
            categoryChartData = categoryChartData,
            weeklyTrendChartData = weeklyTrendChartData
        )
    }

    /**
     * Calculates aggregate statistics across all habits.
     */
    private fun calculateAggregateStats(
        habits: List<Habit>,
        allCompletions: List<Completion>
    ): AggregateStats {
        val totalHabits = habits.size
        val totalCompletions = allCompletions.size

        // Find longest streak across all habits
        val longestStreak = habits.maxOfOrNull { it.streakCount } ?: 0

        // Calculate completion rates
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000L)

        // Get oldest habit creation date
        val oldestHabitDate = habits.minOfOrNull { it.createdAt } ?: now

        // Calculate 7-day completion rate
        val sevenDayCompletions = allCompletions.count { it.completedAt >= sevenDaysAgo }
        val sevenDayDays = minOf(7, daysBetween(oldestHabitDate, now))
        val sevenDayCompletionRate = if (sevenDayDays > 0 && totalHabits > 0) {
            // Average completions per day / total habits
            (sevenDayCompletions.toDouble() / (sevenDayDays * totalHabits)) * 100.0
        } else 0.0

        // Calculate 30-day completion rate
        val thirtyDayCompletions = allCompletions.count { it.completedAt >= thirtyDaysAgo }
        val thirtyDayDays = minOf(30, daysBetween(oldestHabitDate, now))
        val thirtyDayCompletionRate = if (thirtyDayDays > 0 && totalHabits > 0) {
            (thirtyDayCompletions.toDouble() / (thirtyDayDays * totalHabits)) * 100.0
        } else 0.0

        // Calculate overall completion rate
        val totalDays = daysBetween(oldestHabitDate, now)
        val overallCompletionRate = if (totalDays > 0 && totalHabits > 0) {
            (totalCompletions.toDouble() / (totalDays * totalHabits)) * 100.0
        } else 0.0

        return AggregateStats(
            totalHabits = totalHabits,
            totalCompletions = totalCompletions,
            longestStreak = longestStreak,
            sevenDayCompletionRate = sevenDayCompletionRate.coerceIn(0.0, 100.0),
            thirtyDayCompletionRate = thirtyDayCompletionRate.coerceIn(0.0, 100.0),
            overallCompletionRate = overallCompletionRate.coerceIn(0.0, 100.0)
        )
    }

    /**
     * Calculates category breakdown statistics.
     */
    private fun calculateCategoryBreakdown(
        habits: List<Habit>,
        allCompletions: List<Completion>
    ): List<CategoryBreakdown> {
        val categories = HabitCategory.values()
        
        return categories.map { category ->
            val categoryHabits = habits.filter { it.category == category }
            val categoryHabitIds = categoryHabits.map { it.id }.toSet()
            val categoryCompletions = allCompletions.filter { it.habitId in categoryHabitIds }
            
            // Calculate average completion rate for this category
            val habitCompletionRates = categoryHabits.map { habit ->
                val habitCompletions = categoryCompletions.filter { it.habitId == habit.id }
                val habitDays = daysBetween(habit.createdAt, System.currentTimeMillis())
                if (habitDays > 0) {
                    (habitCompletions.size.toDouble() / habitDays) * 100.0
                } else 0.0
            }
            
            val averageCompletionRate = if (habitCompletionRates.isNotEmpty()) {
                habitCompletionRates.average()
            } else 0.0

            CategoryBreakdown(
                category = category,
                habitCount = categoryHabits.size,
                completionCount = categoryCompletions.size,
                averageCompletionRate = averageCompletionRate.coerceIn(0.0, 100.0)
            )
        }.filter { it.habitCount > 0 } // Only include categories with habits
    }

    /**
     * Calculates weekly heatmap data for the last 4 weeks.
     */
    private fun calculateWeeklyHeatmap(
        habits: List<Habit>,
        allCompletions: List<Completion>
    ): List<WeeklyHeatmapData> {
        val now = System.currentTimeMillis()
        val fourWeeksAgo = now - (28 * 24 * 60 * 60 * 1000L)
        
        // Group completions by day
        val completionsByDay = allCompletions
            .filter { it.completedAt >= fourWeeksAgo }
            .groupBy { getDayStart(it.completedAt) }
        
        // Group habits by when they were active
        val habitsByDay = mutableMapOf<Long, Int>()
        habits.forEach { habit ->
            val habitStart = getDayStart(habit.createdAt)
            var currentDay = maxOf(habitStart, fourWeeksAgo)
            val today = getDayStart(now)
            
            while (currentDay <= today) {
                habitsByDay[currentDay] = (habitsByDay[currentDay] ?: 0) + 1
                currentDay = getNextDay(currentDay)
            }
        }
        
        // Create weekly data
        val weeklyData = mutableListOf<WeeklyHeatmapData>()
        var weekStart = getWeekStart(fourWeeksAgo)
        val today = getDayStart(now)
        
        while (weekStart <= today) {
            val weekEnd = weekStart + (6 * 24 * 60 * 60 * 1000L)
            val days = mutableListOf<HeatmapDay>()
            
            var currentDay = weekStart
            while (currentDay <= weekEnd && currentDay <= today) {
                val dayCompletions = completionsByDay[currentDay] ?: emptyList()
                val totalHabitsOnDay = habitsByDay[currentDay] ?: habits.size
                val completionCount = dayCompletions.size
                val completionRate = if (totalHabitsOnDay > 0) {
                    (completionCount.toDouble() / totalHabitsOnDay) * 100.0
                } else 0.0
                
                days.add(
                    HeatmapDay(
                        date = currentDay,
                        completionCount = completionCount,
                        totalHabits = totalHabitsOnDay,
                        completionRate = completionRate.coerceIn(0.0, 100.0)
                    )
                )
                currentDay = getNextDay(currentDay)
            }
            
            weeklyData.add(
                WeeklyHeatmapData(
                    weekStartDate = weekStart,
                    days = days
                )
            )
            
            weekStart = getNextDay(weekEnd)
        }
        
        return weeklyData.reversed() // Most recent first
    }

    /**
     * Calculates insights: most consistent habit and best day.
     */
    private fun calculateInsights(
        habits: List<Habit>,
        allCompletions: List<Completion>
    ): ProfileInsights {
        // Find most consistent habit
        val mostConsistentHabit = habits.mapNotNull { habit ->
            val habitCompletions = allCompletions.filter { it.habitId == habit.id }
            if (habitCompletions.isEmpty()) return@mapNotNull null
            
            val habitDays = daysBetween(habit.createdAt, System.currentTimeMillis())
            val completionRate = if (habitDays > 0) {
                (habitCompletions.size.toDouble() / habitDays) * 100.0
            } else 0.0
            
            MostConsistentHabit(
                habitId = habit.id,
                habitName = habit.name,
                completionRate = completionRate.coerceIn(0.0, 100.0),
                streakCount = habit.streakCount,
                totalCompletions = habitCompletions.size
            )
        }.maxByOrNull { it.completionRate }

        // Find best day of week
        val completionsByDayOfWeek = allCompletions.groupBy { completion ->
            getDayOfWeek(completion.completedAt)
        }
        
        val bestDayEntry = completionsByDayOfWeek.maxByOrNull { it.value.size }
        val bestDay = bestDayEntry?.let { entry ->
            val dayOfWeek = entry.key
            val dayNames = listOf(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"
            )
            
            // Calculate average completions per occurrence of this day
            val totalWeeks = maxOf(1, daysBetween(
                habits.minOfOrNull { it.createdAt } ?: System.currentTimeMillis(),
                System.currentTimeMillis()
            ) / 7)
            
            BestDayInfo(
                dayOfWeek = dayOfWeek,
                dayName = dayNames[dayOfWeek - 1],
                completionCount = entry.value.size,
                averageCompletionsPerDay = entry.value.size.toDouble() / totalWeeks
            )
        }

        return ProfileInsights(
            mostConsistentHabit = mostConsistentHabit,
            bestDay = bestDay
        )
    }

    /**
     * Formats category breakdown data for chart visualization.
     */
    private fun formatCategoryChartData(
        categoryBreakdown: List<CategoryBreakdown>
    ): ChartData {
        val labels = categoryBreakdown.map { it.category.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
        val values = categoryBreakdown.map { it.averageCompletionRate }
        
        return ChartData(
            labels = labels,
            values = values
        )
    }

    /**
     * Formats weekly trend data for chart visualization.
     */
    private fun formatWeeklyTrendChartData(
        weeklyHeatmap: List<WeeklyHeatmapData>
    ): ChartData {
        val labels = weeklyHeatmap.map { week ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = week.weekStartDate
            }
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            "$month/$day"
        }
        
        val values = weeklyHeatmap.map { week ->
            val weekAverage = if (week.days.isNotEmpty()) {
                week.days.map { it.completionRate }.average()
            } else 0.0
            weekAverage
        }
        
        return ChartData(
            labels = labels,
            values = values
        )
    }

    /**
     * Creates empty stats data when no habits exist.
     */
    private fun createEmptyStats(): ProfileStatsData {
        return ProfileStatsData(
            aggregateStats = AggregateStats(
                totalHabits = 0,
                totalCompletions = 0,
                longestStreak = 0,
                sevenDayCompletionRate = 0.0,
                thirtyDayCompletionRate = 0.0,
                overallCompletionRate = 0.0
            ),
            categoryBreakdown = emptyList(),
            weeklyHeatmap = emptyList(),
            insights = ProfileInsights(null, null),
            categoryChartData = ChartData(emptyList(), emptyList()),
            weeklyTrendChartData = ChartData(emptyList(), emptyList())
        )
    }

    /**
     * Refreshes the statistics.
     */
    fun refresh() {
        loadStats()
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }

    // Helper functions

    private fun getDayStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getNextDay(dayStart: Long): Long {
        return dayStart + (24 * 60 * 60 * 1000L)
    }

    private fun getWeekStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Get to Monday (Calendar.MONDAY = 2)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> -1
            Calendar.WEDNESDAY -> -2
            Calendar.THURSDAY -> -3
            Calendar.FRIDAY -> -4
            Calendar.SATURDAY -> -5
            Calendar.SUNDAY -> -6
            else -> 0
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysFromMonday)
        return calendar.timeInMillis
    }

    private fun getDayOfWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        // Convert to Monday=1, Sunday=7
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun daysBetween(start: Long, end: Long): Int {
        val diff = end - start
        return (diff / (24 * 60 * 60 * 1000L)).toInt() + 1 // +1 to include both start and end days
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileStatsViewModel::class.java)) {
                return ProfileStatsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

