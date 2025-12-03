package com.microhabitcoach.data.util

import com.microhabitcoach.data.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitClassifierTest {

    @Test
    fun classify_fitnessKeywords_returnsFitness() {
        val fitnessTitles = listOf(
            "Morning workout routine",
            "Go for a run",
            "Gym session",
            "Cardio exercise",
            "Yoga practice",
            "Swimming lessons"
        )
        
        fitnessTitles.forEach { title ->
            val result = HabitClassifier.classify(title)
            assertEquals("Should classify '$title' as FITNESS", HabitCategory.FITNESS, result)
        }
    }

    @Test
    fun classify_wellnessKeywords_returnsWellness() {
        val wellnessTitles = listOf(
            "Meditation practice",
            "Breathing exercises",
            "Sleep hygiene",
            "Mindfulness routine",
            "Stress management"
        )
        
        wellnessTitles.forEach { title ->
            val result = HabitClassifier.classify(title)
            assertEquals("Should classify '$title' as WELLNESS", HabitCategory.WELLNESS, result)
        }
    }

    @Test
    fun classify_productivityKeywords_returnsProductivity() {
        val productivityTitles = listOf(
            "Read daily",
            "Study schedule",
            "Focus time",
            "Task management",
            "Time management"
        )
        
        productivityTitles.forEach { title ->
            val result = HabitClassifier.classify(title)
            assertEquals("Should classify '$title' as PRODUCTIVITY", HabitCategory.PRODUCTIVITY, result)
        }
    }

    @Test
    fun classify_learningKeywords_returnsLearning() {
        val learningTitles = listOf(
            "Learn new skill",
            "Take course",
            "Study tutorial",
            "Master expertise",
            "Education goal"
        )
        
        learningTitles.forEach { title ->
            val result = HabitClassifier.classify(title)
            assertEquals("Should classify '$title' as LEARNING", HabitCategory.LEARNING, result)
        }
    }

    @Test
    fun classify_noKeywords_returnsGeneral() {
        val generalTitles = listOf(
            "Random activity",
            "Something to do",
            "New thing",
            ""
        )
        
        generalTitles.forEach { title ->
            val result = HabitClassifier.classify(title)
            assertEquals("Should classify '$title' as GENERAL", HabitCategory.GENERAL, result)
        }
    }

    @Test
    fun classify_withContent_usesBothTitleAndContent() {
        val result = HabitClassifier.classify(
            title = "Daily activity",
            content = "This involves going to the gym and working out"
        )
        assertEquals(HabitCategory.FITNESS, result)
    }

    @Test
    fun classify_nullContent_handlesGracefully() {
        val result = HabitClassifier.classify(
            title = "Morning workout",
            content = null
        )
        assertEquals(HabitCategory.FITNESS, result)
    }

    @Test
    fun classify_emptyString_returnsGeneral() {
        val result = HabitClassifier.classify("")
        assertEquals(HabitCategory.GENERAL, result)
    }

    @Test
    fun classify_caseInsensitive_matchesKeywords() {
        val result1 = HabitClassifier.classify("WORKOUT ROUTINE")
        val result2 = HabitClassifier.classify("Workout Routine")
        val result3 = HabitClassifier.classify("workout routine")
        
        assertEquals(HabitCategory.FITNESS, result1)
        assertEquals(HabitCategory.FITNESS, result2)
        assertEquals(HabitCategory.FITNESS, result3)
    }

    @Test
    fun classify_categoryPriority_fitnessOverWellness() {
        // "yoga" appears in both fitness and wellness keywords
        // Fitness should take priority
        val result = HabitClassifier.classify("Yoga practice")
        assertEquals(HabitCategory.FITNESS, result)
    }

    @Test
    fun classify_categoryPriority_fitnessOverProductivity() {
        val result = HabitClassifier.classify("Exercise and focus")
        assertEquals(HabitCategory.FITNESS, result)
    }

    @Test
    fun classify_categoryPriority_wellnessOverProductivity() {
        val result = HabitClassifier.classify("Meditation and reading")
        assertEquals(HabitCategory.WELLNESS, result)
    }

    @Test
    fun classify_categoryPriority_productivityOverLearning() {
        val result = HabitClassifier.classify("Study and learn")
        assertEquals(HabitCategory.PRODUCTIVITY, result)
    }
}

