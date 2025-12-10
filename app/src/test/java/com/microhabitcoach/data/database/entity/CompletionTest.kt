package com.microhabitcoach.data.database.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CompletionTest {

    @Test
    fun completion_creation_withDefaults_setsCorrectValues() {
        val habitId = UUID.randomUUID().toString()
        val completion = Completion(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            completedAt = System.currentTimeMillis()
        )

        assertEquals(habitId, completion.habitId)
        assertFalse(completion.autoCompleted)
        assertNull(completion.notes)
        assertTrue(completion.completedAt > 0)
    }

    @Test
    fun completion_autoCompleted_setsFlag() {
        val completion = Completion(
            id = UUID.randomUUID().toString(),
            habitId = UUID.randomUUID().toString(),
            completedAt = System.currentTimeMillis(),
            autoCompleted = true
        )

        assertTrue(completion.autoCompleted)
    }

    @Test
    fun completion_withNotes_setsNotes() {
        val completion = Completion(
            id = UUID.randomUUID().toString(),
            habitId = UUID.randomUUID().toString(),
            completedAt = System.currentTimeMillis(),
            notes = "Great workout!"
        )

        assertEquals("Great workout!", completion.notes)
    }

    @Test
    fun completion_copy_updatesFields() {
        val original = Completion(
            id = UUID.randomUUID().toString(),
            habitId = UUID.randomUUID().toString(),
            completedAt = 1000L,
            autoCompleted = false
        )

        val updated = original.copy(
            completedAt = 2000L,
            autoCompleted = true,
            notes = "Updated"
        )

        assertEquals(2000L, updated.completedAt)
        assertTrue(updated.autoCompleted)
        assertEquals("Updated", updated.notes)
        assertEquals(original.id, updated.id)
        assertEquals(original.habitId, updated.habitId)
    }

    @Test
    fun completion_equals_sameIdAndFields_returnsTrue() {
        val id = UUID.randomUUID().toString()
        val habitId = UUID.randomUUID().toString()
        val completedAt = 1000L
        val completion1 = Completion(
            id = id,
            habitId = habitId,
            completedAt = completedAt
        )
        val completion2 = Completion(
            id = id,
            habitId = habitId,
            completedAt = completedAt
        )

        assertEquals(completion1, completion2)
    }
    
    @Test
    fun completion_equals_sameIdDifferentFields_returnsFalse() {
        val id = UUID.randomUUID().toString()
        val completion1 = Completion(
            id = id,
            habitId = UUID.randomUUID().toString(),
            completedAt = 1000L
        )
        val completion2 = Completion(
            id = id,
            habitId = UUID.randomUUID().toString(),
            completedAt = 2000L
        )

        // Kotlin data classes compare ALL fields, not just ID
        assertFalse(completion1 == completion2)
    }

    @Test
    fun completion_equals_differentId_returnsFalse() {
        val completion1 = Completion(
            id = UUID.randomUUID().toString(),
            habitId = UUID.randomUUID().toString(),
            completedAt = 1000L
        )
        val completion2 = Completion(
            id = UUID.randomUUID().toString(),
            habitId = completion1.habitId,
            completedAt = 1000L
        )

        assertFalse(completion1 == completion2)
    }

    @Test
    fun completion_hashCode_sameFields_returnsSameHash() {
        val id = UUID.randomUUID().toString()
        val habitId = UUID.randomUUID().toString()
        val completedAt = 1000L
        val completion1 = Completion(
            id = id,
            habitId = habitId,
            completedAt = completedAt
        )
        val completion2 = Completion(
            id = id,
            habitId = habitId,
            completedAt = completedAt
        )

        assertEquals(completion1.hashCode(), completion2.hashCode())
    }
}

