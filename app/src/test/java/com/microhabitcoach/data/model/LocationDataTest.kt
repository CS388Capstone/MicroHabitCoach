package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationDataTest {

    @Test
    fun locationData_creation_withAllFields_setsCorrectly() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York, NY"
        )

        assertEquals(40.7128, location.latitude, 0.0001)
        assertEquals(-74.0060, location.longitude, 0.0001)
        assertEquals("New York, NY", location.address)
    }

    @Test
    fun locationData_creation_withoutAddress_setsNullAddress() {
        val location = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = null
        )

        assertEquals(40.7128, location.latitude, 0.0001)
        assertEquals(-74.0060, location.longitude, 0.0001)
        assertNull(location.address)
    }

    @Test
    fun locationData_copy_updatesFields() {
        val original = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Original"
        )

        val updated = original.copy(
            latitude = 41.8781,
            longitude = -87.6298,
            address = "Chicago, IL"
        )

        assertEquals(41.8781, updated.latitude, 0.0001)
        assertEquals(-87.6298, updated.longitude, 0.0001)
        assertEquals("Chicago, IL", updated.address)
    }

    @Test
    fun locationData_equals_sameValues_returnsTrue() {
        val location1 = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York"
        )
        val location2 = LocationData(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York"
        )

        assertEquals(location1, location2)
    }

    @Test
    fun locationData_extremeCoordinates_handlesCorrectly() {
        val northPole = LocationData(
            latitude = 90.0,
            longitude = 0.0,
            address = "North Pole"
        )
        val southPole = LocationData(
            latitude = -90.0,
            longitude = 0.0,
            address = "South Pole"
        )

        assertEquals(90.0, northPole.latitude, 0.0001)
        assertEquals(-90.0, southPole.latitude, 0.0001)
    }
}

