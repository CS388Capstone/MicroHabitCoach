package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntListConverterTest {

    @Test
    fun fromIntList_validList_returnsJsonString() {
        val ints = listOf(1, 2, 3, 4, 5)
        val result = IntListConverter.fromIntList(ints)
        assert(result != null)
        assert(result!!.contains("1"))
        assert(result.contains("2"))
    }

    @Test
    fun fromIntList_null_returnsNull() {
        val result = IntListConverter.fromIntList(null)
        assertNull(result)
    }

    @Test
    fun fromIntList_emptyList_returnsJsonString() {
        val result = IntListConverter.fromIntList(emptyList())
        assert(result != null)
        assertEquals("[]", result)
    }

    @Test
    fun fromIntList_singleItem_returnsJsonString() {
        val ints = listOf(42)
        val result = IntListConverter.fromIntList(ints)
        assert(result != null)
        assert(result!!.contains("42"))
    }

    @Test
    fun fromIntList_largeNumbers_handlesCorrectly() {
        val ints = listOf(Int.MAX_VALUE, Int.MIN_VALUE, 0)
        val result = IntListConverter.fromIntList(ints)
        assert(result != null)
    }

    @Test
    fun toIntList_validJsonString_returnsList() {
        val json = "[1,2,3,4,5]"
        val result = IntListConverter.toIntList(json)
        assert(result != null)
        assertEquals(5, result!!.size)
        assertEquals(1, result[0])
        assertEquals(5, result[4])
    }

    @Test
    fun toIntList_null_returnsNull() {
        val result = IntListConverter.toIntList(null)
        assertNull(result)
    }

    @Test
    fun toIntList_emptyArray_returnsEmptyList() {
        val result = IntListConverter.toIntList("[]")
        assert(result != null)
        assertEquals(0, result!!.size)
    }

    @Test
    fun toIntList_singleItem_returnsSingleItemList() {
        val json = "[42]"
        val result = IntListConverter.toIntList(json)
        assert(result != null)
        assertEquals(1, result!!.size)
        assertEquals(42, result[0])
    }

    @Test
    fun toIntList_negativeNumbers_handlesCorrectly() {
        val json = "[-1,-2,0,1,2]"
        val result = IntListConverter.toIntList(json)
        assert(result != null)
        assertEquals(5, result!!.size)
        assertEquals(-1, result[0])
        assertEquals(0, result[2])
    }

    @Test
    fun roundTrip_conversion_preservesValue() {
        val ints = listOf(1, 2, 3, 4, 5, 0, -1, Int.MAX_VALUE)
        val jsonString = IntListConverter.fromIntList(ints)
        val convertedBack = IntListConverter.toIntList(jsonString)
        
        assertEquals(ints.size, convertedBack!!.size)
        ints.forEachIndexed { index, value ->
            assertEquals(value, convertedBack[index])
        }
    }

    @Test
    fun roundTrip_emptyList_preservesValue() {
        val emptyList = emptyList<Int>()
        val jsonString = IntListConverter.fromIntList(emptyList)
        val convertedBack = IntListConverter.toIntList(jsonString)
        
        assert(convertedBack != null)
        assertEquals(0, convertedBack!!.size)
    }
}

