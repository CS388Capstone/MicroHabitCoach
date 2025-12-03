package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Utility class for calculating habit analytics.
 */
object HabitAnalytics {
    
    /**
     * Calculates current and best streak from completion history.
     */
    fun calculateStreaks(completions: List<Completion>): StreakInfo {
        if (completions.isEmpty()) {
            return StreakInfo(0, 0)
        }
        
        // Sort completions by date (oldest first)
        val sortedCompletions = completions.sortedBy { it.completedAt }
        
        // Group completions by day
        val completionsByDay = sortedCompletions.groupBy { completion ->
            getDayStart(completion.completedAt)
        }
        
        val daysWithCompletions = completionsByDay.keys.sorted()
        
        if (daysWithCompletions.isEmpty()) {
            return StreakInfo(0, 0)
        }
        
        // Calculate current streak (from today backwards)
        val today = getDayStart(System.currentTimeMillis())
        var currentStreak = 0
        var checkDate = today
        
        // Check if today is completed
        if (daysWithCompletions.contains(checkDate)) {
            currentStreak = 1
            checkDate = getPreviousDay(checkDate)
            
            // Continue checking backwards
            while (daysWithCompletions.contains(checkDate)) {
                currentStreak++
                checkDate = getPreviousDay(checkDate)
            }
        } else {
            // Check yesterday
            checkDate = getPreviousDay(today)
            if (daysWithCompletions.contains(checkDate)) {
                currentStreak = 1
                checkDate = getPreviousDay(checkDate)
                
                // Continue checking backwards
                while (daysWithCompletions.contains(checkDate)) {
                    currentStreak++
                    checkDate = getPreviousDay(checkDate)
                }
            }
        }
        
        // Calculate best streak
        var bestStreak = 0
        var currentRun = 0
        var previousDay: Long? = null
        
        for (day in daysWithCompletions) {
            if (previousDay == null || day == getNextDay(previousDay)) {
                // Consecutive day
                currentRun++
            } else {
                // Streak broken
                bestStreak = maxOf(bestStreak, currentRun)
                currentRun = 1
            }
            previousDay = day
        }
        bestStreak = maxOf(bestStreak, currentRun)
        
        return StreakInfo(currentStreak, bestStreak)
    }
    
    /**
     * Calculates completion percentages for 7-day and 30-day periods.
     */
    fun calculateCompletionStats(
        completions: List<Completion>,
        habitCreatedAt: Long
    ): CompletionStats {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000L)
        
        // Calculate 7-day stats
        val sevenDayCompletions = completions.count { 
            it.completedAt >= sevenDaysAgo 
        }
        val sevenDayDays = minOf(7, daysBetween(habitCreatedAt, now))
        val sevenDayPercentage = if (sevenDayDays > 0) {
            (sevenDayCompletions.toDouble() / sevenDayDays) * 100.0
        } else 0.0
        
        // Calculate 30-day stats
        val thirtyDayCompletions = completions.count { 
            it.completedAt >= thirtyDaysAgo 
        }
        val thirtyDayDays = minOf(30, daysBetween(habitCreatedAt, now))
        val thirtyDayPercentage = if (thirtyDayDays > 0) {
            (thirtyDayCompletions.toDouble() / thirtyDayDays) * 100.0
        } else 0.0
        
        // Calculate overall completion rate
        val totalDays = daysBetween(habitCreatedAt, now)
        val totalCompletions = completions.size
        val completionRate = if (totalDays > 0) {
            (totalCompletions.toDouble() / totalDays) * 100.0
        } else 0.0
        
