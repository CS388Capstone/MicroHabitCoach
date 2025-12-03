package com.microhabitcoach.ui.explore

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SavedArticlesViewModelTest {

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
    fun loadSavedArticles_loadsAllArticles() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val savedArticleDao = db.savedArticleDao()
        
        val article1 = createSavedArticle("article-1", "Article 1")
        val article2 = createSavedArticle("article-2", "Article 2")
        
        savedArticleDao.insertSavedArticle(article1)
        savedArticleDao.insertSavedArticle(article2)
        
        val viewModel = SavedArticlesViewModel(application)
        advanceUntilIdle()
        
        val articles = viewModel.savedArticles.getOrAwaitValue()
        assertTrue(articles.size >= 2)
    }

    @Test
    fun loadSavedArticles_emptyList_whenNoArticles() = runTest {
        val viewModel = SavedArticlesViewModel(application)
        advanceUntilIdle()
        
        val articles = viewModel.savedArticles.getOrAwaitValue()
        assertTrue(articles.isEmpty())
    }

    @Test
    fun deleteSavedArticle_removesFromList() = runTest {
        val db = DatabaseModule.getDatabase(application)
        val savedArticleDao = db.savedArticleDao()
        
        val article = createSavedArticle("article-1", "Article 1")
        savedArticleDao.insertSavedArticle(article)
        
        val viewModel = SavedArticlesViewModel(application)
        advanceUntilIdle()
        
        val initialCount = viewModel.savedArticles.getOrAwaitValue().size
        
        viewModel.deleteSavedArticle(article)
        advanceUntilIdle()
        
        val finalCount = viewModel.savedArticles.getOrAwaitValue().size
        assertTrue(finalCount < initialCount)
    }

    @Test
    fun clearError_clearsError() = runTest {
        val viewModel = SavedArticlesViewModel(application)
        viewModel.clearError()
        
        val error = viewModel.error.getOrAwaitValue()
        assertNull(error)
    }

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

