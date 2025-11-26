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
        
        // Test database connection
        testDatabase()
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
    
    private fun testDatabase() {
        activityScope.launch {
            try {
                // Test database by accessing it
                // This will create the database if it doesn't exist
                val database = DatabaseModule.getDatabase(applicationContext)
                database.habitDao() // Access DAO to initialize database
                
                // Database initialized successfully - app is ready
                // (UI updates can be added later when binding is working)
                
            } catch (e: Exception) {
                // Error handling - can add UI feedback later
                e.printStackTrace()
            }
        }
    }
}

