package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitTypeConverterTest {

    @Test
    fun fromHabitType_validType_returnsName() {
        val result = HabitTypeConverter.fromHabitType(HabitType.TIME)
        assertEquals("TIME", result)
    }

    @Test
    fun fromHabitType_null_returnsNull() {
        val result = HabitTypeConverter.fromHabitType(null)
        assertNull(result)
    }

    @Test
    fun fromHabitType_allTypes_returnsCorrectNames() {
        HabitType.values().forEach { type ->
            val result = HabitTypeConverter.fromHabitType(type)
            assertEquals(type.name, result)
        }
    }

    @Test
    fun toHabitType_validString_returnsType() {
        val result = HabitTypeConverter.toHabitType("MOTION")
        assertEquals(HabitType.MOTION, result)
    }

    @Test
    fun toHabitType_null_returnsNull() {
        val result = HabitTypeConverter.toHabitType(null)
        assertNull(result)
    }

    @Test
    fun toHabitType_allTypeNames_returnsCorrectTypes() {
        HabitType.values().forEach { type ->
            val result = HabitTypeConverter.toHabitType(type.name)
            assertEquals(type, result)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun toHabitType_invalidString_throwsException() {
        HabitTypeConverter.toHabitType("INVALID_TYPE")
    }

    @Test
    fun roundTrip_conversion_preservesValue() {
        HabitType.values().forEach { type ->
            val stringValue = HabitTypeConverter.fromHabitType(type)
            val convertedBack = HabitTypeConverter.toHabitType(stringValue)
            assertEquals(type, convertedBack)
        }
    }
}

