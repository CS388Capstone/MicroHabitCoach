package com.microhabitcoach.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.microhabitcoach.data.api.ApiModule
import com.microhabitcoach.data.api.HackerNewsItem
import com.microhabitcoach.data.api.NewsArticle
import com.microhabitcoach.data.database.AppDatabase
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.util.HabitClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

class ApiRepository(private val database: AppDatabase) {
    
    private val apiSuggestionDao = database.apiSuggestionDao()
    private val hackerNewsApi = ApiModule.hackerNewsApi
    private val newsApi = ApiModule.newsApi
    
    // Keywords to filter relevant posts from Hacker News
    // Focused on health, fitness, wellness, productivity, and learning
    // Expanded list to catch more relevant articles from Hacker News
    private val relevantKeywords = listOf(
        // Health & Fitness
        "workout", "exercise", "gym", "run", "walk", "jog", "fitness", "health", "healthy",
        "cardio", "strength", "training", "bike", "cycling", "swim", "swimming",
        "yoga", "pilates", "stretch", "hike", "hiking", "sprint", "marathon",
        "weight", "lifting", "aerobics", "calisthenics", "physical", "activity", "active",
        "movement", "sport", "sports", "athletic", "endurance", "flexibility",
        // Wellness & Mental Health
        "meditation", "meditate", "breath", "breathing", "wellness", "mindfulness",
        "sleep", "water", "hydrate", "hydration", "relax", "relaxation", "stress",
        "anxiety", "mental", "self-care", "therapy", "calm", "peace", "zen",
        "wellbeing", "well-being", "nutrition", "diet", "eating", "food", "meal",
        "energy", "rest", "recovery", "balance", "lifestyle",
        // Productivity & Learning
        "productivity", "read", "reading", "study", "learn", "learning", "focus",
        "pomodoro", "organize", "plan", "schedule", "task", "goal", "achieve",
        "complete", "finish", "work", "project", "time management", "efficiency",
        "optimize", "improve", "develop", "build", "course", "tutorial", "skill",
        "education", "teach", "knowledge", "research", "explore", "discover",
        "practice", "master", "expert", "expertise", "technique", "method",
        "system", "process", "workflow", "automation", "tools", "app", "software",
        // Self-Improvement
        "self-improvement", "habit", "habits", "routine", "discipline", "consistency",
        "motivation", "inspiration", "growth", "development", "progress", "success",
        "challenge", "commitment", "dedication", "persistence", "resilience"
    )
    
    // LiveData observable
    fun getAllSuggestions(): LiveData<List<ApiSuggestion>> {
        return apiSuggestionDao.getAllSuggestions().asLiveData()
    }
    
    /**
     * Fetches suggestions from both Hacker News API and News API.
     * Combines results from both sources, filters by keywords, and converts to ApiSuggestion entities.
     * 
     * @return Result containing list of ApiSuggestion or error
     */
    suspend fun fetchSuggestions(): Result<List<ApiSuggestion>> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ApiRepository", "Starting to fetch from both Hacker News and News API...")
                
