package com.microhabitcoach.data.repository

import android.content.Context
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.data.database.entity.UserPreferences
import com.microhabitcoach.data.model.HabitCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for managing user preferences.
 * Provides getter/setter methods and ensures defaults are initialized.
 */
class PreferencesRepository(context: Context) {
    
    private val database = DatabaseModule.getDatabase(context)
    private val userPreferencesDao = database.userPreferencesDao()
    private val userId = "default_user"
    
    /**
     * Gets user preferences as Flow for reactive updates.
     */
    fun observePreferences(): Flow<UserPreferences?> {
        return userPreferencesDao.getUserPreferencesFlow(userId)
    }
    
    /**
     * Gets user preferences synchronously.
     */
    suspend fun getPreferences(): UserPreferences? {
        return userPreferencesDao.getUserPreferences(userId)
    }
    
    /**
     * Gets user preferences or creates default if none exist.
     */
    suspend fun getPreferencesOrCreateDefault(): UserPreferences {
        return userPreferencesDao.getUserPreferences(userId) 
            ?: createDefaultPreferences()
    }
    
    /**
     * Initializes default preferences if they don't exist.
     * Called on first app launch.
     */
    suspend fun initializeDefaultsIfNeeded(): UserPreferences {
        val existing = userPreferencesDao.getUserPreferences(userId)
        return if (existing == null) {
            createDefaultPreferences()
        } else {
            existing
        }
    }
    
    /**
     * Creates and saves default preferences.
     */
    private suspend fun createDefaultPreferences(): UserPreferences {
        val defaultPrefs = UserPreferences(
            userId = userId,
            preferredCategories = getDefaultPreferredCategories(),
            notificationsEnabled = true,
            quietHoursStart = null,
            quietHoursEnd = null,
            batteryOptimizationMode = false,
            hasCompletedOnboarding = false
        )
        userPreferencesDao.insertUserPreferences(defaultPrefs)
        return defaultPrefs
    }
    
    /**
     * Gets default preferred categories (all categories by default).
     */
    private fun getDefaultPreferredCategories(): Set<HabitCategory> {
        return HabitCategory.values().toSet()
    }
    
    // Getter methods
    
    /**
     * Gets preferred categories.
     */
    suspend fun getPreferredCategories(): Set<HabitCategory> {
        return getPreferencesOrCreateDefault().preferredCategories
    }
    
    /**
     * Gets notification preferences.
     */
    suspend fun areNotificationsEnabled(): Boolean {
        return getPreferencesOrCreateDefault().notificationsEnabled
    }
    
    /**
     * Gets quiet hours start time.
     */
    suspend fun getQuietHoursStart(): String? {
        return getPreferencesOrCreateDefault().quietHoursStart
    }
    
    /**
     * Gets quiet hours end time.
     */
    suspend fun getQuietHoursEnd(): String? {
        return getPreferencesOrCreateDefault().quietHoursEnd
    }
    
    /**
     * Gets battery optimization mode preference.
     */
    suspend fun isBatteryOptimizationEnabled(): Boolean {
        return getPreferencesOrCreateDefault().batteryOptimizationMode
    }
    
    /**
     * Checks if onboarding has been completed.
     */
    suspend fun hasCompletedOnboarding(): Boolean {
        return getPreferencesOrCreateDefault().hasCompletedOnboarding
    }
    
    // Setter methods
    
    /**
     * Updates preferred categories.
     */
    suspend fun setPreferredCategories(categories: Set<HabitCategory>) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedPrefs = currentPrefs.copy(
            preferredCategories = categories,
            updatedAt = System.currentTimeMillis()
        )
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
    
    /**
     * Adds a preferred category.
     */
    suspend fun addPreferredCategory(category: HabitCategory) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedCategories = currentPrefs.preferredCategories + category
        setPreferredCategories(updatedCategories)
    }
    
    /**
     * Removes a preferred category.
     */
    suspend fun removePreferredCategory(category: HabitCategory) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedCategories = currentPrefs.preferredCategories - category
        setPreferredCategories(updatedCategories)
    }
    
    /**
     * Sets notification preferences.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedPrefs = currentPrefs.copy(
            notificationsEnabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
    
    /**
     * Sets quiet hours.
     */
    suspend fun setQuietHours(startTime: String?, endTime: String?) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedPrefs = currentPrefs.copy(
            quietHoursStart = startTime,
            quietHoursEnd = endTime,
            updatedAt = System.currentTimeMillis()
        )
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
    
    /**
     * Sets battery optimization mode preference.
     */
    suspend fun setBatteryOptimizationMode(enabled: Boolean) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedPrefs = currentPrefs.copy(
            batteryOptimizationMode = enabled,
            updatedAt = System.currentTimeMillis()
        )
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
    
    /**
     * Marks onboarding as completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        val currentPrefs = getPreferencesOrCreateDefault()
        val updatedPrefs = currentPrefs.copy(
            hasCompletedOnboarding = completed,
            updatedAt = System.currentTimeMillis()
        )
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
    
    /**
     * Updates all preferences at once.
     */
    suspend fun updatePreferences(preferences: UserPreferences) {
        val updatedPrefs = preferences.copy(updatedAt = System.currentTimeMillis())
        userPreferencesDao.updateUserPreferences(updatedPrefs)
    }
}


