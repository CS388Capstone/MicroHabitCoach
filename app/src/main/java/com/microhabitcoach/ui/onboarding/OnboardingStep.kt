package com.microhabitcoach.ui.onboarding

enum class OnboardingStep {
    // Single, condensed onboarding step to keep the flow simple:
    // explains the app and then takes the user straight into Add Habit.
    WELCOME;

    companion object {
        const val TOTAL_STEPS = 1

        fun fromPosition(position: Int): OnboardingStep {
            return values()[position.coerceIn(0, values().size - 1)]
        }
    }
}



