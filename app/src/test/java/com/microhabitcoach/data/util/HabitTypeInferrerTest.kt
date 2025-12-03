package com.microhabitcoach.data.util

import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitTypeInferrerTest {

    private fun createSuggestion(
        title: String = "Test",
        category: HabitCategory = HabitCategory.GENERAL,
        content: String? = null
    ) = ApiSuggestion(
        id = "test-1",
        title = title,
        content = content,
        category = category,
        fitScore = 0,
        source = "test",
        cachedAt = System.currentTimeMillis()
    )

    @Test
    fun inferType_motionKeywords_returnsMotion() {
        val motionTitles = listOf(
            "Morning walk",
            "Go for a run",
            "Cycling routine",
            "Gym workout",
            "Cardio exercise",
            "Hiking trail"
        )
        
        motionTitles.forEach { title ->
            val suggestion = createSuggestion(title = title)
            val result = HabitTypeInferrer.inferType(suggestion)
            assertEquals("Should infer '$title' as MOTION", HabitType.MOTION, result)
        }
    }

    @Test
    fun inferType_locationKeywords_returnsLocation() {
        val locationTitles = listOf(
            "Arrive at home",
            "Check in at gym",
            "Reach office",
            "Visit park",
            "Location reminder"
        )
        
        locationTitles.forEach { title ->
            val suggestion = createSuggestion(title = title)
            val result = HabitTypeInferrer.inferType(suggestion)
            assertEquals("Should infer '$title' as LOCATION", HabitType.LOCATION, result)
        }
    }

    @Test
    fun inferType_timeKeywords_returnsTime() {
        val timeTitles = listOf(
            "Daily meditation",
            "Morning routine",
            "Evening schedule",
            "Reminder alarm",
            "Time-based habit"
        )
        
        timeTitles.forEach { title ->
            val suggestion = createSuggestion(title = title)
            val result = HabitTypeInferrer.inferType(suggestion)
            assertEquals("Should infer '$title' as TIME", HabitType.TIME, result)
        }
    }

    @Test
    fun inferType_motionPriority_overLocation() {
        val suggestion = createSuggestion(title = "Walk at park")
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.MOTION, result)
    }

    @Test
    fun inferType_motionPriority_overTime() {
        val suggestion = createSuggestion(title = "Daily walk")
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.MOTION, result)
    }

    @Test
    fun inferType_locationPriority_overTime() {
        val suggestion = createSuggestion(title = "Daily at home")
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.LOCATION, result)
    }

    @Test
    fun inferType_fitnessCategory_fallbackToMotion() {
        val suggestion = createSuggestion(
            title = "Some activity",
            category = HabitCategory.FITNESS
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.MOTION, result)
    }

    @Test
    fun inferType_wellnessCategory_fallbackToTime() {
        val suggestion = createSuggestion(
            title = "Some activity",
            category = HabitCategory.WELLNESS
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.TIME, result)
    }

    @Test
    fun inferType_productivityCategory_fallbackToTime() {
        val suggestion = createSuggestion(
            title = "Some activity",
            category = HabitCategory.PRODUCTIVITY
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.TIME, result)
    }

    @Test
    fun inferType_learningCategory_fallbackToTime() {
        val suggestion = createSuggestion(
            title = "Some activity",
            category = HabitCategory.LEARNING
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.TIME, result)
    }

    @Test
    fun inferType_generalCategory_fallbackToTime() {
        val suggestion = createSuggestion(
            title = "Some activity",
            category = HabitCategory.GENERAL
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.TIME, result)
    }

    @Test
    fun inferType_withContent_usesBothTitleAndContent() {
        val suggestion = createSuggestion(
            title = "Activity",
            content = "This involves walking and running"
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.MOTION, result)
    }

    @Test
    fun suggestParameters_motionType_returnsMotionParams() {
        val suggestion = createSuggestion(title = "Morning walk")
        val type = HabitType.MOTION
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        assertTrue(params.containsKey("motionType"))
        assertTrue(params.containsKey("duration"))
        assertEquals(30, params["duration"])
    }

    @Test
    fun suggestParameters_locationType_returnsLocationParams() {
        val suggestion = createSuggestion(title = "Arrive at gym")
        val type = HabitType.LOCATION
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        assertTrue(params.containsKey("radius"))
        assertEquals(100f, params["radius"])
    }

    @Test
    fun suggestParameters_timeType_returnsTimeParams() {
        val suggestion = createSuggestion(title = "Daily reminder")
        val type = HabitType.TIME
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        assertTrue(params.containsKey("reminderTimes"))
        assertTrue(params.containsKey("reminderDays"))
    }

    @Test
    fun inferMotionType_runKeywords_returnsRun() {
        val suggestion = createSuggestion(title = "Go for a run")
        val type = HabitType.MOTION
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        val motionType = params["motionType"] as? String
        assertTrue(motionType?.contains("run", ignoreCase = true) == true || motionType == "Run")
    }

    @Test
    fun inferMotionType_walkKeywords_returnsWalk() {
        val suggestion = createSuggestion(title = "Morning walk")
        val type = HabitType.MOTION
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        val motionType = params["motionType"] as? String
        assertTrue(motionType?.contains("walk", ignoreCase = true) == true || motionType == "Walk")
    }

    @Test
    fun inferMotionType_noKeywords_defaultsToWalk() {
        val suggestion = createSuggestion(title = "Exercise")
        val type = HabitType.MOTION
        
        val params = HabitTypeInferrer.suggestParameters(suggestion, type)
        
        val motionType = params["motionType"] as? String
        assertEquals("Walk", motionType)
    }

    @Test
    fun inferType_caseInsensitive_matchesKeywords() {
        val suggestion1 = createSuggestion(title = "WALK")
        val suggestion2 = createSuggestion(title = "Walk")
        val suggestion3 = createSuggestion(title = "walk")
        
        assertEquals(HabitType.MOTION, HabitTypeInferrer.inferType(suggestion1))
        assertEquals(HabitType.MOTION, HabitTypeInferrer.inferType(suggestion2))
        assertEquals(HabitType.MOTION, HabitTypeInferrer.inferType(suggestion3))
    }

    @Test
    fun inferType_emptyTitle_fallbackToCategory() {
        val suggestion = createSuggestion(
            title = "",
            category = HabitCategory.FITNESS
        )
        val result = HabitTypeInferrer.inferType(suggestion)
        assertEquals(HabitType.MOTION, result)
    }
}

