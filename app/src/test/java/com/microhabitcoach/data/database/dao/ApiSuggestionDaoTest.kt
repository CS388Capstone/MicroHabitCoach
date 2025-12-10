package com.microhabitcoach.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ApiSuggestionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ApiSuggestionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.apiSuggestionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertSuggestion_insertsSuccessfully() = runTest {
        val suggestion = createTestSuggestion()
        dao.insertSuggestion(suggestion)

        val retrieved = dao.getSuggestionById(suggestion.id)
        assertEquals(suggestion.id, retrieved?.id)
        assertEquals(suggestion.title, retrieved?.title)
    }

    @Test
    fun insertSuggestions_insertsMultipleSuccessfully() = runTest {
        val suggestions = listOf(
            createTestSuggestion(id = "1", fitScore = 80),
            createTestSuggestion(id = "2", fitScore = 90),
            createTestSuggestion(id = "3", fitScore = 70)
        )
        dao.insertSuggestions(suggestions)

        val all = dao.getAllSuggestions().first()
        assertEquals(3, all.size)
    }

    @Test
    fun getAllSuggestions_ordersByFitScoreDesc() = runTest {
        val suggestions = listOf(
            createTestSuggestion(id = "1", fitScore = 70),
            createTestSuggestion(id = "2", fitScore = 90),
            createTestSuggestion(id = "3", fitScore = 80)
        )
        dao.insertSuggestions(suggestions)

        val all = dao.getAllSuggestions().first()
        assertEquals("2", all[0].id) // Highest score first
        assertEquals("3", all[1].id)
        assertEquals("1", all[2].id)
    }

    @Test
    fun getSuggestionById_existing_returnsSuggestion() = runTest {
        val suggestion = createTestSuggestion()
        dao.insertSuggestion(suggestion)

        val retrieved = dao.getSuggestionById(suggestion.id)
        assertEquals(suggestion.id, retrieved?.id)
    }

    @Test
    fun getSuggestionById_nonExistent_returnsNull() = runTest {
        val retrieved = dao.getSuggestionById("non-existent")
        assertNull(retrieved)
    }

    @Test
    fun getValidSuggestions_returnsOnlyNonExpired() = runTest {
        val now = System.currentTimeMillis()
        val valid = createTestSuggestion(id = "valid", expiresAt = now + 10000)
        val expired = createTestSuggestion(id = "expired", expiresAt = now - 1000)
        val noExpiry = createTestSuggestion(id = "no-expiry", expiresAt = null)
        
        dao.insertSuggestions(listOf(valid, expired, noExpiry))

        val validSuggestions = dao.getValidSuggestions(now)
        assertEquals(2, validSuggestions.size) // valid + no-expiry
        assert(validSuggestions.any { it.id == "valid" })
        assert(validSuggestions.any { it.id == "no-expiry" })
    }

    @Test
    fun getValidSuggestions_respectsLimit() = runTest {
        val now = System.currentTimeMillis()
        val suggestions = (1..10).map { 
            createTestSuggestion(id = "$it", fitScore = 100 - it, expiresAt = now + 10000)
        }
        dao.insertSuggestions(suggestions)

        val valid = dao.getValidSuggestions(now, limit = 5)
        assertEquals(5, valid.size)
    }

    @Test
    fun getSuggestionsByMinScore_returnsOnlyAboveThreshold() = runTest {
        val suggestions = listOf(
            createTestSuggestion(id = "1", fitScore = 60),
            createTestSuggestion(id = "2", fitScore = 80),
            createTestSuggestion(id = "3", fitScore = 70)
        )
        dao.insertSuggestions(suggestions)

        val highScore = dao.getSuggestionsByMinScore(75).first()
        assertEquals(1, highScore.size)
        assertEquals("2", highScore.first().id)
    }

    @Test
    fun deleteSuggestion_deletesSuccessfully() = runTest {
        val suggestion = createTestSuggestion()
        dao.insertSuggestion(suggestion)
        dao.deleteSuggestion(suggestion)

        val retrieved = dao.getSuggestionById(suggestion.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteSuggestionById_deletesSuccessfully() = runTest {
        val suggestion = createTestSuggestion()
        dao.insertSuggestion(suggestion)
        dao.deleteSuggestionById(suggestion.id)

        val retrieved = dao.getSuggestionById(suggestion.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteOldSuggestions_deletesOnlyOld() = runTest {
        val old = createTestSuggestion(id = "old", cachedAt = 1000L)
        val new = createTestSuggestion(id = "new", cachedAt = System.currentTimeMillis())
        
        dao.insertSuggestions(listOf(old, new))
        dao.deleteOldSuggestions(5000L)

        val all = dao.getAllSuggestions().first()
        assertEquals(1, all.size)
        assertEquals("new", all.first().id)
    }

    @Test
    fun deleteExpiredSuggestions_deletesOnlyExpired() = runTest {
        val now = System.currentTimeMillis()
        val expired = createTestSuggestion(id = "expired", expiresAt = now - 1000)
        val valid = createTestSuggestion(id = "valid", expiresAt = now + 10000)
        val noExpiry = createTestSuggestion(id = "no-expiry", expiresAt = null)
        
        dao.insertSuggestions(listOf(expired, valid, noExpiry))
        dao.deleteExpiredSuggestions(now)

        val all = dao.getAllSuggestions().first()
        assertEquals(2, all.size) // valid + no-expiry remain
    }

    @Test
    fun deleteAllSuggestions_deletesEverything() = runTest {
        val suggestions = listOf(
            createTestSuggestion(id = "1"),
            createTestSuggestion(id = "2"),
            createTestSuggestion(id = "3")
        )
        dao.insertSuggestions(suggestions)
        dao.deleteAllSuggestions()

        val all = dao.getAllSuggestions().first()
        assertEquals(0, all.size)
    }

    private fun createTestSuggestion(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Suggestion",
        fitScore: Int = 50,
        cachedAt: Long = System.currentTimeMillis(),
        expiresAt: Long? = System.currentTimeMillis() + 86400000
    ): ApiSuggestion {
        return ApiSuggestion(
            id = id,
            title = title,
            content = "Test content",
            source = "test",
            sourceUrl = null,
            category = HabitCategory.FITNESS,
            fitScore = fitScore,
            cachedAt = cachedAt,
            expiresAt = expiresAt
        )
    }
}

