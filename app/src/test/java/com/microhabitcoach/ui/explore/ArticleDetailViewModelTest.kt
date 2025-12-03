package com.microhabitcoach.ui.explore

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private val suggestionId = "suggestion-1"

    @Before
    fun setup() {
        application = Application()
    }

    @Test
    fun loadSuggestion_loadsFromApiSuggestion() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        
        val suggestion = createApiSuggestion(suggestionId, "Test Article")
        apiSuggestionDao.insertSuggestion(suggestion)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        val loaded = viewModel.suggestion.getOrAwaitValue()
        assertNotNull(loaded)
        assertEquals("Test Article", loaded?.title)
    }

    @Test
    fun loadSuggestion_fallsBackToSavedArticle() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val savedArticleDao = db.savedArticleDao()
        
        val savedArticle = createSavedArticle(suggestionId, "Saved Article")
        savedArticleDao.insertSavedArticle(savedArticle)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        val loaded = viewModel.suggestion.getOrAwaitValue()
        assertNotNull(loaded)
        assertEquals("Saved Article", loaded?.title)
    }

    @Test
    fun saveArticle_savesToDatabase() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        val savedArticleDao = db.savedArticleDao()
        
        val suggestion = createApiSuggestion(suggestionId, "Test Article")
        apiSuggestionDao.insertSuggestion(suggestion)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        viewModel.saveArticle()
        advanceUntilIdle()
        
        val isSaved = viewModel.isSaved.getOrAwaitValue()
        assertTrue(isSaved)
    }

    @Test
    fun unsaveArticle_removesFromDatabase() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val savedArticleDao = db.savedArticleDao()
        
        val savedArticle = createSavedArticle(suggestionId, "Saved Article")
        savedArticleDao.insertSavedArticle(savedArticle)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        viewModel.unsaveArticle()
        advanceUntilIdle()
        
        val isSaved = viewModel.isSaved.getOrAwaitValue()
        assertFalse(isSaved)
    }

    @Test
    fun toggleSave_togglesState() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        
        val suggestion = createApiSuggestion(suggestionId, "Test Article")
        apiSuggestionDao.insertSuggestion(suggestion)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        val initialSaved = viewModel.isSaved.getOrAwaitValue()
        
        viewModel.toggleSave()
        advanceUntilIdle()
        
        val afterToggle = viewModel.isSaved.getOrAwaitValue()
        assertTrue(initialSaved != afterToggle)
    }

    @Test
    fun formatPublishedDate_formatsCorrectly() = runTest {
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        
        val formatted = viewModel.formatPublishedDate("2024-01-01T12:00:00Z")
        assertNotNull(formatted)
        assertTrue(formatted?.contains("2024") == true || formatted?.contains("Jan") == true)
    }

    @Test
    fun formatPublishedDate_nullReturnsNull() = runTest {
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        
        val formatted = viewModel.formatPublishedDate(null)
        assertNull(formatted)
    }

    @Test
    fun getDisplaySource_returnsFormattedSource() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        
        val suggestion = createApiSuggestion(suggestionId, "Test", source = "hacker_news")
        apiSuggestionDao.insertSuggestion(suggestion)
        
        val viewModel = ArticleDetailViewModel(application, suggestionId)
        advanceUntilIdle()
        
        val loaded = viewModel.suggestion.getOrAwaitValue()
        val displaySource = viewModel.getDisplaySource(loaded)
        
        assertTrue(displaySource.contains("Hacker", ignoreCase = true))
    }

    private fun createApiSuggestion(
        id: String,
        title: String,
        source: String = "test"
    ) = ApiSuggestion(
        id = id,
        title = title,
        content = "Test content",
        category = HabitCategory.GENERAL,
        fitScore = 50,
        source = source,
        cachedAt = System.currentTimeMillis()
    )

    private fun createSavedArticle(
        id: String,
        title: String
    ) = SavedArticle(
        id = id,
        title = title,
        description = "Test description",
        content = "Test content",
        source = "test",
        sourceUrl = "https://example.com",
        savedAt = System.currentTimeMillis(),
        category = HabitCategory.GENERAL,
        originalFitScore = 50
    )
}

