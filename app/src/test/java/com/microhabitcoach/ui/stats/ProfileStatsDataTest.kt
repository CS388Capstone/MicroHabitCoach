package com.microhabitcoach.ui.stats

import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileStatsDataTest {

    @Test
    fun aggregateStats_containsAllMetrics() {
        val stats = AggregateStats(
            totalHabits = 5,
            totalCompletions = 100,
            longestStreak = 10,
            sevenDayCompletionRate = 85.5,
            thirtyDayCompletionRate = 75.0,
            overallCompletionRate = 80.0
        )
        
        assertEquals(5, stats.totalHabits)
        assertEquals(100, stats.totalCompletions)
        assertEquals(10, stats.longestStreak)
        assertEquals(85.5, stats.sevenDayCompletionRate, 0.1)
        assertEquals(75.0, stats.thirtyDayCompletionRate, 0.1)
        assertEquals(80.0, stats.overallCompletionRate, 0.1)
    }

    @Test
    fun categoryBreakdown_containsCategoryData() {
        val breakdown = CategoryBreakdown(
            category = HabitCategory.FITNESS,
            habitCount = 3,
            completionCount = 50,
            averageCompletionRate = 75.5
        )
        
        assertEquals(HabitCategory.FITNESS, breakdown.category)
        assertEquals(3, breakdown.habitCount)
        assertEquals(50, breakdown.completionCount)
        assertEquals(75.5, breakdown.averageCompletionRate, 0.1)
    }

    @Test
    fun heatmapDay_containsDayData() {
        val day = HeatmapDay(
            date = System.currentTimeMillis(),
            completionCount = 5,
            totalHabits = 10,
            completionRate = 50.0
        )
        
        assertTrue(day.date > 0)
        assertEquals(5, day.completionCount)
        assertEquals(10, day.totalHabits)
        assertEquals(50.0, day.completionRate, 0.1)
    }

    @Test
    fun weeklyHeatmapData_containsWeekData() {
        val days = (0..6).map {
            HeatmapDay(
                date = System.currentTimeMillis() - (it * 24 * 60 * 60 * 1000L),
                completionCount = it,
                totalHabits = 10,
                completionRate = (it * 10.0)
            )
        }
        
        val weekData = WeeklyHeatmapData(
            weekStartDate = System.currentTimeMillis(),
            days = days
        )
        
        assertEquals(7, weekData.days.size)
        assertTrue(weekData.weekStartDate > 0)
    }

    @Test
    fun mostConsistentHabit_containsHabitData() {
        val habit = MostConsistentHabit(
            habitId = "habit-1",
            habitName = "Consistent Habit",
            completionRate = 95.5,
            streakCount = 30,
            totalCompletions = 100
        )
        
        assertEquals("habit-1", habit.habitId)
        assertEquals("Consistent Habit", habit.habitName)
        assertEquals(95.5, habit.completionRate, 0.1)
        assertEquals(30, habit.streakCount)
        assertEquals(100, habit.totalCompletions)
    }

    @Test
    fun bestDayInfo_containsDayData() {
        val bestDay = BestDayInfo(
            dayOfWeek = 1,
            dayName = "Monday",
            completionCount = 20,
            averageCompletionsPerDay = 5.0
        )
        
        assertEquals(1, bestDay.dayOfWeek)
        assertEquals("Monday", bestDay.dayName)
        assertEquals(20, bestDay.completionCount)
        assertEquals(5.0, bestDay.averageCompletionsPerDay, 0.1)
    }

    @Test
    fun profileInsights_containsInsights() {
        val mostConsistent = MostConsistentHabit("habit-1", "Habit", 90.0, 10, 50)
        val bestDay = BestDayInfo(1, "Monday", 20, 5.0)
        
        val insights = ProfileInsights(
            mostConsistentHabit = mostConsistent,
            bestDay = bestDay
        )
        
        assertNotNull(insights.mostConsistentHabit)
        assertNotNull(insights.bestDay)
        assertEquals("Habit", insights.mostConsistentHabit?.habitName)
        assertEquals("Monday", insights.bestDay?.dayName)
    }

    @Test
    fun profileInsights_nullableInsights() {
        val insights = ProfileInsights(
            mostConsistentHabit = null,
            bestDay = null
        )
        
        assertNull(insights.mostConsistentHabit)
        assertNull(insights.bestDay)
    }

    @Test
    fun chartData_containsLabelsAndValues() {
        val chartData = ChartData(
            labels = listOf("Mon", "Tue", "Wed"),
            values = listOf(10.0, 20.0, 30.0),
            colors = listOf(0xFF0000, 0x00FF00, 0x0000FF)
        )
        
        assertEquals(3, chartData.labels.size)
        assertEquals(3, chartData.values.size)
        assertNotNull(chartData.colors)
        assertEquals(3, chartData.colors?.size ?: 0)
    }

    @Test
    fun chartData_nullableColors() {
        val chartData = ChartData(
            labels = listOf("A", "B"),
            values = listOf(1.0, 2.0),
            colors = null
        )
        
        assertEquals(2, chartData.labels.size)
        assertNull(chartData.colors)
    }

    @Test
    fun profileStatsData_containsAllData() {
        val aggregateStats = AggregateStats(5, 100, 10, 85.0, 75.0, 80.0)
        val categoryBreakdown = listOf(
            CategoryBreakdown(HabitCategory.FITNESS, 2, 50, 75.0)
        )
        val weeklyHeatmap = listOf(
            WeeklyHeatmapData(
                System.currentTimeMillis(),
                emptyList()
            )
        )
        val insights = ProfileInsights(null, null)
        val categoryChart = ChartData(listOf("Fitness"), listOf(75.0))
        val weeklyChart = ChartData(listOf("Week 1"), listOf(85.0))
        
        val statsData = ProfileStatsData(
            aggregateStats = aggregateStats,
            categoryBreakdown = categoryBreakdown,
            weeklyHeatmap = weeklyHeatmap,
            insights = insights,
            categoryChartData = categoryChart,
            weeklyTrendChartData = weeklyChart
        )
        
        assertEquals(5, statsData.aggregateStats.totalHabits)
        assertEquals(1, statsData.categoryBreakdown.size)
        assertEquals(1, statsData.weeklyHeatmap.size)
        assertNotNull(statsData.insights)
        assertEquals(1, statsData.categoryChartData.labels.size)
        assertEquals(1, statsData.weeklyTrendChartData.labels.size)
    }
}

