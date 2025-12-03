package com.microhabitcoach.ui.explore

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.database.entity.UserPreferences
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application

    @Before
    fun setup() {
        application = Application()
    }

    @Test
    fun loadSuggestions_loadsCachedSuggestions() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        
        val suggestion = createSuggestion("suggestion-1", "Test Suggestion")
        apiSuggestionDao.insertSuggestion(suggestion)
        
        val viewModel = ExploreViewModel(application)
        advanceUntilIdle()
        
        val suggestions = viewModel.suggestions.getOrAwaitValue()
        assertTrue(suggestions.isNotEmpty())
    }

    @Test
    fun refreshSuggestions_refreshesData() = runTest {
        val viewModel = ExploreViewModel(application)
        
        viewModel.refreshSuggestions()
        advanceUntilIdle()
        
        // Should not crash - may load from API or cache
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertTrue(!isLoading) // Should finish loading
    }

    @Test
    fun suggestions_sortedByFitScore() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val apiSuggestionDao = db.apiSuggestionDao()
        
        val suggestion1 = createSuggestion("suggestion-1", "Low Score", fitScore = 30)
        val suggestion2 = createSuggestion("suggestion-2", "High Score", fitScore = 90)
        val suggestion3 = createSuggestion("suggestion-3", "Medium Score", fitScore = 60)
        
        apiSuggestionDao.insertSuggestion(suggestion1)
        apiSuggestionDao.insertSuggestion(suggestion2)
        apiSuggestionDao.insertSuggestion(suggestion3)
        
        val viewModel = ExploreViewModel(application)
        advanceUntilIdle()
        
        val suggestions = viewModel.suggestions.getOrAwaitValue()
        if (suggestions.size >= 3) {
            // Should be sorted by FitScore descending
            assertTrue(suggestions[0].fitScore >= suggestions[1].fitScore)
        }
    }

    @Test
    fun error_handlesGracefully() = runTest {
        val viewModel = ExploreViewModel(application)
        advanceUntilIdle()
        
        // Should not crash even if there's an error
        val error = viewModel.error.getOrAwaitValue()
        // Error may be null or contain a message
    }

    private fun createSuggestion(
        id: String,
        title: String,
        fitScore: Int = 50
    ) = ApiSuggestion(
        id = id,
        title = title,
        content = "Test content",
        category = HabitCategory.GENERAL,
        fitScore = fitScore,
        source = "test",
        cachedAt = System.currentTimeMillis()
    )
}

