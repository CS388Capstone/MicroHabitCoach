package com.microhabitcoach.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.microhabitcoach.R
import com.microhabitcoach.data.database.DatabaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Test database connection
        testDatabase()
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

