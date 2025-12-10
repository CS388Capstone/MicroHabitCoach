package com.microhabitcoach.data.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class DatabaseModuleTest {

    @After
    fun tearDown() {
        // Reset singleton instance between tests
        try {
            val field: Field = DatabaseModule::class.java.getDeclaredField("INSTANCE")
            field.isAccessible = true
            field.set(null, null)
        } catch (e: Exception) {
            // Ignore if reflection fails
        }
    }

    @Test
    fun getDatabase_returnsDatabaseInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val database1 = DatabaseModule.getDatabase(context)
        val database2 = DatabaseModule.getDatabase(context)

        assertNotNull(database1)
        assertNotNull(database2)
        // Should return same instance (singleton)
        assertSame(database1, database2)
    }

    @Test
    fun getDatabase_multipleCalls_returnsSameInstance() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val database1 = DatabaseModule.getDatabase(context)
        val database2 = DatabaseModule.getDatabase(context)
        val database3 = DatabaseModule.getDatabase(context)

        assertSame(database1, database2)
        assertSame(database2, database3)
    }

    @Test
    fun getDatabase_hasAllDaos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = DatabaseModule.getDatabase(context)

        assertNotNull(database.habitDao())
        assertNotNull(database.completionDao())
        assertNotNull(database.apiSuggestionDao())
        assertNotNull(database.savedArticleDao())
        assertNotNull(database.userPreferencesDao())
    }
}

