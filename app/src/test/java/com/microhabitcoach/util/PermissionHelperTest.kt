package com.microhabitcoach.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class PermissionHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun hasActivityRecognitionPermission_androidQPlus_granted_returnsTrue() {
        setSdkInt(Build.VERSION_CODES.Q)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) } returns PackageManager.PERMISSION_GRANTED

        val result = PermissionHelper.hasActivityRecognitionPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasActivityRecognitionPermission_androidQPlus_denied_returnsFalse() {
        setSdkInt(Build.VERSION_CODES.Q)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) } returns PackageManager.PERMISSION_DENIED

        val result = PermissionHelper.hasActivityRecognitionPermission(context)

        assertFalse(result)
    }

    @Test
    fun hasActivityRecognitionPermission_androidBelowQ_returnsTrue() {
        setSdkInt(Build.VERSION_CODES.P)

        val result = PermissionHelper.hasActivityRecognitionPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasLocationPermission_fineLocationGranted_returnsTrue() {
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_DENIED

        val result = PermissionHelper.hasLocationPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasLocationPermission_coarseLocationGranted_returnsTrue() {
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        val result = PermissionHelper.hasLocationPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasLocationPermission_bothDenied_returnsFalse() {
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) } returns PackageManager.PERMISSION_DENIED

        val result = PermissionHelper.hasLocationPermission(context)

        assertFalse(result)
    }

    @Test
    fun hasBackgroundLocationPermission_androidQPlus_granted_returnsTrue() {
        setSdkInt(Build.VERSION_CODES.Q)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        val result = PermissionHelper.hasBackgroundLocationPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasBackgroundLocationPermission_androidQPlus_denied_returnsFalse() {
        setSdkInt(Build.VERSION_CODES.Q)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED

        val result = PermissionHelper.hasBackgroundLocationPermission(context)

        assertFalse(result)
    }

    @Test
    fun hasBackgroundLocationPermission_androidBelowQ_usesForegroundPermission() {
        setSdkInt(Build.VERSION_CODES.P)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        val result = PermissionHelper.hasBackgroundLocationPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasNotificationPermission_androidTiramisuPlus_granted_returnsTrue() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_GRANTED

        val result = PermissionHelper.hasNotificationPermission(context)

        assertTrue(result)
    }

    @Test
    fun hasNotificationPermission_androidTiramisuPlus_denied_returnsFalse() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_DENIED

        val result = PermissionHelper.hasNotificationPermission(context)

        assertFalse(result)
    }

    @Test
    fun hasNotificationPermission_androidBelowTiramisu_returnsTrue() {
        setSdkInt(Build.VERSION_CODES.S)

        val result = PermissionHelper.hasNotificationPermission(context)

        assertTrue(result)
    }

    @Test
    fun getActivityRecognitionPermissionExplanation_returnsString() {
        every { context.getString(com.microhabitcoach.R.string.permission_activity_recognition_explanation) } returns "Test explanation"

        val result = PermissionHelper.getActivityRecognitionPermissionExplanation(context)

        assert(result.isNotEmpty())
    }

    @Test
    fun getLocationPermissionExplanation_returnsString() {
        every { context.getString(com.microhabitcoach.R.string.permission_location_explanation) } returns "Test explanation"

        val result = PermissionHelper.getLocationPermissionExplanation(context)

        assert(result.isNotEmpty())
    }

    @Test
    fun getBackgroundLocationPermissionExplanation_returnsString() {
        every { context.getString(com.microhabitcoach.R.string.permission_background_location_explanation) } returns "Test explanation"

        val result = PermissionHelper.getBackgroundLocationPermissionExplanation(context)

        assert(result.isNotEmpty())
    }

    @Test
    fun getNotificationPermissionExplanation_returnsString() {
        every { context.getString(com.microhabitcoach.R.string.permission_notification_explanation) } returns "Test explanation"

        val result = PermissionHelper.getNotificationPermissionExplanation(context)

        assert(result.isNotEmpty())
    }

    private fun setSdkInt(version: Int) {
        // Robolectric 4.12.1 lacks ShadowBuild.setVersionInt; set the static field directly.
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", version)
    }
}

