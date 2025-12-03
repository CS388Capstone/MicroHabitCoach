package com.microhabitcoach.ui.stats

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.microhabitcoach.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var application: Application

    @Before
    fun setup() {
        application = Application()
    }

    @Test
    fun loadStats_doesNotCrash() = runTest {
        val viewModel = StatsViewModel(application)
        viewModel.loadStats()
        advanceUntilIdle()
        
        // Should not crash - currently just a placeholder
    }
}

