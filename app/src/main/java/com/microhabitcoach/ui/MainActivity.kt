package com.microhabitcoach.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.microhabitcoach.R
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.databinding.ActivityMainBinding
import com.microhabitcoach.activity.ActivityRecognitionService
import com.microhabitcoach.geofence.GeofenceService
import com.microhabitcoach.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup navigation
        setupNavigation()
        
        // Initialize notification service
        NotificationService.initialize(this)
        
        // Test database connection and initialize services
        testDatabase()
        
        // Sync geofences on app startup
        syncGeofences()
        
        // Initialize Activity Recognition for motion-based habits
        initializeActivityRecognition()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configure AppBar with top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.todayFragment,
                R.id.exploreFragment,
                R.id.statsFragment,
                R.id.settingsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment
        return navHostFragment?.navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
    
    private fun testDatabase() {
        activityScope.launch {
            try {
                // Test database by accessing it
                // This will create the database if it doesn't exist
                val database = DatabaseModule.getDatabase(applicationContext)
                database.habitDao() // Access DAO to initialize database
                
                // Initialize default user preferences on first launch
                initializeUserPreferences()
                
                // Database initialized successfully - app is ready
                // (UI updates can be added later when binding is working)
                
            } catch (e: Exception) {
                // Error handling - can add UI feedback later
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Initializes default user preferences on first app launch.
     * This ensures preferences are available for FitScore calculation and other features.
     */
    private fun initializeUserPreferences() {
        activityScope.launch {
            try {
                val preferencesRepository = com.microhabitcoach.data.repository.PreferencesRepository(applicationContext)
                preferencesRepository.initializeDefaultsIfNeeded()
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Syncs all geofences on app startup.
     * Ensures all active location-based habits have geofences registered,
     * and removes geofences for habits that no longer exist or are inactive.
     */
    private fun syncGeofences() {
        activityScope.launch {
            try {
                GeofenceService.updateGeofences(applicationContext)
            } catch (e: Exception) {
                // Log error but don't crash the app - geofencing is not critical for app launch
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Initializes Activity Recognition service on app startup.
     * This enables auto-completion for motion-based habits.
     */
    private fun initializeActivityRecognition() {
        try {
            ActivityRecognitionService.startMonitoring(applicationContext)
        } catch (e: Exception) {
            // Log error but don't crash the app - activity recognition requires permissions
            e.printStackTrace()
        }
    }
}

