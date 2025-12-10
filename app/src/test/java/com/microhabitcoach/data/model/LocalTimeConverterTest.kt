package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class LocalTimeConverterTest {

    @Test
    fun fromLocalTime_validTime_returnsString() {
        val time = LocalTime.of(9, 30)
        val result = LocalTimeConverter.fromLocalTime(time)
        assertEquals("09:30", result)
    }

    @Test
    fun fromLocalTime_null_returnsNull() {
        val result = LocalTimeConverter.fromLocalTime(null)
        assertNull(result)
    }

    @Test
    fun fromLocalTime_midnight_returnsCorrectString() {
        val time = LocalTime.MIDNIGHT
        val result = LocalTimeConverter.fromLocalTime(time)
        assertEquals("00:00", result)
    }

    @Test
    fun fromLocalTime_endOfDay_returnsCorrectString() {
        val time = LocalTime.of(23, 59)
        val result = LocalTimeConverter.fromLocalTime(time)
        assertEquals("23:59", result)
    }

    @Test
    fun toLocalTime_validString_returnsLocalTime() {
        val result = LocalTimeConverter.toLocalTime("09:30")
        assertEquals(LocalTime.of(9, 30), result)
    }

    @Test
    fun toLocalTime_null_returnsNull() {
        val result = LocalTimeConverter.toLocalTime(null)
        assertNull(result)
    }

    @Test
    fun toLocalTime_midnightString_returnsMidnight() {
        val result = LocalTimeConverter.toLocalTime("00:00")
        assertEquals(LocalTime.MIDNIGHT, result)
    }

    @Test
    fun toLocalTime_withSeconds_parsesCorrectly() {
        val result = LocalTimeConverter.toLocalTime("09:30:45")
        assertEquals(LocalTime.of(9, 30, 45), result)
    }

    @Test(expected = Exception::class)
    fun toLocalTime_invalidString_throwsException() {
        LocalTimeConverter.toLocalTime("invalid")
    }

    @Test
    fun roundTrip_conversion_preservesValue() {
        val times = listOf(
            LocalTime.of(9, 0),
            LocalTime.of(12, 30),
            LocalTime.of(18, 45),
            LocalTime.MIDNIGHT,
            LocalTime.of(23, 59)
        )
        
        times.forEach { time ->
            val stringValue = LocalTimeConverter.fromLocalTime(time)
            val convertedBack = LocalTimeConverter.toLocalTime(stringValue)
            assertEquals(time, convertedBack)
        }
    }
}

