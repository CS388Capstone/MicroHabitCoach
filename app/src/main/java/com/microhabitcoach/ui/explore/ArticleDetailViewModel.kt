package com.microhabitcoach.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.util.HabitTypeInferrer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ArticleDetailViewModel(
    application: Application,
    private val suggestionId: String
) : AndroidViewModel(application) {

    private val database = DatabaseModule.getDatabase(application)
    private val apiSuggestionDao = database.apiSuggestionDao()
    private val savedArticleDao = database.savedArticleDao()

    // LiveData observables
    private val _suggestion = MutableLiveData<ApiSuggestion?>()
    val suggestion: LiveData<ApiSuggestion?> = _suggestion

    private val _isSaved = MutableLiveData<Boolean>(false)
    val isSaved: LiveData<Boolean> = _isSaved

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadSuggestion()
        checkIfSaved()
    }

    /**
     * Loads the suggestion from the database.
     * First tries ApiSuggestion cache, then falls back to SavedArticle.
     */
    private fun loadSuggestion() {
        viewModelScope.launch {
            try {
                // First try to load from ApiSuggestion cache
                var suggestion = withContext(Dispatchers.IO) {
                    apiSuggestionDao.getSuggestionById(suggestionId)
                }
                
                // If not found in cache, try SavedArticle
                if (suggestion == null) {
                    val savedArticle = withContext(Dispatchers.IO) {
                        savedArticleDao.getSavedArticleById(suggestionId)
                    }
                    
                    if (savedArticle != null) {
                        // Convert SavedArticle to ApiSuggestion for display
                        suggestion = convertToApiSuggestion(savedArticle)
                    }
                }
                
                _suggestion.value = suggestion
                
                if (suggestion == null) {
                    _error.value = "Article not found"
                }
            } catch (e: Exception) {
                android.util.Log.e("ArticleDetailViewModel", "Error loading suggestion: ${e.message}", e)
                _error.value = "Failed to load article: ${e.message}"
            }
        }
    }
    
    /**
     * Converts SavedArticle to ApiSuggestion for display.
     */
    private fun convertToApiSuggestion(savedArticle: SavedArticle): ApiSuggestion {
        return ApiSuggestion(
            id = savedArticle.id,
            title = savedArticle.title,
            content = savedArticle.description ?: savedArticle.content,
            source = savedArticle.source,
            sourceUrl = savedArticle.sourceUrl,
            category = savedArticle.category ?: com.microhabitcoach.data.model.HabitCategory.GENERAL,
            fitScore = savedArticle.originalFitScore ?: 50,
            cachedAt = savedArticle.savedAt,
            expiresAt = null,
            imageUrl = savedArticle.imageUrl,
            author = savedArticle.author,
            publishedAt = savedArticle.publishedAt,
            sourceName = savedArticle.sourceName,
            score = savedArticle.score,
            commentCount = savedArticle.commentCount
        )
    }

    /**
     * Checks if the article is already saved.
     */
    private fun checkIfSaved() {
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    savedArticleDao.isArticleSaved(suggestionId)
                }
                _isSaved.value = saved
            } catch (e: Exception) {
                android.util.Log.e("ArticleDetailViewModel", "Error checking if saved: ${e.message}", e)
            }
        }
    }

    /**
     * Saves the article to saved articles.
     */
    fun saveArticle() {
        viewModelScope.launch {
            try {
                val suggestion = _suggestion.value
                if (suggestion == null) {
                    _error.value = "Cannot save: article not loaded"
                    return@launch
                }

                val savedArticle = convertToSavedArticle(suggestion)
                
                withContext(Dispatchers.IO) {
                    savedArticleDao.insertSavedArticle(savedArticle)
                }
                
                _isSaved.value = true
                android.util.Log.d("ArticleDetailViewModel", "Article saved: ${suggestion.title}")
            } catch (e: Exception) {
                android.util.Log.e("ArticleDetailViewModel", "Error saving article: ${e.message}", e)
                _error.value = "Failed to save article: ${e.message}"
            }
        }
    }

    /**
     * Removes the article from saved articles.
     */
    fun unsaveArticle() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    savedArticleDao.deleteSavedArticleById(suggestionId)
                }
                
                _isSaved.value = false
                android.util.Log.d("ArticleDetailViewModel", "Article unsaved: $suggestionId")
            } catch (e: Exception) {
                android.util.Log.e("ArticleDetailViewModel", "Error unsaving article: ${e.message}", e)
                _error.value = "Failed to remove article: ${e.message}"
            }
        }
    }

    /**
     * Toggles save/unsave state.
     */
    fun toggleSave() {
        val currentSaved = _isSaved.value ?: false
        if (currentSaved) {
            unsaveArticle()
        } else {
            saveArticle()
        }
    }

    /**
     * Converts ApiSuggestion to SavedArticle.
     */
    private fun convertToSavedArticle(suggestion: ApiSuggestion): SavedArticle {
        return SavedArticle(
            id = suggestion.id,
            title = suggestion.title ?: "",
            description = suggestion.content,
            content = suggestion.content,
            source = suggestion.source ?: "",
            sourceUrl = suggestion.sourceUrl,
            imageUrl = suggestion.imageUrl,
            author = suggestion.author,
            publishedAt = suggestion.publishedAt,
            sourceName = suggestion.sourceName,
            savedAt = System.currentTimeMillis(),
            category = suggestion.category,
            originalFitScore = suggestion.fitScore,
            score = suggestion.score,
            commentCount = suggestion.commentCount
        )
    }

    /**
     * Formats the published date for display.
     */
    fun formatPublishedDate(publishedAt: String?): String? {
        if (publishedAt == null) return null
        
        return try {
            // Try to parse ISO 8601 format from News API
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val date = inputFormat.parse(publishedAt)
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            // If parsing fails, return as-is or try alternative format
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val date = inputFormat.parse(publishedAt)
                date?.let { outputFormat.format(it) }
            } catch (e2: Exception) {
                publishedAt // Return original if all parsing fails
            }
        }
    }

    /**
     * Formats the source name for display.
     */
    fun getDisplaySource(suggestion: ApiSuggestion?): String {
        if (suggestion == null) return ""
        
        return when {
            !suggestion.sourceName.isNullOrBlank() -> suggestion.sourceName!!
            suggestion.source == "hacker_news" -> "Hacker News"
            suggestion.source == "news_api" -> "News API"
            else -> (suggestion.source ?: "").replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    class Factory(
        private val application: Application,
        private val suggestionId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ArticleDetailViewModel::class.java)) {
                return ArticleDetailViewModel(application, suggestionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

