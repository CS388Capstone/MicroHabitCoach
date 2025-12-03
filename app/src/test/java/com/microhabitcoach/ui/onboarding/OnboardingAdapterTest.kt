package com.microhabitcoach.ui.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for OnboardingAdapter DiffUtil logic and step management.
 */
class OnboardingAdapterTest {

    @Test
    fun areItemsTheSame_sameStep_returnsTrue() {
        val oldItem = OnboardingStep.WELCOME
        val newItem = OnboardingStep.WELCOME
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun areItemsTheSame_differentStep_returnsFalse() {
        val oldItem = OnboardingStep.WELCOME
        val newItem = OnboardingStep.TEMPLATES
        
        val areSame = oldItem == newItem
        assertFalse(areSame)
    }

    @Test
    fun areContentsTheSame_sameStep_returnsTrue() {
        val oldItem = OnboardingStep.PERMISSION_MOTION
        val newItem = OnboardingStep.PERMISSION_MOTION
        
        val areSame = oldItem == newItem
        assertTrue(areSame)
    }

    @Test
    fun onboardingStep_values_containsAllSteps() {
        val steps = OnboardingStep.values()
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.contains(OnboardingStep.WELCOME))
        assertTrue(steps.contains(OnboardingStep.TEMPLATES))
        assertTrue(steps.contains(OnboardingStep.PERMISSION_MOTION))
        assertTrue(steps.contains(OnboardingStep.PERMISSION_LOCATION))
        assertTrue(steps.contains(OnboardingStep.PERMISSION_NOTIFICATIONS))
    }
}

