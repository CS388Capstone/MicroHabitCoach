package com.microhabitcoach.ui.explore

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.database.entity.UserPreferences
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.MotionState
import com.microhabitcoach.data.model.UserContext
import com.microhabitcoach.data.repository.ApiRepository
import com.microhabitcoach.data.util.FitScoreCalculator
import com.microhabitcoach.data.util.HabitClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class ExploreViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = DatabaseModule.getDatabase(application)
    private val apiRepository = ApiRepository(database)
    private val apiSuggestionDao = database.apiSuggestionDao()
    private val userPreferencesDao = database.userPreferencesDao()

    // LiveData observables
    private val _suggestions = MutableLiveData<List<ApiSuggestion>>(emptyList())
    val suggestions: LiveData<List<ApiSuggestion>> = _suggestions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Cache refresh settings
    private val CACHE_REFRESH_INTERVAL_MS = 6 * 60 * 60 * 1000L // 6 hours
    private val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours
    private var refreshJob: Job? = null

    init {
        // Observe cached suggestions from Room
        observeCachedSuggestions()
        
        // Load suggestions on initialization
        loadSuggestions()
        
        // Start periodic refresh
        startPeriodicRefresh()
    }

    /**
     * Observes cached suggestions from Room database.
     * Suggestions are automatically sorted by FitScore.
     */
    private fun observeCachedSuggestions() {
        viewModelScope.launch {
            apiSuggestionDao.getAllSuggestions()
                .catch { e ->
                    _error.value = "Failed to load cached suggestions: ${e.message}"
                }
                .collect { cachedSuggestions ->
                    _suggestions.value = cachedSuggestions
                }
        }
    }

    /**
     * Loads suggestions from API, classifies them, calculates FitScore, and caches them.
     * Uses cached data if available and fresh, otherwise fetches from API.
     */
    fun loadSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Check if we have valid cached suggestions
                val cachedSuggestions = withContext(Dispatchers.IO) {
                    apiSuggestionDao.getValidSuggestions(
                        currentTime = System.currentTimeMillis(),
                        limit = 50
                    )
                }
                
                // If we have fresh cached data, use it
                if (cachedSuggestions.isNotEmpty()) {
                    _isLoading.value = false
                    return@launch
                }
                
                // Otherwise, fetch from API
                refreshSuggestions()
            } catch (e: Exception) {
                _error.value = "Failed to load suggestions: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Forces a refresh of suggestions from the API.
     * Fetches, classifies, scores, and caches new suggestions.
     */
    fun refreshSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Build UserContext for FitScore calculation
                val userContext = buildUserContext()
                
                // Fetch suggestions from API with timeout
                val apiResult = withContext(Dispatchers.IO) {
                    apiRepository.fetchSuggestions()
                }
                
                when {
                    apiResult.isSuccess -> {
                        val rawSuggestions = apiResult.getOrNull() ?: emptyList()
                        
                        android.util.Log.d("ExploreViewModel", "=== PROCESSING SUGGESTIONS ===")
                        android.util.Log.d("ExploreViewModel", "Raw suggestions received: ${rawSuggestions.size}")
                        
                        // Separate by source for comparison
                        val hackerNewsSuggestions = rawSuggestions.filter { it.source == "hacker_news" }
                        val newsApiSuggestions = rawSuggestions.filter { it.source == "news_api" }
                        
                        android.util.Log.d("ExploreViewModel", "  - Hacker News: ${hackerNewsSuggestions.size}")
                        android.util.Log.d("ExploreViewModel", "  - News API: ${newsApiSuggestions.size}")
                        
                        // Process suggestions: classify and calculate FitScore
                        val processedSuggestions = withContext(Dispatchers.Default) {
                            processSuggestions(rawSuggestions, userContext)
                        }
                        
                        // Compare classification quality by source
                        val processedHn = processedSuggestions.filter { it.source == "hacker_news" }
                        val processedNews = processedSuggestions.filter { it.source == "news_api" }
                        
                        val hnAvgFitScore = if (processedHn.isNotEmpty()) {
                            processedHn.map { it.fitScore }.average()
                        } else 0.0
                        val newsAvgFitScore = if (processedNews.isNotEmpty()) {
                            processedNews.map { it.fitScore }.average()
                        } else 0.0
                        
                        android.util.Log.d("ExploreViewModel", "=== CLASSIFICATION QUALITY COMPARISON ===")
                        android.util.Log.d("ExploreViewModel", "Hacker News: ${processedHn.size} suggestions, avg FitScore: ${"%.1f".format(hnAvgFitScore)}")
                        processedHn.groupBy { it.category }.forEach { (category, suggestions) ->
                            val avgScore = suggestions.map { it.fitScore }.average()
                            android.util.Log.d("ExploreViewModel", "  - $category: ${suggestions.size} (avg FitScore: ${"%.1f".format(avgScore)})")
                        }
                        android.util.Log.d("ExploreViewModel", "News API: ${processedNews.size} suggestions, avg FitScore: ${"%.1f".format(newsAvgFitScore)}")
                        processedNews.groupBy { it.category }.forEach { (category, suggestions) ->
                            val avgScore = suggestions.map { it.fitScore }.average()
                            android.util.Log.d("ExploreViewModel", "  - $category: ${suggestions.size} (avg FitScore: ${"%.1f".format(avgScore)})")
                        }
                        android.util.Log.d("ExploreViewModel", "=======================================")
                        
                        // Log top suggestions by FitScore
                        android.util.Log.d("ExploreViewModel", "Top 20 FitScores (sorted):")
                        processedSuggestions.take(20).forEachIndexed { index, suggestion ->
                            android.util.Log.d("ExploreViewModel", "  ${index + 1}. [${suggestion.fitScore}] [${suggestion.source}] ${suggestion.title.take(45)} (${suggestion.category})")
                        }
                        
                        // Take top 10 by FitScore for display
                        val topSuggestions = processedSuggestions.take(10)
                        
                        android.util.Log.d("ExploreViewModel", "=== TOP 10 SELECTED ===")
                        topSuggestions.forEachIndexed { index, suggestion ->
                            android.util.Log.d("ExploreViewModel", "  ${index + 1}. [${suggestion.fitScore}] [${suggestion.source}] ${suggestion.title.take(50)}")
                        }
                        android.util.Log.d("ExploreViewModel", "========================")
                        
                        // Cache processed suggestions (even if empty, to prevent repeated API calls)
                        withContext(Dispatchers.IO) {
                            // Clear ALL old suggestions to get fresh data on refresh
                            apiSuggestionDao.deleteAllSuggestions()
                            
                            // Insert top 10 suggestions for display
                            if (topSuggestions.isNotEmpty()) {
                                apiRepository.cacheSuggestions(topSuggestions)
                            }
                        }
                        
                        android.util.Log.d("ExploreViewModel", "Refreshed: ${topSuggestions.size} top suggestions loaded")
                        _isLoading.value = false
                    }
                    apiResult.isFailure -> {
                        val exception = apiResult.exceptionOrNull()
                        val errorMessage = exception?.message ?: "Unknown error"
                        android.util.Log.e("ExploreViewModel", "API error: $errorMessage", exception)
                        
                        // Try to load cached data as fallback
                        val cachedSuggestions = withContext(Dispatchers.IO) {
                            apiSuggestionDao.getValidSuggestions(
                                currentTime = System.currentTimeMillis(),
                                limit = 50
                            )
                        }
                        
                        if (cachedSuggestions.isNotEmpty()) {
                            // Show cached data with a warning
                            _error.value = "Using cached data. ${errorMessage}"
                        } else {
                            // No cached data - try mock data for development/testing
                            android.util.Log.w("ExploreViewModel", "No cached data, using mock suggestions for testing")
                            val mockSuggestions = withContext(Dispatchers.IO) {
                                apiRepository.getMockSuggestions()
                            }
                            
                            if (mockSuggestions.isNotEmpty()) {
                                // Process mock suggestions with FitScore
                                val processedMock = withContext(Dispatchers.Default) {
                                    processSuggestions(mockSuggestions, userContext)
                                }
                                
                                // Cache mock suggestions
                                withContext(Dispatchers.IO) {
                                    apiRepository.cacheSuggestions(processedMock)
                                }
                                
                                _error.value = "Using demo data. ${errorMessage}"
                            } else {
                                // No data available at all
                                _error.value = "Failed to load suggestions: ${errorMessage}"
                            }
                        }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreViewModel", "Error in refreshSuggestions: ${e.message}", e)
                
                // Try to load cached data as fallback
                try {
                    val cachedSuggestions = withContext(Dispatchers.IO) {
                        apiSuggestionDao.getValidSuggestions(
                            currentTime = System.currentTimeMillis(),
                            limit = 50
                        )
                    }
                    
                    if (cachedSuggestions.isNotEmpty()) {
                        _error.value = "Using cached data. Error: ${e.message}"
                    } else {
                        _error.value = "Failed to refresh suggestions: ${e.message}"
                    }
                } catch (cacheError: Exception) {
                    _error.value = "Failed to refresh suggestions: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Processes raw API suggestions by classifying and calculating FitScore.
     * 
     * @param rawSuggestions Raw suggestions from API
     * @param userContext Current user context for scoring
     * @return Processed suggestions with category and FitScore
     */
    private fun processSuggestions(
        rawSuggestions: List<ApiSuggestion>,
        userContext: UserContext
    ): List<ApiSuggestion> {
        val currentTime = System.currentTimeMillis()
        
        return rawSuggestions.map { suggestion ->
            // Classify if not already classified (for raw API data)
            val category = if (suggestion.category == HabitCategory.GENERAL && 
                               suggestion.title.isNotEmpty()) {
                HabitClassifier.classify(suggestion.title, suggestion.content)
            } else {
                suggestion.category
            }
            
            // Create suggestion with category
            val categorizedSuggestion = suggestion.copy(category = category)
            
            // Calculate FitScore
            val fitScore = FitScoreCalculator.calculate(categorizedSuggestion, userContext)
            
            // Return processed suggestion with FitScore and expiry
            categorizedSuggestion.copy(
                fitScore = fitScore,
                cachedAt = currentTime,
                expiresAt = currentTime + CACHE_EXPIRY_MS
            )
        }.sortedByDescending { it.fitScore } // Sort by FitScore descending
    }

    /**
     * Builds UserContext from user preferences, location, and motion state.
     * 
     * @return UserContext for FitScore calculation
     */
    private suspend fun buildUserContext(): UserContext {
        // Get user preferences
        val preferences = withContext(Dispatchers.IO) {
            userPreferencesDao.getUserPreferences() 
                ?: UserPreferences() // Default preferences if none exist
        }
        
        // Get current time
        val currentTime = LocalTime.now()
        
        // TODO: Get current location from LocationManager/FusedLocationProvider
        // For now, we'll use null (location is optional)
        val currentLocation: Location? = null
        
        // TODO: Get recent motion state from ActivityRecognition
        // For now, we'll use UNKNOWN (motion state is optional)
        val motionState = MotionState.UNKNOWN
        
        // TODO: Get current weather (optional for MVP)
        val currentWeather = null
        
        return UserContext(
            preferredCategories = preferences.preferredCategories,
            currentTime = currentTime,
            currentLocation = currentLocation,
            recentMotionState = motionState,
            currentWeather = currentWeather
        )
    }

    /**
     * Starts periodic refresh of suggestions.
     * Refreshes every 6 hours to reduce API calls.
     */
    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(CACHE_REFRESH_INTERVAL_MS)
                refreshSuggestions()
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
                return ExploreViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

