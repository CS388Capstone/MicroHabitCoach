package com.microhabitcoach.ui.habit

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.data.repository.FakeHabitRepository
import com.microhabitcoach.testing.getOrAwaitValue
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditHabitViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val application = Application()

    @Test
    fun validateFormFailsWithoutName() {
        val viewModel = AddEditHabitViewModel(application, FakeHabitRepository())
        val result = viewModel.validateForm(
            name = "",
            type = HabitType.TIME,
            reminderTimes = listOf(LocalTime.NOON),
            reminderDays = listOf(1),
            motionType = null,
            duration = null,
            hasLocation = true,
            radius = null
        )
        assertFalse(result.isValid)
    }

    @Test
    fun saveHabitPersistsDataAndEmitsSuccess() = runTest {
        val repository = FakeHabitRepository()
        val viewModel = AddEditHabitViewModel(application, repository)

        viewModel.saveHabit(
            existingHabit = null,
            name = "Deep Work",
            category = HabitCategory.PRODUCTIVITY,
            type = HabitType.TIME,
            reminderTimes = listOf(LocalTime.NOON),
            reminderDays = listOf(1, 3, 5),
            motionType = null,
            targetDuration = null,
            location = null,
            geofenceRadius = null
        )

        advanceUntilIdle()

        val state = viewModel.saveState.getOrAwaitValue()
        assertTrue(state is AddEditHabitViewModel.SaveState.Success)
        assertEquals("Deep Work", repository.lastSavedHabit?.name)
    }

    @Test
    fun loadHabitPopulatesFormState() = runTest {
        val repository = FakeHabitRepository()
        val habitId = UUID.randomUUID().toString()
        repository.saveHabit(
            TestHabitFactory.create(
                id = habitId,
                name = "Evening Walk",
                type = HabitType.MOTION,
                motionType = "Walk",
                targetDuration = 20
            )
        )
        val viewModel = AddEditHabitViewModel(application, repository)

        viewModel.loadHabit(habitId)
        advanceUntilIdle()

        val loadedHabit = viewModel.habit.getOrAwaitValue()
        assertEquals(habitId, loadedHabit?.id)
    }

    private object TestHabitFactory {
        fun create(
            id: String = UUID.randomUUID().toString(),
            name: String = "Sample",
            type: HabitType = HabitType.TIME,
            motionType: String? = null,
            targetDuration: Int? = null,
            location: LocationData? = null,
            geofenceRadius: Float? = null
        ) = com.microhabitcoach.data.database.entity.Habit(
            id = id,
            name = name,
            category = HabitCategory.GENERAL,
            type = type,
            motionType = motionType,
            targetDuration = targetDuration,
            location = location,
            geofenceRadius = geofenceRadius,
            reminderTimes = listOf(LocalTime.NOON),
            reminderDays = listOf(1),
            streakCount = 0
        )
    }
}

