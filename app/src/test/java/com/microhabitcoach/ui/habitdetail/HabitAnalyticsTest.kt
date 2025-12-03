package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.UUID

class HabitAnalyticsTest {

    private fun createCompletion(
        habitId: String = "habit-1",
        daysAgo: Int = 0,
        hoursOffset: Int = 0
    ): Completion {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        calendar.add(Calendar.HOUR_OF_DAY, -hoursOffset)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return Completion(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            completedAt = calendar.timeInMillis,
            autoCompleted = false
        )
    }

    @Test
    fun calculateStreaks_emptyCompletions_returnsZero() {
        val result = HabitAnalytics.calculateStreaks(emptyList())
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.bestStreak)
    }

    @Test
    fun calculateStreaks_singleCompletion_returnsOne() {
        val completions = listOf(createCompletion(daysAgo = 0))
        val result = HabitAnalytics.calculateStreaks(completions)
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.bestStreak)
    }

    @Test
    fun calculateStreaks_consecutiveDays_calculatesCurrentStreak() {
        val completions = listOf(
            createCompletion(daysAgo = 2),
            createCompletion(daysAgo = 1),
            createCompletion(daysAgo = 0) // Today
        )
        val result = HabitAnalytics.calculateStreaks(completions)
        assertEquals(3, result.currentStreak)
        assertEquals(3, result.bestStreak)
    }

    @Test
    fun calculateStreaks_brokenStreak_calculatesBestStreak() {
        val completions = listOf(
            createCompletion(daysAgo = 5),
            createCompletion(daysAgo = 4),
            createCompletion(daysAgo = 3),
            // Gap
            createCompletion(daysAgo = 1),
            createCompletion(daysAgo = 0)
        )
        val result = HabitAnalytics.calculateStreaks(completions)
        assertEquals(2, result.currentStreak) // Last 2 days
        assertEquals(3, result.bestStreak) // Best was 3 days
    }

    @Test
    fun calculateStreaks_yesterdayCompleted_calculatesFromYesterday() {
        val completions = listOf(
            createCompletion(daysAgo = 2),
            createCompletion(daysAgo = 1) // Yesterday, not today
        )
        val result = HabitAnalytics.calculateStreaks(completions)
        assertEquals(2, result.currentStreak)
    }

    @Test
    fun calculateCompletionStats_emptyCompletions_returnsZero() {
        val habitCreatedAt = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L)
        val result = HabitAnalytics.calculateCompletionStats(emptyList(), habitCreatedAt)
        
        assertEquals(0.0, result.sevenDayPercentage, 0.1)
        assertEquals(0.0, result.thirtyDayPercentage, 0.1)
        assertEquals(0, result.totalCompletions)
        assertEquals(0.0, result.completionRate, 0.1)
    }

    @Test
    fun calculateCompletionStats_allDaysCompleted_returns100Percent() {
        val habitCreatedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val completions = (0..6).map { createCompletion(daysAgo = it) }
        
        val result = HabitAnalytics.calculateCompletionStats(completions, habitCreatedAt)
        
        assertTrue(result.sevenDayPercentage >= 100.0)
        assertTrue(result.completionRate > 0.0)
    }

    @Test
    fun calculateCompletionStats_halfDaysCompleted_returns50Percent() {
        val habitCreatedAt = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val completions = listOf(
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 2),
            createCompletion(daysAgo = 4),
            createCompletion(daysAgo = 6)
        )
        
        val result = HabitAnalytics.calculateCompletionStats(completions, habitCreatedAt)
        
        // Approximately 50% (4 out of 7 days)
        assertTrue(result.sevenDayPercentage >= 50.0)
    }

    @Test
    fun calculateCompletionStats_oldHabit_limitsTo30Days() {
        val habitCreatedAt = System.currentTimeMillis() - (60 * 24 * 60 * 60 * 1000L)
        val completions = (0..29).map { createCompletion(daysAgo = it) }
        
        val result = HabitAnalytics.calculateCompletionStats(completions, habitCreatedAt)
        
        assertTrue(result.thirtyDayPercentage >= 100.0)
    }

    @Test
    fun analyzeTrend_improvingTrend_returnsImproving() {
        val now = System.currentTimeMillis()
        val completions = listOf(
            // Recent period (last 7 days) - 5 completions
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 1),
            createCompletion(daysAgo = 2),
            createCompletion(daysAgo = 3),
            createCompletion(daysAgo = 4),
            // Previous period (7-14 days ago) - 2 completions
            createCompletion(daysAgo = 10),
            createCompletion(daysAgo = 12)
        )
        
        val result = HabitAnalytics.analyzeTrend(completions)
        
        assertTrue(result.isImproving)
        assertTrue(result.trendPercentage > 0.0)
    }

    @Test
    fun analyzeTrend_decliningTrend_returnsNotImproving() {
        val completions = listOf(
            // Recent period - 2 completions
            createCompletion(daysAgo = 1),
            createCompletion(daysAgo = 3),
            // Previous period - 5 completions
            createCompletion(daysAgo = 10),
            createCompletion(daysAgo = 11),
            createCompletion(daysAgo = 12),
            createCompletion(daysAgo = 13),
            createCompletion(daysAgo = 14)
        )
        
        val result = HabitAnalytics.analyzeTrend(completions)
        
        assertTrue(!result.isImproving)
        assertTrue(result.trendPercentage < 0.0)
    }

    @Test
    fun analyzeTrend_noPreviousData_handlesGracefully() {
        val completions = listOf(
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 1)
        )
        
        val result = HabitAnalytics.analyzeTrend(completions)
        
        assertNotNull(result)
        assertTrue(result.recentAverage >= 0.0)
    }

    @Test
    fun findBestDay_emptyCompletions_returnsNull() {
        val result = HabitAnalytics.findBestDay(emptyList())
        assertNull(result)
    }

    @Test
    fun findBestDay_multipleCompletions_findsMostFrequentDay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val mondayTime = calendar.timeInMillis
        
        val completions = listOf(
            Completion(UUID.randomUUID().toString(), "habit-1", mondayTime, false),
            Completion(UUID.randomUUID().toString(), "habit-1", mondayTime + (24 * 60 * 60 * 1000L), false),
            Completion(UUID.randomUUID().toString(), "habit-1", mondayTime + (2 * 24 * 60 * 60 * 1000L), false),
            Completion(UUID.randomUUID().toString(), "habit-1", mondayTime + (7 * 24 * 60 * 60 * 1000L), false) // Next Monday
        )
        
        val result = HabitAnalytics.findBestDay(completions)
        
        assertNotNull(result)
        assertEquals("Monday", result?.dayName)
        assertEquals(4, result?.completionCount)
    }

    @Test
    fun formatCompletionHistory_formatsCorrectly() {
        val completions = listOf(
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 3)
        )
        
        val result = HabitAnalytics.formatCompletionHistory(completions)
        
        assertEquals(2, result.size)
        assertTrue(result[0].isToday)
        assertTrue(result[0].isThisWeek)
        assertNotNull(result[0].formattedDate)
        assertNotNull(result[0].formattedTime)
    }

    @Test
    fun createCalendarData_creates30Days() {
        val completions = listOf(
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 5),
            createCompletion(daysAgo = 15)
        )
        
        val result = HabitAnalytics.createCalendarData(completions)
        
        assertTrue(result.size <= 30)
        assertTrue(result.any { it.hasCompletion })
    }

    @Test
    fun createCalendarData_oldCompletions_excludesBeyond30Days() {
        val completions = listOf(
            createCompletion(daysAgo = 0),
            createCompletion(daysAgo = 35) // Should be excluded
        )
        
        val result = HabitAnalytics.createCalendarData(completions)
        
        val hasOldCompletion = result.any { it.date < System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) }
        assertTrue(!hasOldCompletion)
    }
}

