package com.microhabitcoach.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.SavedArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedArticlesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = DatabaseModule.getDatabase(application)
    private val savedArticleDao = database.savedArticleDao()

    // LiveData observables
    private val _savedArticles = MutableLiveData<List<SavedArticle>>(emptyList())
    val savedArticles: LiveData<List<SavedArticle>> = _savedArticles

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        loadSavedArticles()
    }

    /**
     * Loads all saved articles from the database.
     */
    private fun loadSavedArticles() {
        viewModelScope.launch {
            savedArticleDao.getAllSavedArticles()
                .collect { articles ->
                    _savedArticles.value = articles
                }
        }
    }

    /**
     * Deletes a saved article.
     */
    fun deleteSavedArticle(article: SavedArticle) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    savedArticleDao.deleteSavedArticle(article)
                }
                android.util.Log.d("SavedArticlesViewModel", "Deleted article: ${article.title}")
            } catch (e: Exception) {
                android.util.Log.e("SavedArticlesViewModel", "Error deleting article: ${e.message}", e)
                _error.value = "Failed to delete article: ${e.message}"
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SavedArticlesViewModel::class.java)) {
                return SavedArticlesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

