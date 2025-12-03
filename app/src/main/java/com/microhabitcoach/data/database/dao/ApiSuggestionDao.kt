package com.microhabitcoach.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.microhabitcoach.data.database.entity.ApiSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiSuggestionDao {
    
    @Query("SELECT * FROM api_suggestions ORDER BY fitScore DESC, cachedAt DESC")
    fun getAllSuggestions(): Flow<List<ApiSuggestion>>
    
    @Query("SELECT * FROM api_suggestions WHERE id = :id")
    suspend fun getSuggestionById(id: String): ApiSuggestion?
    
    @Query("SELECT * FROM api_suggestions WHERE (expiresAt IS NULL OR expiresAt > :currentTime) ORDER BY fitScore DESC, cachedAt DESC LIMIT :limit")
    suspend fun getValidSuggestions(currentTime: Long = System.currentTimeMillis(), limit: Int = 50): List<ApiSuggestion>
    
    @Query("SELECT * FROM api_suggestions WHERE fitScore >= :minScore ORDER BY fitScore DESC, cachedAt DESC")
    fun getSuggestionsByMinScore(minScore: Int): Flow<List<ApiSuggestion>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: ApiSuggestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestions(suggestions: List<ApiSuggestion>)
    
    @Delete
    suspend fun deleteSuggestion(suggestion: ApiSuggestion)
    
    @Query("DELETE FROM api_suggestions WHERE id = :id")
    suspend fun deleteSuggestionById(id: String)
    
    @Query("DELETE FROM api_suggestions WHERE cachedAt < :beforeTime")
    suspend fun deleteOldSuggestions(beforeTime: Long)
    
    @Query("DELETE FROM api_suggestions WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime")
    suspend fun deleteExpiredSuggestions(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM api_suggestions")
    suspend fun deleteAllSuggestions()
}

