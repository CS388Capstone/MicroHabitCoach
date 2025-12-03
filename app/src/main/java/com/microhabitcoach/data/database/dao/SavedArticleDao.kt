package com.microhabitcoach.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microhabitcoach.data.database.entity.SavedArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedArticleDao {
    
    /**
     * Get all saved articles, ordered by most recently saved first.
     */
    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticle>>
    
    /**
     * Get a saved article by ID.
     */
    @Query("SELECT * FROM saved_articles WHERE id = :id")
    suspend fun getSavedArticleById(id: String): SavedArticle?
    
    /**
     * Check if an article is already saved.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    suspend fun isArticleSaved(id: String): Boolean
    
    /**
     * Get saved articles by source.
     */
    @Query("SELECT * FROM saved_articles WHERE source = :source ORDER BY savedAt DESC")
    fun getSavedArticlesBySource(source: String): Flow<List<SavedArticle>>
    
    /**
     * Get saved articles by category.
     */
    @Query("SELECT * FROM saved_articles WHERE category = :category ORDER BY savedAt DESC")
    fun getSavedArticlesByCategory(category: String): Flow<List<SavedArticle>>
    
    /**
     * Insert a saved article.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedArticle(article: SavedArticle)
    
    /**
     * Insert multiple saved articles.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedArticles(articles: List<SavedArticle>)
    
    /**
     * Delete a saved article.
     */
    @Delete
    suspend fun deleteSavedArticle(article: SavedArticle)
    
    /**
     * Delete a saved article by ID.
     */
    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteSavedArticleById(id: String)
    
    /**
     * Delete all saved articles.
     */
    @Query("DELETE FROM saved_articles")
    suspend fun deleteAllSavedArticles()
    
    /**
     * Get count of saved articles.
     */
    @Query("SELECT COUNT(*) FROM saved_articles")
    suspend fun getSavedArticlesCount(): Int
}

