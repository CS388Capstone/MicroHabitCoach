package com.microhabitcoach.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitCategoryConverterTest {

    @Test
    fun fromHabitCategory_validCategory_returnsName() {
        val result = HabitCategoryConverter.fromHabitCategory(HabitCategory.FITNESS)
        assertEquals("FITNESS", result)
    }

    @Test
    fun fromHabitCategory_null_returnsNull() {
        val result = HabitCategoryConverter.fromHabitCategory(null)
        assertNull(result)
    }

    @Test
    fun fromHabitCategory_allCategories_returnsCorrectNames() {
        HabitCategory.values().forEach { category ->
            val result = HabitCategoryConverter.fromHabitCategory(category)
            assertEquals(category.name, result)
        }
    }

    @Test
    fun toHabitCategory_validString_returnsCategory() {
        val result = HabitCategoryConverter.toHabitCategory("FITNESS")
        assertEquals(HabitCategory.FITNESS, result)
    }

    @Test
    fun toHabitCategory_null_returnsNull() {
        val result = HabitCategoryConverter.toHabitCategory(null)
        assertNull(result)
    }

    @Test
    fun toHabitCategory_allCategoryNames_returnsCorrectCategories() {
        HabitCategory.values().forEach { category ->
            val result = HabitCategoryConverter.toHabitCategory(category.name)
            assertEquals(category, result)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun toHabitCategory_invalidString_throwsException() {
        HabitCategoryConverter.toHabitCategory("INVALID_CATEGORY")
    }

    @Test
    fun roundTrip_conversion_preservesValue() {
        HabitCategory.values().forEach { category ->
            val stringValue = HabitCategoryConverter.fromHabitCategory(category)
            val convertedBack = HabitCategoryConverter.toHabitCategory(stringValue)
            assertEquals(category, convertedBack)
        }
    }
}

