package com.microhabitcoach.ui.habitdetail

import com.microhabitcoach.data.database.entity.Completion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class HabitDetailDataTest {

    private fun createCompletion() = Completion(
        id = UUID.randomUUID().toString(),
        habitId = "habit-1",
        completedAt = System.currentTimeMillis(),
        autoCompleted = false
    )

    @Test
    fun streakInfo_containsCurrentAndBestStreak() {
        val streakInfo = StreakInfo(currentStreak = 5, bestStreak = 10)
        
        assertEquals(5, streakInfo.currentStreak)
        assertEquals(10, streakInfo.bestStreak)
    }

    @Test
    fun completionStats_containsAllMetrics() {
        val stats = CompletionStats(
            sevenDayPercentage = 85.5,
            thirtyDayPercentage = 75.0,
            totalCompletions = 30,
            completionRate = 80.0
        )
        
        assertEquals(85.5, stats.sevenDayPercentage, 0.1)
        assertEquals(75.0, stats.thirtyDayPercentage, 0.1)
        assertEquals(30, stats.totalCompletions)
        assertEquals(80.0, stats.completionRate, 0.1)
    }

    @Test
    fun trendAnalysis_containsTrendData() {
        val trend = TrendAnalysis(
            isImproving = true,
            trendPercentage = 15.5,
            recentAverage = 5.0,
            previousAverage = 4.0
        )
        
        assertTrue(trend.isImproving)
        assertEquals(15.5, trend.trendPercentage, 0.1)
        assertEquals(5.0, trend.recentAverage, 0.1)
        assertEquals(4.0, trend.previousAverage, 0.1)
    }

    @Test
    fun bestDayInfo_containsDayData() {
        val bestDay = BestDayInfo(
            dayOfWeek = 1,
            dayName = "Monday",
            completionCount = 10,
            percentage = 25.0
        )
        
        assertEquals(1, bestDay.dayOfWeek)
        assertEquals("Monday", bestDay.dayName)
        assertEquals(10, bestDay.completionCount)
        assertEquals(25.0, bestDay.percentage, 0.1)
    }

    @Test
    fun calendarDayData_containsDayInfo() {
        val completion = createCompletion()
        val dayData = CalendarDayData(
            date = System.currentTimeMillis(),
            hasCompletion = true,
            completion = completion
        )
        
        assertTrue(dayData.hasCompletion)
        assertNotNull(dayData.completion)
        assertEquals(completion.id, dayData.completion?.id)
    }

    @Test
    fun calendarDayData_noCompletion() {
        val dayData = CalendarDayData(
            date = System.currentTimeMillis(),
            hasCompletion = false,
            completion = null
        )
        
        assertFalse(dayData.hasCompletion)
        assertNull(dayData.completion)
    }

    @Test
    fun completionHistoryItem_containsFormattedData() {
        val completion = createCompletion()
        val historyItem = CompletionHistoryItem(
            completion = completion,
            formattedDate = "Jan 1, 2024",
            formattedTime = "12:00 PM",
            isToday = true,
            isThisWeek = true
        )
        
        assertEquals(completion, historyItem.completion)
        assertEquals("Jan 1, 2024", historyItem.formattedDate)
        assertEquals("12:00 PM", historyItem.formattedTime)
        assertTrue(historyItem.isToday)
        assertTrue(historyItem.isThisWeek)
    }

    @Test
    fun habitDetailData_containsAllAnalytics() {
        val streakInfo = StreakInfo(5, 10)
        val completionStats = CompletionStats(85.0, 75.0, 30, 80.0)
        val trendAnalysis = TrendAnalysis(true, 15.0, 5.0, 4.0)
        val bestDay = BestDayInfo(1, "Monday", 10, 25.0)
        val calendarData = listOf(CalendarDayData(System.currentTimeMillis(), true, createCompletion()))
        val historyItems = listOf(
            CompletionHistoryItem(createCompletion(), "Jan 1", "12:00 PM", true, true)
        )
        
        val detailData = HabitDetailData(
            habitId = "habit-1",
            habitName = "Test Habit",
            streakInfo = streakInfo,
            completionStats = completionStats,
            trendAnalysis = trendAnalysis,
            bestDayInfo = bestDay,
            calendarData = calendarData,
            historyItems = historyItems
        )
        
        assertEquals("habit-1", detailData.habitId)
        assertEquals("Test Habit", detailData.habitName)
        assertEquals(5, detailData.streakInfo.currentStreak)
        assertEquals(30, detailData.completionStats.totalCompletions)
        assertNotNull(detailData.bestDayInfo)
        assertEquals(1, detailData.calendarData.size)
        assertEquals(1, detailData.historyItems.size)
    }

    @Test
    fun habitDetailData_nullableBestDay() {
        val detailData = HabitDetailData(
            habitId = "habit-1",
            habitName = "Test Habit",
            streakInfo = StreakInfo(0, 0),
            completionStats = CompletionStats(0.0, 0.0, 0, 0.0),
            trendAnalysis = TrendAnalysis(false, 0.0, 0.0, 0.0),
            bestDayInfo = null,
            calendarData = emptyList(),
            historyItems = emptyList()
        )
        
        assertNull(detailData.bestDayInfo)
    }
}

