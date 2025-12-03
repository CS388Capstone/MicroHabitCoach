package com.microhabitcoach.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseModule {
    
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    /**
     * Migration from version 2 to 3: Add saved_articles table and extra fields to api_suggestions.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create saved_articles table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS saved_articles (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    content TEXT,
                    source TEXT NOT NULL,
                    sourceUrl TEXT,
                    imageUrl TEXT,
                    author TEXT,
                    publishedAt TEXT,
                    sourceName TEXT,
                    savedAt INTEGER NOT NULL,
                    category TEXT,
                    originalFitScore INTEGER,
                    score INTEGER,
                    commentCount INTEGER
                )
            """.trimIndent())
            
            // Add indices for saved_articles
            database.execSQL("CREATE INDEX IF NOT EXISTS index_saved_articles_savedAt ON saved_articles(savedAt)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_saved_articles_source ON saved_articles(source)")
            
            // Add new columns to api_suggestions table (if they don't exist)
            // Note: SQLite doesn't support IF NOT EXISTS for ALTER TABLE ADD COLUMN in older versions
            // We'll use a try-catch approach or check if column exists
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN imageUrl TEXT")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN author TEXT")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN publishedAt TEXT")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN sourceName TEXT")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN score INTEGER")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
            try {
                database.execSQL("ALTER TABLE api_suggestions ADD COLUMN commentCount INTEGER")
            } catch (e: Exception) {
                // Column might already exist, ignore
            }
        }
    }
    
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
            INSTANCE = instance
            instance
        }
    }
}

