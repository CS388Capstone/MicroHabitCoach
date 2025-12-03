package com.microhabitcoach.data.util

import com.microhabitcoach.data.model.HabitCategory

/**
 * Classifies habit suggestions based on keyword matching.
 * No AI/ML required - simple keyword-based classification.
 */
object HabitClassifier {
    
    private val fitnessKeywords = listOf(
        "workout", "exercise", "gym", "run", "walk", "jog", "squat", "pushup",
        "cardio", "strength", "fitness", "training", "bike", "cycling", "swim",
        "swimming", "yoga", "pilates", "dance", "hike", "hiking", "sprint",
        "marathon", "weight", "lifting", "aerobics", "calisthenics", "stretch"
    )
    
    private val wellnessKeywords = listOf(
        "meditate", "meditation", "breath", "breathing", "yoga", "stretch",
        "sleep", "water", "hydrate", "hydration", "mindfulness", "relax",
        "relaxation", "stress", "anxiety", "mental", "health", "wellness",
        "self-care", "therapy", "calm", "peace", "zen", "mindful"
    )
    
    private val productivityKeywords = listOf(
        "read", "reading", "study", "learn", "practice", "focus", "pomodoro",
        "productivity", "organize", "plan", "schedule", "task", "goal",
        "achieve", "complete", "finish", "work", "project", "time management",
        "efficiency", "optimize", "improve", "develop", "build"
    )
    
    private val learningKeywords = listOf(
        "learn", "learning", "course", "tutorial", "skill", "education",
        "teach", "teaching", "knowledge", "study", "research", "explore",
        "discover", "understand", "master", "expert", "expertise", "certification",
        "degree", "certificate", "training", "workshop", "seminar"
    )
    
    /**
     * Classifies a suggestion based on its title and optional content.
     * 
     * @param title The title of the suggestion
     * @param content Optional content/description
     * @return The classified HabitCategory
     */
    fun classify(title: String, content: String? = null): HabitCategory {
        val text = (title + " " + (content ?: "")).lowercase()
        
        // Check fitness keywords first (most specific)
        val fitnessMatch = fitnessKeywords.firstOrNull { text.contains(it) }
        if (fitnessMatch != null) {
            android.util.Log.d("HabitClassifier", "Classified as FITNESS: '$title' (matched: $fitnessMatch)")
            return HabitCategory.FITNESS
        }
        
        // Check wellness keywords
        val wellnessMatch = wellnessKeywords.firstOrNull { text.contains(it) }
        if (wellnessMatch != null) {
            android.util.Log.d("HabitClassifier", "Classified as WELLNESS: '$title' (matched: $wellnessMatch)")
            return HabitCategory.WELLNESS
        }
        
        // Check productivity keywords
        val productivityMatch = productivityKeywords.firstOrNull { text.contains(it) }
        if (productivityMatch != null) {
            android.util.Log.d("HabitClassifier", "Classified as PRODUCTIVITY: '$title' (matched: $productivityMatch)")
            return HabitCategory.PRODUCTIVITY
        }
        
        // Check learning keywords
        val learningMatch = learningKeywords.firstOrNull { text.contains(it) }
        if (learningMatch != null) {
            android.util.Log.d("HabitClassifier", "Classified as LEARNING: '$title' (matched: $learningMatch)")
            return HabitCategory.LEARNING
        }
        
        // Default to general if no match
        android.util.Log.d("HabitClassifier", "Classified as GENERAL: '$title' (no keyword match)")
        return HabitCategory.GENERAL
    }
}

