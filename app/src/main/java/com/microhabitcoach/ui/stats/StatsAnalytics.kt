package com.microhabitcoach.ui.stats

import com.microhabitcoach.data.database.dao.CompletionDao
import com.microhabitcoach.data.database.dao.HabitDao
import com.microhabitcoach.data.database.entity.Completion
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Utility class for calculating aggregate statistics and analytics.
 */
class StatsAnalytics(
    private val habitDao: HabitDao,
    private val completionDao: CompletionDao
) {
    
    /**
     * Get total number of habits (active and inactive).
     */
    suspend fun getTotalHabits(): Int {
        return habitDao.getAllHabitsSync().size
    }
    
    /**
     * Get longest streak across all habits.
     */
    suspend fun getLongestStreak(): Int {
        val habits = habitDao.getAllHabitsSync()
        return habits.maxOfOrNull { it.streakCount } ?: 0
    }
    
    /**
     * Get total number of completions (all-time).
     */
    suspend fun getTotalCompletions(): Int {
        return completionDao.getAllCompletions().size
    }
    
    /**
     * Calculate overall completion rate for a given number of days.
     * Returns the average completion rate across all habits (more meaningful than total).
     * Only counts days since each habit was created.
     */
    suspend fun getOverallCompletionRate(days: Int): Double {
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return 0.0
        
        // Calculate average completion rate per habit
        var totalRate = 0.0
        var habitCount = 0
        
        habits.forEach { habit ->
            // Only count days since habit was created, not the full window
            val habitStartTime = maxOf(habit.createdAt, windowStartTime)
            val daysSinceCreation = ((endTime - habitStartTime) / (24 * 60 * 60 * 1000L)).toInt()
            
            if (daysSinceCreation > 0) {
                val completions = completionDao.getCompletionsInRange(habit.id, habitStartTime, endTime)
                // Count unique days with completions
                val calendar = Calendar.getInstance()
                val uniqueDays = completions.map { completion ->
                    calendar.timeInMillis = completion.completedAt
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }.distinct().size
                
                // Calculate as percentage of days completed since creation
                val habitRate = (uniqueDays.toDouble() / daysSinceCreation) * 100.0
                totalRate += habitRate.coerceIn(0.0, 100.0)
                habitCount++
            }
        }
        
        return if (habitCount > 0) {
            totalRate / habitCount
        } else {
            0.0
        }
    }
    
    /**
     * Get habit with highest completion rate (most consistent).
     * Completion rate is calculated as percentage of days completed (capped at 100%).
     * Only counts days since the habit was created.
     */
    suspend fun getMostConsistentHabit(days: Int = 30): MostConsistentHabit? {
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return null
        
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        var bestHabit: Habit? = null
        var bestRate = 0.0
        
        habits.forEach { habit ->
            // Only count days since habit was created, not the full window
            val habitStartTime = maxOf(habit.createdAt, windowStartTime)
            val daysSinceCreation = ((endTime - habitStartTime) / (24 * 60 * 60 * 1000L)).toInt()
            
            if (daysSinceCreation > 0) {
                val completions = completionDao.getCompletionsInRange(habit.id, habitStartTime, endTime)
                // Count unique days with completions
                val calendar = Calendar.getInstance()
                val uniqueDays = completions.map { completion ->
                    calendar.timeInMillis = completion.completedAt
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }.distinct().size
                
                // Calculate as percentage of days completed since creation
                val rate = (uniqueDays.toDouble() / daysSinceCreation) * 100.0
                
                if (rate > bestRate) {
                    bestRate = rate.coerceIn(0.0, 100.0)
                    bestHabit = habit
                }
            }
        }
        
        return bestHabit?.let { habit ->
            val habitStartTime = maxOf(habit.createdAt, windowStartTime)
            val completions = completionDao.getCompletionsInRange(habit.id, habitStartTime, endTime)
            val totalCompletions = completionDao.getCompletionCount(habit.id)
            MostConsistentHabit(
                habitId = habit.id,
                habitName = habit.name,
                completionRate = bestRate,
                streakCount = habit.streakCount,
                totalCompletions = totalCompletions
            )
        }
    }
    
    /**
     * Get best day of the week (day with most completions).
     */
    suspend fun getBestDayOfWeek(): BestDayInfo? {
        val calendar = Calendar.getInstance()
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (30 * 24 * 60 * 60 * 1000L) // Last 30 days
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        if (completions.isEmpty()) return null
        
        // Count completions by day of week (1=Monday, 7=Sunday)
        val dayCounts = IntArray(8) // Index 0 unused, 1-7 for days
        val dayOccurrences = IntArray(8)
        
        completions.forEach { completion ->
            calendar.timeInMillis = completion.completedAt
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Convert Calendar day (1=Sunday) to our format (1=Monday)
            val ourDay = when (dayOfWeek) {
                Calendar.SUNDAY -> 7
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 1
            }
            dayCounts[ourDay]++
        }
        
        // Count how many times each day appeared in the range
        calendar.timeInMillis = startTime
        while (calendar.timeInMillis < endTime) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val ourDay = when (dayOfWeek) {
                Calendar.SUNDAY -> 7
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 1
            }
            dayOccurrences[ourDay]++
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Find day with most completions
        var bestDay = 1
        var maxCompletions = dayCounts[1]
        for (i in 2..7) {
            if (dayCounts[i] > maxCompletions) {
                maxCompletions = dayCounts[i]
                bestDay = i
            }
        }
        
        val dayNames = listOf("", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val avgCompletions = if (dayOccurrences[bestDay] > 0) {
            maxCompletions.toDouble() / dayOccurrences[bestDay]
        } else {
            0.0
        }
        
        return BestDayInfo(
            dayOfWeek = bestDay,
            dayName = dayNames[bestDay],
            completionCount = maxCompletions,
            averageCompletionsPerDay = avgCompletions
        )
    }
    
    /**
     * Get habit with longest current streak.
     */
    suspend fun getCurrentStreakLeader(): Habit? {
        val habits = habitDao.getAllHabitsSync()
        return habits.maxByOrNull { it.streakCount }
    }
    
    /**
     * Get completion trend data for a line chart (last N days).
     */
    suspend fun getCompletionTrendData(days: Int): List<Pair<Long, Int>> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        
        // Group completions by day
        val calendar = Calendar.getInstance()
        val dayMap = mutableMapOf<Long, Int>()
        
        completions.forEach { completion ->
            calendar.timeInMillis = completion.completedAt
            // Set to start of day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            
            dayMap[dayStart] = dayMap.getOrDefault(dayStart, 0) + 1
        }
        
        // Fill in missing days with 0
        val result = mutableListOf<Pair<Long, Int>>()
        calendar.timeInMillis = startTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        for (i in 0 until days) {
            val dayStart = calendar.timeInMillis
            result.add(Pair(dayStart, dayMap.getOrDefault(dayStart, 0)))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return result
    }
    
    /**
     * Get category breakdown data for a pie chart.
     */
    suspend fun getCategoryBreakdownData(): Map<HabitCategory, Int> {
        val habits = habitDao.getAllHabitsSync()
        val categoryMap = mutableMapOf<HabitCategory, Int>()
        
        habits.forEach { habit ->
            categoryMap[habit.category] = categoryMap.getOrDefault(habit.category, 0) + 1
        }
        
        return categoryMap
    }
    
    /**
     * Get total days active (days with at least one completion).
     * Only counts days since the oldest habit was created.
     */
    suspend fun getTotalDaysActive(days: Int = 30): Int {
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return 0
        
        // Find the earliest habit creation time within the window
        val earliestCreation = habits.map { maxOf(it.createdAt, windowStartTime) }.minOrNull() ?: windowStartTime
        val startTime = maxOf(earliestCreation, windowStartTime)
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        val calendar = Calendar.getInstance()
        
        val uniqueDays = completions.map { completion ->
            calendar.timeInMillis = completion.completedAt
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.distinct().size
        
        return uniqueDays
    }
    
    /**
     * Get perfect days (days where all habits were completed).
     * Only counts days since all habits existed.
     */
    suspend fun getPerfectDays(days: Int = 30): Int {
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return 0
        
        // Only count days since all habits existed
        val latestCreation = habits.maxOfOrNull { it.createdAt } ?: windowStartTime
        val startTime = maxOf(latestCreation, windowStartTime)
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        val calendar = Calendar.getInstance()
        
        // Group completions by day
        val completionsByDay = completions.groupBy { completion ->
            calendar.timeInMillis = completion.completedAt
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        
        // Count days where all habits were completed
        var perfectDays = 0
        completionsByDay.forEach { (_, dayCompletions) ->
            val habitsCompleted = dayCompletions.map { it.habitId }.distinct().size
            if (habitsCompleted >= habits.size) {
                perfectDays++
            }
        }
        
        return perfectDays
    }
    
    /**
     * Get average completions per day.
     * Only counts days since the oldest habit was created.
     */
    suspend fun getAverageCompletionsPerDay(days: Int = 30): Double {
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (days * 24 * 60 * 60 * 1000L)
        
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return 0.0
        
        // Find the earliest habit creation time within the window
        val earliestCreation = habits.map { maxOf(it.createdAt, windowStartTime) }.minOrNull() ?: windowStartTime
        val startTime = maxOf(earliestCreation, windowStartTime)
        val daysSinceCreation = ((endTime - startTime) / (24 * 60 * 60 * 1000L)).toInt()
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        
        return if (daysSinceCreation > 0) {
            completions.size.toDouble() / daysSinceCreation
        } else {
            0.0
        }
    }
    
    /**
     * Get weekly heatmap data (last 4 weeks, 7 days each).
     */
    suspend fun getWeeklyHeatmapData(): Array<Array<Int>> {
        val calendar = Calendar.getInstance()
        val endTime = System.currentTimeMillis()
        
        // Go back 4 weeks
        calendar.timeInMillis = endTime
        calendar.add(Calendar.WEEK_OF_YEAR, -4)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        val completions = completionDao.getAllCompletionsInRange(startTime, endTime)
        
        // Initialize 7x4 grid (7 days, 4 weeks)
        val heatmap = Array(7) { Array(4) { 0 } }
        
        // Group completions by day
        val dayMap = mutableMapOf<Long, Int>()
        val tempCalendar = Calendar.getInstance()
        
        completions.forEach { completion ->
            tempCalendar.timeInMillis = completion.completedAt
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0)
            tempCalendar.set(Calendar.MINUTE, 0)
            tempCalendar.set(Calendar.SECOND, 0)
            tempCalendar.set(Calendar.MILLISECOND, 0)
            val dayStart = tempCalendar.timeInMillis
            
            dayMap[dayStart] = dayMap.getOrDefault(dayStart, 0) + 1
        }
        
        // Fill heatmap
        calendar.timeInMillis = startTime
        var week = 0
        var dayOfWeek = 0
        
        while (calendar.timeInMillis < endTime && week < 4) {
            val dayStart = calendar.timeInMillis
            val count = dayMap.getOrDefault(dayStart, 0)
            
            // Convert Calendar day (1=Sunday) to our format (0=Monday)
            val ourDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> 6
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 0
            }
            
            if (ourDay < 7 && week < 4) {
                heatmap[ourDay][week] = count
            }
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            dayOfWeek++
            if (dayOfWeek >= 7) {
                dayOfWeek = 0
                week++
            }
        }
        
        return heatmap
    }
    
    /**
     * Get top performing habits (sorted by completion rate).
     */
    suspend fun getTopPerformingHabits(limit: Int = 3): List<HabitPerformance> {
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return emptyList()
        
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (30 * 24 * 60 * 60 * 1000L)
        
        val performances = habits.map { habit ->
            val habitStartTime = maxOf(habit.createdAt, windowStartTime)
            val daysSinceCreation = ((endTime - habitStartTime) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
            
            val completions = completionDao.getCompletionsInRange(habit.id, habitStartTime, endTime)
            val calendar = Calendar.getInstance()
            val uniqueDays = completions.map { completion ->
                calendar.timeInMillis = completion.completedAt
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.distinct().size
            
            val completionRate = if (daysSinceCreation > 0) {
                (uniqueDays.toDouble() / daysSinceCreation) * 100.0
            } else {
                0.0
            }
            
            HabitPerformance(
                habitId = habit.id,
                habitName = habit.name,
                completionRate = completionRate.coerceIn(0.0, 100.0),
                streakCount = habit.streakCount,
                totalCompletions = completions.size,
                daysSinceCreation = daysSinceCreation
            )
        }
        
        return performances.sortedByDescending { it.completionRate }.take(limit)
    }
    
    /**
     * Get weekly comparison (this week vs last week).
     */
    suspend fun getWeeklyComparison(): WeeklyComparison {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        // This week (Monday to Sunday)
        calendar.timeInMillis = now
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val thisWeekStart = calendar.timeInMillis
        
        // Last week
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        val lastWeekStart = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val lastWeekEnd = calendar.timeInMillis
        
        val habits = habitDao.getAllHabitsSync()
        val totalHabits = habits.size
        
        val thisWeekCompletions = completionDao.getAllCompletionsInRange(thisWeekStart, now).size
        val lastWeekCompletions = completionDao.getAllCompletionsInRange(lastWeekStart, lastWeekEnd).size
        
        val thisWeekRate = if (totalHabits > 0 && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            val daysThisWeek = ((now - thisWeekStart) / (24 * 60 * 60 * 1000L)).toInt() + 1
            (thisWeekCompletions.toDouble() / (totalHabits * daysThisWeek)) * 100.0
        } else {
            0.0
        }
        
        val lastWeekRate = if (totalHabits > 0) {
            (lastWeekCompletions.toDouble() / (totalHabits * 7)) * 100.0
        } else {
            0.0
        }
        
        val changePercent = if (lastWeekCompletions > 0) {
            ((thisWeekCompletions - lastWeekCompletions).toDouble() / lastWeekCompletions) * 100.0
        } else {
            if (thisWeekCompletions > 0) 100.0 else 0.0
        }
        
        return WeeklyComparison(
            thisWeekCompletions = thisWeekCompletions,
            lastWeekCompletions = lastWeekCompletions,
            thisWeekRate = thisWeekRate.coerceIn(0.0, 100.0),
            lastWeekRate = lastWeekRate.coerceIn(0.0, 100.0),
            changePercent = changePercent
        )
    }
    
    /**
     * Calculate overall consistency score (0-100) with grade.
     * Based on consistency rates, not absolute numbers, so new habits aren't penalized.
     */
    suspend fun getConsistencyScore(): ConsistencyScore {
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) {
            return ConsistencyScore(
                score = 0,
                grade = "N/A",
                description = "Create your first habit to get started!"
            )
        }
        
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (30 * 24 * 60 * 60 * 1000L)
        
        // Check if all habits are very new (less than 3 days old)
        val latestCreation = habits.maxOfOrNull { it.createdAt } ?: windowStartTime
        val daysSinceAllHabits = ((endTime - latestCreation) / (24 * 60 * 60 * 1000L)).toInt()
        
        // If habits are very new (less than 3 days), use special handling
        if (daysSinceAllHabits < 3) {
            val totalCompletions = completionDao.getAllCompletions().size
            val hasAnyCompletions = totalCompletions > 0
            
            return if (hasAnyCompletions) {
                // If they've completed at least one habit, give them an encouraging score
                ConsistencyScore(
                    score = 75, // B+ grade for getting started
                    grade = "B+",
                    description = "Great start! You're building your habits. Keep it up!"
                )
            } else {
                // If no completions yet, show neutral/encouraging message
                ConsistencyScore(
                    score = 0,
                    grade = "—",
                    description = "Getting started! Complete your first habit to see your consistency score."
                )
            }
        }
        
        // Calculate average completion rate (already accounts for days since creation)
        val completionRate30 = getOverallCompletionRate(30)
        
        // Calculate average streak consistency (streak relative to days since creation)
        var totalStreakConsistency = 0.0
        var habitCount = 0
        habits.forEach { habit ->
            val habitStartTime = maxOf(habit.createdAt, windowStartTime)
            val daysSinceCreation = ((endTime - habitStartTime) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
            
            // Streak consistency: what % of days since creation is the current streak?
            val streakConsistency = if (daysSinceCreation > 0) {
                (habit.streakCount.toDouble() / daysSinceCreation) * 100.0
            } else {
                0.0
            }
            totalStreakConsistency += streakConsistency.coerceIn(0.0, 100.0)
            habitCount++
        }
        val avgStreakConsistency = if (habitCount > 0) totalStreakConsistency / habitCount else 0.0
        
        // Calculate perfect days rate (relative to days since all habits existed)
        val startTime = maxOf(latestCreation, windowStartTime)
        val daysSinceAllHabitsForCalc = ((endTime - startTime) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
        val perfectDays = getPerfectDays(30)
        val perfectDaysRate = if (daysSinceAllHabitsForCalc > 0) {
            (perfectDays.toDouble() / daysSinceAllHabitsForCalc) * 100.0
        } else {
            0.0
        }
        
        // Score calculation: 70% completion rate, 20% streak consistency, 10% perfect days rate
        // This heavily favors consistency over absolute numbers
        val rateScore = (completionRate30 * 0.7).coerceIn(0.0, 70.0)
        val streakScore = (avgStreakConsistency * 0.2).coerceIn(0.0, 20.0)
        val perfectDaysScore = (perfectDaysRate * 0.1).coerceIn(0.0, 10.0)
        
        val totalScore = (rateScore + streakScore + perfectDaysScore).toInt().coerceIn(0, 100)
        
        val (grade, description) = when {
            totalScore >= 90 -> "A+" to "Outstanding! You're incredibly consistent."
            totalScore >= 80 -> "A" to "Excellent! You're building strong habits."
            totalScore >= 70 -> "B+" to "Great work! Keep up the momentum."
            totalScore >= 60 -> "B" to "Good progress! You're on the right track."
            totalScore >= 50 -> "C+" to "Not bad! Room for improvement."
            totalScore >= 40 -> "C" to "Keep going! Every day counts."
            totalScore >= 30 -> "D" to "You can do better! Stay committed."
            else -> "F" to "Time to refocus! Start fresh today."
        }
        
        return ConsistencyScore(
            score = totalScore,
            grade = grade,
            description = description
        )
    }
    
    /**
     * Get category performance breakdown.
     */
    suspend fun getCategoryPerformance(): List<CategoryPerformance> {
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return emptyList()
        
        val endTime = System.currentTimeMillis()
        val windowStartTime = endTime - (30 * 24 * 60 * 60 * 1000L)
        
        val categoryMap = mutableMapOf<HabitCategory, MutableList<Habit>>()
        habits.forEach { habit ->
            categoryMap.getOrPut(habit.category) { mutableListOf() }.add(habit)
        }
        
        return categoryMap.map { (category, categoryHabits) ->
            var totalRate = 0.0
            var totalCompletions = 0
            var habitCount = 0
            
            categoryHabits.forEach { habit ->
                val habitStartTime = maxOf(habit.createdAt, windowStartTime)
                val daysSinceCreation = ((endTime - habitStartTime) / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(1)
                
                val completions = completionDao.getCompletionsInRange(habit.id, habitStartTime, endTime)
                totalCompletions += completions.size
                
                val calendar = Calendar.getInstance()
                val uniqueDays = completions.map { completion ->
                    calendar.timeInMillis = completion.completedAt
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }.distinct().size
                
                val habitRate = if (daysSinceCreation > 0) {
                    (uniqueDays.toDouble() / daysSinceCreation) * 100.0
                } else {
                    0.0
                }
                
                totalRate += habitRate
                habitCount++
            }
            
            val averageRate = if (habitCount > 0) {
                totalRate / habitCount
            } else {
                0.0
            }
            
            CategoryPerformance(
                category = category,
                habitCount = categoryHabits.size,
                averageCompletionRate = averageRate.coerceIn(0.0, 100.0),
                totalCompletions = totalCompletions
            )
        }.sortedByDescending { it.averageCompletionRate }
    }
}

