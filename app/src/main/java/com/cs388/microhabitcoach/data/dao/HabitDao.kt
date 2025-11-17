package com.cs388.microhabitcoach.data.dao

import androidx.room.*
import com.cs388.microhabitcoach.data.entities.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun getHabitById(id: Long): Flow<Habit?>

    @Query("SELECT * FROM habits ORDER BY updatedAt DESC")
    fun getAllHabitsIncludingArchived(): Flow<List<Habit>>
}
