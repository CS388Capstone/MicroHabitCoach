package com.microhabitcoach.ui.onboarding

enum class OnboardingStep {
    WELCOME,
    TEMPLATES,
    PERMISSION_MOTION,
    PERMISSION_LOCATION,
    PERMISSION_NOTIFICATIONS;

    companion object {
        const val TOTAL_STEPS = 5

        fun fromPosition(position: Int): OnboardingStep {
            return values()[position.coerceIn(0, values().size - 1)]
        }
    }
}

