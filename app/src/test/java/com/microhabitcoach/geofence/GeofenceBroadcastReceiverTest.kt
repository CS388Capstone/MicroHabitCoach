package com.microhabitcoach.geofence

import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.microhabitcoach.notification.GeofenceNotificationHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.Q])
class GeofenceBroadcastReceiverTest {

    private lateinit var context: Context
    private lateinit var intent: Intent
    private lateinit var receiver: GeofenceBroadcastReceiver
    private var currentEvent: GeofencingEvent? = null

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        intent = mockk(relaxed = true)
        receiver = GeofenceBroadcastReceiver()

        // Provide a safe fake event builder to avoid parcelable casting issues inside Robolectric
        GeofenceBroadcastReceiver.geofencingEventProvider = { currentEvent }
        mockkStatic(GeofencingEvent::class)
        mockkObject(GeofenceNotificationHandler)
        every { GeofenceNotificationHandler.onGeofenceTriggered(any(), any()) } returns Unit
    }

    @Test
    fun onReceive_enterTransition_handlesEnter() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)
        val geofence = mockk<Geofence>(relaxed = true)
        val habitId = "test-habit-id"

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { geofencingEvent.triggeringGeofences } returns listOf(geofence)
        every { geofence.requestId } returns habitId

        receiver.onReceive(context, intent)

        verify { GeofenceNotificationHandler.onGeofenceTriggered(context, habitId) }
    }

    @Test
    fun onReceive_exitTransition_doesNotHandle() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_EXIT

        receiver.onReceive(context, intent)

        verify(exactly = 0) { GeofenceNotificationHandler.onGeofenceTriggered(any(), any()) }
    }

    @Test
    fun onReceive_hasError_doesNotProcess() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns true
        every { geofencingEvent.errorCode } returns 1000

        receiver.onReceive(context, intent)

        verify(exactly = 0) { GeofenceNotificationHandler.onGeofenceTriggered(any(), any()) }
    }

    @Test
    fun onReceive_multipleGeofences_processesAll() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)
        val geofence1 = mockk<Geofence>(relaxed = true)
        val geofence2 = mockk<Geofence>(relaxed = true)

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { geofencingEvent.triggeringGeofences } returns listOf(geofence1, geofence2)
        every { geofence1.requestId } returns "habit-1"
        every { geofence2.requestId } returns "habit-2"

        receiver.onReceive(context, intent)

        verify { GeofenceNotificationHandler.onGeofenceTriggered(context, "habit-1") }
        verify { GeofenceNotificationHandler.onGeofenceTriggered(context, "habit-2") }
    }

    @Test
    fun onReceive_nullGeofences_doesNotProcess() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { geofencingEvent.triggeringGeofences } returns null

        receiver.onReceive(context, intent)

        verify(exactly = 0) { GeofenceNotificationHandler.onGeofenceTriggered(any(), any()) }
    }

    @Test
    fun onReceive_emptyGeofences_doesNotProcess() {
        val geofencingEvent = mockk<GeofencingEvent>(relaxed = true)

        currentEvent = geofencingEvent
        every { geofencingEvent.hasError() } returns false
        every { geofencingEvent.geofenceTransition } returns Geofence.GEOFENCE_TRANSITION_ENTER
        every { geofencingEvent.triggeringGeofences } returns emptyList()

        receiver.onReceive(context, intent)

        verify(exactly = 0) { GeofenceNotificationHandler.onGeofenceTriggered(any(), any()) }
    }
}

