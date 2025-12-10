package com.microhabitcoach.data.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class SavedArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: SavedArticleDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.savedArticleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertSavedArticle_insertsSuccessfully() = runTest {
        val article = createTestArticle()
        dao.insertSavedArticle(article)

        val retrieved = dao.getSavedArticleById(article.id)
        assertEquals(article.id, retrieved?.id)
        assertEquals(article.title, retrieved?.title)
    }

    @Test
    fun insertSavedArticles_insertsMultipleSuccessfully() = runTest {
        val articles = listOf(
            createTestArticle(id = "1"),
            createTestArticle(id = "2"),
            createTestArticle(id = "3")
        )
        dao.insertSavedArticles(articles)

        val all = dao.getAllSavedArticles().first()
        assertEquals(3, all.size)
    }

    @Test
    fun getAllSavedArticles_ordersBySavedAtDesc() = runTest {
        val article1 = createTestArticle(id = "1", savedAt = 1000L)
        val article2 = createTestArticle(id = "2", savedAt = 2000L)
        val article3 = createTestArticle(id = "3", savedAt = 3000L)
        
        dao.insertSavedArticles(listOf(article1, article2, article3))

        val all = dao.getAllSavedArticles().first()
        assertEquals("3", all[0].id) // Most recent first
        assertEquals("2", all[1].id)
        assertEquals("1", all[2].id)
    }

    @Test
    fun getSavedArticleById_existing_returnsArticle() = runTest {
        val article = createTestArticle()
        dao.insertSavedArticle(article)

        val retrieved = dao.getSavedArticleById(article.id)
        assertEquals(article.id, retrieved?.id)
    }

    @Test
    fun getSavedArticleById_nonExistent_returnsNull() = runTest {
        val retrieved = dao.getSavedArticleById("non-existent")
        assertNull(retrieved)
    }

    @Test
    fun isArticleSaved_existing_returnsTrue() = runTest {
        val article = createTestArticle()
        dao.insertSavedArticle(article)

        val isSaved = dao.isArticleSaved(article.id)
        assertTrue(isSaved)
    }

    @Test
    fun isArticleSaved_nonExistent_returnsFalse() = runTest {
        val isSaved = dao.isArticleSaved("non-existent")
        assertFalse(isSaved)
    }

    @Test
    fun getSavedArticlesBySource_returnsOnlyMatchingSource() = runTest {
        val article1 = createTestArticle(id = "1", source = "hacker_news")
        val article2 = createTestArticle(id = "2", source = "news_api")
        val article3 = createTestArticle(id = "3", source = "hacker_news")
        
        dao.insertSavedArticles(listOf(article1, article2, article3))

        val hackerNewsArticles = dao.getSavedArticlesBySource("hacker_news").first()
        assertEquals(2, hackerNewsArticles.size)
    }

    @Test
    fun getSavedArticlesByCategory_returnsOnlyMatchingCategory() = runTest {
        val article1 = createTestArticle(id = "1", category = HabitCategory.FITNESS)
        val article2 = createTestArticle(id = "2", category = HabitCategory.WELLNESS)
        val article3 = createTestArticle(id = "3", category = HabitCategory.FITNESS)
        
        dao.insertSavedArticles(listOf(article1, article2, article3))

        val fitnessArticles = dao.getSavedArticlesByCategory("FITNESS").first()
        assertEquals(2, fitnessArticles.size)
    }

    @Test
    fun deleteSavedArticle_deletesSuccessfully() = runTest {
        val article = createTestArticle()
        dao.insertSavedArticle(article)
        dao.deleteSavedArticle(article)

        val retrieved = dao.getSavedArticleById(article.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteSavedArticleById_deletesSuccessfully() = runTest {
        val article = createTestArticle()
        dao.insertSavedArticle(article)
        dao.deleteSavedArticleById(article.id)

        val retrieved = dao.getSavedArticleById(article.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteAllSavedArticles_deletesEverything() = runTest {
        val articles = listOf(
            createTestArticle(id = "1"),
            createTestArticle(id = "2"),
            createTestArticle(id = "3")
        )
        dao.insertSavedArticles(articles)
        dao.deleteAllSavedArticles()

        val all = dao.getAllSavedArticles().first()
        assertEquals(0, all.size)
    }

    @Test
    fun getSavedArticlesCount_returnsCorrectCount() = runTest {
        val articles = listOf(
            createTestArticle(id = "1"),
            createTestArticle(id = "2"),
            createTestArticle(id = "3")
        )
        dao.insertSavedArticles(articles)

        val count = dao.getSavedArticlesCount()
        assertEquals(3, count)
    }

    private fun createTestArticle(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Article",
        source: String = "test",
        category: HabitCategory? = HabitCategory.FITNESS,
        savedAt: Long = System.currentTimeMillis()
    ): SavedArticle {
        return SavedArticle(
            id = id,
            title = title,
            description = "Test description",
            content = "Test content",
            source = source,
            sourceUrl = null,
            imageUrl = null,
            author = null,
            publishedAt = null,
            sourceName = "Test Source",
            savedAt = savedAt,
            category = category,
            originalFitScore = 50,
            score = null,
            commentCount = null
        )
    }
}