                // Overall timeout of 45 seconds for the entire operation (both APIs, 75 articles each)
                withTimeout(45000) {
                    // Fetch from both sources in parallel
                    val hackerNewsResult = async {
                        fetchFromHackerNews()
                    }
                    
                    val newsApiResult = async {
                        fetchFromNewsApi()
                    }
                    
                    // Wait for both to complete (or fail gracefully)
                    val hackerNewsSuggestions = try {
                        hackerNewsResult.await().getOrElse { emptyList() }
                    } catch (e: Exception) {
                        android.util.Log.w("ApiRepository", "Hacker News fetch failed: ${e.message}")
                        emptyList()
                    }
                    
                    val newsApiSuggestions = try {
                        newsApiResult.await().getOrElse { emptyList() }
                    } catch (e: Exception) {
                        android.util.Log.w("ApiRepository", "News API fetch failed: ${e.message}")
                        emptyList()
                    }
                    
                    // Combine and deduplicate by title (case-insensitive)
                    val allSuggestions = (hackerNewsSuggestions + newsApiSuggestions)
                        .distinctBy { it.title.lowercase().trim() }
                    
                    // Log classification breakdown by source
                    val hnByCategory = hackerNewsSuggestions.groupBy { it.category }
                    val newsByCategory = newsApiSuggestions.groupBy { it.category }
                    
                    android.util.Log.d("ApiRepository", "=== API FETCH SUMMARY ===")
                    android.util.Log.d("ApiRepository", "Hacker News: ${hackerNewsSuggestions.size} suggestions")
                    hnByCategory.forEach { (category, suggestions) ->
                        android.util.Log.d("ApiRepository", "  - $category: ${suggestions.size}")
                    }
                    android.util.Log.d("ApiRepository", "News API: ${newsApiSuggestions.size} suggestions")
                    newsByCategory.forEach { (category, suggestions) ->
                        android.util.Log.d("ApiRepository", "  - $category: ${suggestions.size}")
                    }
                    android.util.Log.d("ApiRepository", "Total (deduplicated): ${allSuggestions.size} suggestions")
                    android.util.Log.d("ApiRepository", "========================")
                    
                    Result.success(allSuggestions)
                }
            } catch (e: TimeoutCancellationException) {
                android.util.Log.e("ApiRepository", "Request timed out: ${e.message}")
                Result.failure(Exception("Request timed out. Please check your internet connection."))
            } catch (e: java.net.UnknownHostException) {
                android.util.Log.e("ApiRepository", "Network error - unknown host: ${e.message}")
                Result.failure(Exception("Network error: Cannot reach server. Check your internet connection."))
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.e("ApiRepository", "Socket timeout: ${e.message}")
                Result.failure(Exception("Connection timed out. Please check your internet connection."))
            } catch (e: java.io.IOException) {
                android.util.Log.e("ApiRepository", "IO error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                android.util.Log.e("ApiRepository", "Error fetching suggestions: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Fetches suggestions from Hacker News API.
     */
    private suspend fun fetchFromHackerNews(): Result<List<ApiSuggestion>> {
        return try {
            android.util.Log.d("ApiRepository", "Fetching from Hacker News...")
            
            val topStoryIds = hackerNewsApi.getTopStories()
            // Fetch 75 articles from Hacker News
            val limitedIds = topStoryIds.take(75)
            
            // Fetch individual items in parallel using coroutineScope
            val items = coroutineScope {
                limitedIds.chunked(10).flatMap { chunk ->
                    chunk.map { id ->
                        async {
                            try {
                                withTimeout(2000) {
                                    hackerNewsApi.getItem(id)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll()
                }.filterNotNull()
            }
            
            // Filter for relevant stories
            val relevantItems = items.filter { item ->
                item.isStory() && isRelevant(item)
            }
            
            android.util.Log.d("ApiRepository", "Hacker News: Fetched ${items.size} items (requested 75), found ${relevantItems.size} relevant after filtering")
            
            // Convert to ApiSuggestion entities
            val suggestions = relevantItems.map { item ->
                convertToApiSuggestion(item)
            }
            
            Result.success(suggestions)
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "Hacker News error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetches suggestions from News API.
     * Uses multiple targeted queries for better health/wellness/productivity results.
     */
    private suspend fun fetchFromNewsApi(): Result<List<ApiSuggestion>> {
        return try {
            android.util.Log.d("ApiRepository", "Fetching from News API with targeted queries...")
            
            // Use multiple targeted queries to get better results for each category
            // Each query fetches 25 articles, totaling 75 articles
            val queries = listOf(
                // Health & Fitness focused query
                "(health OR fitness OR exercise OR workout OR gym OR running OR yoga OR strength training) AND (habit OR routine OR daily OR practice)",
                // Wellness & Mental Health focused query
                "(wellness OR meditation OR mindfulness OR sleep OR stress OR mental health OR self-care) AND (habit OR routine OR daily OR practice)",
                // Productivity & Learning focused query
                "(productivity OR time management OR learning OR study OR focus OR pomodoro OR organization) AND (habit OR routine OR daily OR practice)"
            )
            
            val allArticles = coroutineScope {
                queries.mapIndexed { index, query ->
                    async {
                        try {
                            android.util.Log.d("ApiRepository", "News API query ${index + 1}/3: $query")
                            
                            val response = newsApi.getEverything(
                                q = query,
                                apiKey = ApiModule.NEWS_API_KEY,
                                pageSize = 25, // 25 per query = 75 total
                                sortBy = "relevancy"
                            )
                            
                            android.util.Log.d("ApiRepository", "News API query ${index + 1} response: status=${response.status}, totalResults=${response.totalResults}")
                            
                            if (response.status == "ok") {
                                response.articles
                            } else {
                                android.util.Log.w("ApiRepository", "News API query ${index + 1} returned non-ok status: ${response.status}")
                                emptyList()
                            }
                        } catch (e: retrofit2.HttpException) {
                            val errorBody = e.response()?.errorBody()?.string()
                            android.util.Log.e("ApiRepository", "News API query ${index + 1} HTTP error: ${e.code()} - ${e.message()}")
                            android.util.Log.e("ApiRepository", "News API query ${index + 1} error body: $errorBody")
                            emptyList()
                        } catch (e: Exception) {
                            android.util.Log.e("ApiRepository", "News API query ${index + 1} failed: ${e.javaClass.simpleName} - ${e.message}", e)
                            emptyList()
                        }
                    }
                }.awaitAll()
            }.flatten()
            
            android.util.Log.d("ApiRepository", "News API: Fetched ${allArticles.size} articles total (from ${queries.size} queries, ~25 each)")
            
            // Log sample articles from each query
            allArticles.take(10).forEachIndexed { index, article ->
                android.util.Log.d("ApiRepository", "News API article ${index + 1}: '${article.title?.take(60)}'")
            }
            
            // Filter for relevant articles (double-check with our keyword filter)
            val relevantArticles = allArticles.filter { article ->
                isRelevantNewsArticle(article)
            }
            
            android.util.Log.d("ApiRepository", "News API: Found ${relevantArticles.size} relevant articles out of ${allArticles.size} total fetched")
            
            // Convert to ApiSuggestion entities
            val suggestions = relevantArticles.map { article ->
                convertNewsArticleToApiSuggestion(article)
            }
            
            Result.success(suggestions)
        } catch (e: Exception) {
            android.util.Log.e("ApiRepository", "News API error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Checks if an item is relevant based on keyword matching.
     */
    private fun isRelevant(item: HackerNewsItem): Boolean {
        val content = item.getContentForClassification().lowercase()
        return relevantKeywords.any { keyword ->
            content.contains(keyword.lowercase())
        }
    }
    
    /**
     * Checks if a News API article is relevant based on keyword matching.
     */
    private fun isRelevantNewsArticle(article: NewsArticle): Boolean {
        val content = ((article.title ?: "") + " " + (article.description ?: "") + " " + (article.content ?: "")).lowercase()
        return relevantKeywords.any { keyword ->
            content.contains(keyword.lowercase())
        }
    }
    
    /**
     * Converts HackerNewsItem to ApiSuggestion entity.
     */
    private fun convertToApiSuggestion(item: HackerNewsItem): ApiSuggestion {
        // Use Hacker News ID as the suggestion ID
        val id = "hn_${item.id}"
        
        // Classify the item (will be re-scored later with FitScore)
        val category = HabitClassifier.classify(
            item.title ?: "",
            item.text
        )
        
        // Extract content snippet (first 200 chars of text or title)
        val content = when {
            !item.text.isNullOrBlank() -> item.text.take(200)
            !item.title.isNullOrBlank() -> item.title
            else -> null
        }
        
        return ApiSuggestion(
            id = id,
            title = item.title ?: "Untitled",
            content = content,
            source = "hacker_news",
            sourceUrl = item.url,
            category = category,
            fitScore = 50, // Base score, will be recalculated with FitScoreCalculator
            cachedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
            // Extra fields from Hacker News
            imageUrl = null, // Hacker News doesn't provide images
            author = item.by,
            publishedAt = null, // Hacker News uses Unix timestamp, not ISO date
            sourceName = "Hacker News",
            score = item.score,
            commentCount = item.descendants
        )
    }
    
    /**
     * Converts NewsArticle to ApiSuggestion entity.
     */
    private fun convertNewsArticleToApiSuggestion(article: NewsArticle): ApiSuggestion {
        // Use URL hash as ID (News API doesn't have numeric IDs)
        val id = "news_${article.url?.hashCode() ?: UUID.randomUUID().toString()}"
        
        // Classify the article
        val category = HabitClassifier.classify(
            article.title ?: "",
            article.description ?: article.content
        )
        
        // Extract content snippet (prefer description, fallback to content)
        val content = when {
            !article.description.isNullOrBlank() -> article.description.take(200)
            !article.content.isNullOrBlank() -> article.content.take(200)
            !article.title.isNullOrBlank() -> article.title
            else -> null
        }
        
        return ApiSuggestion(
            id = id,
            title = article.title ?: "Untitled",
            content = content,
            source = "news_api",
            sourceUrl = article.url,
            category = category,
            fitScore = 50, // Base score, will be recalculated with FitScoreCalculator
            cachedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
            // Extra fields from News API
            imageUrl = article.urlToImage,
            author = null, // News API doesn't provide author in this format
            publishedAt = article.publishedAt,
            sourceName = article.source?.name,
            score = null, // News API doesn't provide upvotes
            commentCount = null // News API doesn't provide comment count
        )
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
    
    /**
     * Generates mock suggestions for testing when API is unavailable.
     * This helps with development and testing without requiring network access.
     */
    suspend fun getMockSuggestions(): List<ApiSuggestion> {
        return withContext(Dispatchers.Default) {
            listOf(
                ApiSuggestion(
                    id = "mock_1",
                    title = "10-Minute Morning Workout Routine",
                    content = "A quick 10-minute workout routine that you can do every morning to start your day with energy and focus.",
                    source = "mock",
                    sourceUrl = null,
                    category = HabitCategory.FITNESS,
                    fitScore = 85,
                    cachedAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                ),
                ApiSuggestion(
                    id = "mock_2",
                    title = "Daily Meditation Practice",
                    content = "Start your day with 5 minutes of meditation to improve focus and reduce stress.",
                    source = "mock",
                    sourceUrl = null,
                    category = HabitCategory.WELLNESS,
                    fitScore = 80,
                    cachedAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                ),
                ApiSuggestion(
                    id = "mock_3",
                    title = "Read 10 Pages Daily",
                    content = "Build a reading habit by committing to just 10 pages per day. Small steps lead to big results.",
                    source = "mock",
                    sourceUrl = null,
                    category = HabitCategory.PRODUCTIVITY,
                    fitScore = 75,
                    cachedAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                ),
                ApiSuggestion(
                    id = "mock_4",
                    title = "Evening Stretch Routine",
                    content = "A 15-minute stretching routine to do before bed to improve flexibility and sleep quality.",
                    source = "mock",
                    sourceUrl = null,
                    category = HabitCategory.WELLNESS,
                    fitScore = 70,
                    cachedAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                ),
                ApiSuggestion(
                    id = "mock_5",
                    title = "Learn a New Skill",
                    content = "Dedicate 20 minutes daily to learning something new - a language, coding, or any skill you're interested in.",
                    source = "mock",
                    sourceUrl = null,
                    category = HabitCategory.LEARNING,
                    fitScore = 65,
                    cachedAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                )
            )
        }
    }
}