        return CompletionStats(
            sevenDayPercentage = sevenDayPercentage.coerceIn(0.0, 100.0),
            thirtyDayPercentage = thirtyDayPercentage.coerceIn(0.0, 100.0),
            totalCompletions = totalCompletions,
            completionRate = completionRate.coerceIn(0.0, 100.0)
        )
    }
    
    /**
     * Analyzes trend by comparing recent period to previous period.
     */
    fun analyzeTrend(completions: List<Completion>): TrendAnalysis {
        val now = System.currentTimeMillis()
        val fourteenDaysAgo = now - (14 * 24 * 60 * 60 * 1000L)
        val twentyEightDaysAgo = now - (28 * 24 * 60 * 60 * 1000L)
        
        // Recent period (last 7 days)
        val recentCompletions = completions.filter { 
            it.completedAt >= fourteenDaysAgo && it.completedAt < now 
        }
        val recentAverage = recentCompletions.size / 7.0
        
        // Previous period (7-14 days ago)
        val previousCompletions = completions.filter { 
            it.completedAt >= twentyEightDaysAgo && it.completedAt < fourteenDaysAgo 
        }
        val previousAverage = previousCompletions.size / 7.0
        
        val isImproving = recentAverage > previousAverage
        val trendPercentage = if (previousAverage > 0) {
            ((recentAverage - previousAverage) / previousAverage) * 100.0
        } else if (recentAverage > 0) {
            100.0
        } else {
            0.0
        }
        
        return TrendAnalysis(
            isImproving = isImproving,
            trendPercentage = trendPercentage,
            recentAverage = recentAverage,
            previousAverage = previousAverage
        )
    }
    
    /**
     * Finds the best day of the week for completions.
     */
    fun findBestDay(completions: List<Completion>): BestDayInfo? {
        if (completions.isEmpty()) return null
        
        // Group completions by day of week (1=Monday, 7=Sunday)
        val completionsByDay = completions.groupBy { completion ->
            getDayOfWeek(completion.completedAt)
        }
        
        // Find day with most completions
        val bestDayEntry = completionsByDay.maxByOrNull { it.value.size }
            ?: return null
        
        val dayOfWeek = bestDayEntry.key
        val completionCount = bestDayEntry.value.size
        val totalCompletions = completions.size
        val percentage = (completionCount.toDouble() / totalCompletions) * 100.0
        
        val dayNames = listOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
        
        return BestDayInfo(
            dayOfWeek = dayOfWeek,
            dayName = dayNames[dayOfWeek - 1],
            completionCount = completionCount,
            percentage = percentage
        )
    }
    
    /**
     * Formats completion history for list view.
     */
    fun formatCompletionHistory(completions: List<Completion>): List<CompletionHistoryItem> {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val today = getDayStart(now)
        
        val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        return completions.map { completion ->
            val completionDay = getDayStart(completion.completedAt)
            val isToday = completionDay == today
            val isThisWeek = completion.completedAt >= weekAgo
            
            CompletionHistoryItem(
                completion = completion,
                formattedDate = dateFormatter.format(completion.completedAt),
                formattedTime = timeFormatter.format(completion.completedAt),
                isToday = isToday,
                isThisWeek = isThisWeek
            )
        }
    }
    
    /**
     * Creates calendar data for the last 30 days.
     */
    fun createCalendarData(completions: List<Completion>): List<CalendarDayData> {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000L)
        
        // Group completions by day
        val completionsByDay = completions
            .filter { it.completedAt >= thirtyDaysAgo }
            .groupBy { getDayStart(it.completedAt) }
        
        // Create data for each day in the last 30 days
        val calendarData = mutableListOf<CalendarDayData>()
        var currentDay = getDayStart(thirtyDaysAgo)
        val firstDayOfWeek = getDayOfWeek(currentDay) // 1 = Mon ... 7 = Sun
        val today = getDayStart(now)
        
        // Add leading placeholders so the first real day aligns with its weekday column
        repeat(firstDayOfWeek - 1) {
            calendarData.add(
                CalendarDayData(
                    date = 0L,
                    hasCompletion = false,
                    completion = null,
                    isPlaceholder = true
                )
            )
        }
        
        while (currentDay <= today) {
            val dayCompletions = completionsByDay[currentDay]
            calendarData.add(
                CalendarDayData(
                    date = currentDay,
                    hasCompletion = dayCompletions != null && dayCompletions.isNotEmpty(),
                    completion = dayCompletions?.firstOrNull()
                )
            )
            currentDay = getNextDay(currentDay)
        }
        
        // Keep chronological order (oldest to newest) so weekdays line up with header
        return calendarData
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
    
    private fun getPreviousDay(dayStart: Long): Long {
        return dayStart - (24 * 60 * 60 * 1000L)
    }
    
    private fun getNextDay(dayStart: Long): Long {
        return dayStart + (24 * 60 * 60 * 1000L)
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
}

