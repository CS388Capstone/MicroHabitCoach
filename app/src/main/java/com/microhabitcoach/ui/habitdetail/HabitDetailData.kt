package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import java.util.Calendar

/**
 * Data class for streak information.
 */
data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int
)

/**
 * Data class for completion statistics.
 */
data class CompletionStats(
    val sevenDayPercentage: Double,
    val thirtyDayPercentage: Double,
    val totalCompletions: Int,
    val completionRate: Double // Overall completion rate
)

/**
 * Data class for trend analysis.
 */
data class TrendAnalysis(
    val isImproving: Boolean,
    val trendPercentage: Double, // Positive if improving, negative if declining
    val recentAverage: Double, // Average completions per day in recent period
    val previousAverage: Double // Average completions per day in previous period
)

/**
 * Data class for best day of week analysis.
 */
data class BestDayInfo(
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val dayName: String,
    val completionCount: Int,
    val percentage: Double
)

/**
 * Data class for calendar view data.
 */
data class CalendarDayData(
    val date: Long, // Timestamp for the day
    val hasCompletion: Boolean,
    val completion: Completion? = null
)

/**
 * Data class for list view data (completion history).
 */
data class CompletionHistoryItem(
    val completion: Completion,
    val formattedDate: String,
    val formattedTime: String,
    val isToday: Boolean,
    val isThisWeek: Boolean
)

/**
 * Complete habit detail data.
 */
data class HabitDetailData(
    val habitId: String,
    val habitName: String,
    val streakInfo: StreakInfo,
    val completionStats: CompletionStats,
    val trendAnalysis: TrendAnalysis,
    val bestDayInfo: BestDayInfo?,
    val calendarData: List<CalendarDayData>,
    val historyItems: List<CompletionHistoryItem>
)

