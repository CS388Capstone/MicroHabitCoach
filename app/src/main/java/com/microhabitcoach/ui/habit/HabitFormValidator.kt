package com.microhabitcoach.ui.habit

import com.microhabitcoach.data.model.HabitType
import java.time.LocalTime

/**
 * Stateless validation helpers for habit forms.
 */
object HabitFormValidator {
    data class Result(val valid: Boolean, val errors: List<String>)

    fun validate(
        name: String,
        type: HabitType,
        times: List<LocalTime>,
        days: List<Int>,
        motionType: String?,
        duration: Int?,
        hasLocation: Boolean,
        radius: Float?
    ): Result {
        val errors = mutableListOf<String>()
        if (name.isBlank()) errors += "Name required"
        when (type) {
            HabitType.TIME -> {
                if (times.isEmpty()) errors += "At least one time"
                if (days.isEmpty()) errors += "Select days"
            }
            HabitType.MOTION -> {
                if (motionType.isNullOrBlank()) errors += "Motion type required"
                if (duration == null || duration <= 0) errors += "Duration must be >0"
            }
            HabitType.LOCATION -> {
                if (!hasLocation) errors += "Location required"
                if (radius == null || radius <= 0f) errors += "Radius must be >0"
            }
        }
        return Result(errors.isEmpty(), errors)
    }
}
