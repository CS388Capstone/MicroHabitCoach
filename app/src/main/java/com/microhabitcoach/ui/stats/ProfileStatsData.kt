package com.microhabitcoach.ui.stats

import com.microhabitcoach.data.model.HabitCategory

/**
 * Data class for aggregate statistics across all habits.
 */
data class AggregateStats(
    val totalHabits: Int,
    val totalCompletions: Int,
    val longestStreak: Int,
    val sevenDayCompletionRate: Double,
    val thirtyDayCompletionRate: Double,
    val overallCompletionRate: Double
)

/**
 * Data class for category breakdown statistics.
 */
data class CategoryBreakdown(
    val category: HabitCategory,
    val habitCount: Int,
    val completionCount: Int,
    val averageCompletionRate: Double
)

/**
 * Data class for weekly heatmap data (7 days x weeks).
 */
data class WeeklyHeatmapData(
    val weekStartDate: Long, // Timestamp for the start of the week
    val days: List<HeatmapDay> // 7 days of data
)

/**
 * Data class for a single day in the heatmap.
 */
data class HeatmapDay(
    val date: Long, // Timestamp for the day
    val completionCount: Int, // Number of habits completed on this day
    val totalHabits: Int, // Total active habits on this day
    val completionRate: Double // Percentage of habits completed
)

/**
 * Data class for insights.
 */
data class ProfileInsights(
    val mostConsistentHabit: MostConsistentHabit?,
    val bestDay: BestDayInfo?
)

/**
 * Data class for most consistent habit insight.
 */
data class MostConsistentHabit(
    val habitId: String,
    val habitName: String,
    val completionRate: Double,
    val streakCount: Int,
    val totalCompletions: Int
)

/**
 * Data class for best day of week insight.
 */
data class BestDayInfo(
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val dayName: String,
    val completionCount: Int,
    val averageCompletionsPerDay: Double
)

/**
 * Data class for chart data (formatted for visualization).
 */
data class ChartData(
    val labels: List<String>,
    val values: List<Double>,
    val colors: List<Int>? = null // Optional colors for each data point
)

/**
 * Complete profile statistics data.
 */
data class ProfileStatsData(
    val aggregateStats: AggregateStats,
    val categoryBreakdown: List<CategoryBreakdown>,
    val weeklyHeatmap: List<WeeklyHeatmapData>,
    val insights: ProfileInsights,
    val categoryChartData: ChartData,
    val weeklyTrendChartData: ChartData
)

