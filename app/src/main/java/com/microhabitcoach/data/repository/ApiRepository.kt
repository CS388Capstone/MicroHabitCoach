package com.microhabitcoach.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.ApiSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository(private val database: AppDatabase) {
    
    private val apiSuggestionDao = database.apiSuggestionDao()
    
    // LiveData observable
    fun getAllSuggestions(): LiveData<List<ApiSuggestion>> {
        return apiSuggestionDao.getAllSuggestions().asLiveData()
    }
    
    // Stub methods for Sprint 2 - API integration
    suspend fun fetchSuggestions(): Result<List<ApiSuggestion>> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement API calls in Sprint 2
                // For now, return empty list
                Result.success(emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    suspend fun cacheSuggestions(suggestions: List<ApiSuggestion>) {
        withContext(Dispatchers.IO) {
            try {
                apiSuggestionDao.insertSuggestions(suggestions)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

