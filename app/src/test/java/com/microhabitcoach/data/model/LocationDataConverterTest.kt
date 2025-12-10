package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationDataConverterTest {

    @Test
    fun fromLocationData_validLocation_returnsJsonString() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York, NY"
        )
        val result = LocationDataConverter.fromLocationData(location)
        assert(result != null)
        // Gson may serialize numbers differently, just verify it's valid JSON
        assert(result!!.startsWith("{") && result.endsWith("}"))
    }

    @Test
    fun fromLocationData_null_returnsNull() {
        val result = LocationDataConverter.fromLocationData(null)
        assertNull(result)
    }

    @Test
    fun fromLocationData_withoutAddress_returnsJsonString() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = null
        )
        val result = LocationDataConverter.fromLocationData(location)
        assert(result != null)
    }

    @Test
    fun toLocationData_validJsonString_returnsLocationData() {
        val json = """{"latitude":40.7128,"longitude":-74.0060,"address":"New York, NY"}"""
        val result = LocationDataConverter.toLocationData(json)
        assert(result != null)
        assertEquals(40.7128, result!!.latitude, 0.0001)
        assertEquals(-74.0060, result.longitude, 0.0001)
        assertEquals("New York, NY", result.address)
    }

    @Test
    fun toLocationData_null_returnsNull() {
        val result = LocationDataConverter.toLocationData(null)
        assertNull(result)
    }

    @Test
    fun toLocationData_withoutAddress_handlesNullAddress() {
        val json = """{"latitude":40.7128,"longitude":-74.0060}"""
        val result = LocationDataConverter.toLocationData(json)
        assert(result != null)
        assertEquals(40.7128, result!!.latitude, 0.0001)
        assertEquals(-74.0060, result.longitude, 0.0001)
        assertNull(result.address)
    }

    @Test
    fun roundTrip_conversion_preservesValue() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York, NY"
        )
        
        val jsonString = LocationDataConverter.fromLocationData(location)
        val convertedBack = LocationDataConverter.toLocationData(jsonString)
        
        assert(convertedBack != null)
        assertEquals(location.latitude, convertedBack!!.latitude, 0.0001)
        assertEquals(location.longitude, convertedBack.longitude, 0.0001)
        assertEquals(location.address, convertedBack.address)
    }

    @Test
    fun roundTrip_withoutAddress_preservesValue() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = null
        )
        
        val jsonString = LocationDataConverter.fromLocationData(location)
        val convertedBack = LocationDataConverter.toLocationData(jsonString)
        
        assert(convertedBack != null)
        assertEquals(location.latitude, convertedBack!!.latitude, 0.0001)
        assertEquals(location.longitude, convertedBack.longitude, 0.0001)
        assertNull(convertedBack.address)
    }

    @Test
    fun roundTrip_extremeCoordinates_handlesCorrectly() {
        val location = LocationData(
            latitude = 90.0,
            longitude = 180.0,
            address = "North Pole"
        )
        
        val jsonString = LocationDataConverter.fromLocationData(location)
        val convertedBack = LocationDataConverter.toLocationData(jsonString)
        
        assert(convertedBack != null)
        assertEquals(location.latitude, convertedBack!!.latitude, 0.0001)
        assertEquals(location.longitude, convertedBack.longitude, 0.0001)
    }
}

