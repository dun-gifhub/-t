package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferenceManager
import com.example.telemetry.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Device Monitor", appName)
    }

    @Test
    fun `preference manager creates persistent UUID and defaults location sharing to false`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager(context)

        // Location sharing must be OFF by default per Requirement 10
        assertFalse("Location sharing must default to OFF", prefs.isLocationSharingEnabled)

        // Device UUID must be generated and valid UUID v4
        val uuid1 = prefs.deviceUuid
        assertNotNull(uuid1)
        assertEquals(36, uuid1.length)

        // Subsequent access should return exact same UUID
        val uuid2 = prefs.deviceUuid
        assertEquals(uuid1, uuid2)
    }

    @Test
    fun `validate location coordinate bounds`() {
        // Valid coordinates
        assertTrue(LocationHelper.isValidLocation(21.5944, 105.8442, 10f))
        assertTrue(LocationHelper.isValidLocation(0.0, 0.0, 0f))
        assertTrue(LocationHelper.isValidLocation(-90.0, -180.0, 5f))
        assertTrue(LocationHelper.isValidLocation(90.0, 180.0, 5f))

        // Invalid coordinates
        assertFalse(LocationHelper.isValidLocation(91.0, 105.0, 10f))
        assertFalse(LocationHelper.isValidLocation(-91.0, 105.0, 10f))
        assertFalse(LocationHelper.isValidLocation(21.0, 181.0, 10f))
        assertFalse(LocationHelper.isValidLocation(21.0, -181.0, 10f))
        assertFalse(LocationHelper.isValidLocation(21.0, 105.0, -1f))
    }

    @Test
    fun `validate register request model creation`() {
        val req = com.example.data.model.RegisterRequest(
            name = "Nguyen Van A",
            email = "user@test.com",
            password = "password123"
        )
        assertEquals("Nguyen Van A", req.name)
        assertEquals("user@test.com", req.email)
        assertEquals("password123", req.password)
    }

    @Test
    fun `validate telemetry report model creation and student prefs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager(context)

        prefs.studentName = "Nguyễn Minh Quân"
        prefs.className = "10A1"
        prefs.schoolName = "THPT Chuyên Lê Hồng Phong"

        assertEquals("Nguyễn Minh Quân", prefs.studentName)
        assertEquals("10A1", prefs.className)
        assertEquals("THPT Chuyên Lê Hồng Phong", prefs.schoolName)

        val report = com.example.data.model.DeviceTelemetryReportRequest(
            deviceUuid = prefs.deviceUuid,
            name = "Test Android",
            studentName = prefs.studentName,
            className = prefs.className,
            schoolName = prefs.schoolName,
            batteryLevel = 90,
            charging = true,
            networkType = "WIFI",
            latitude = 21.0285,
            longitude = 105.8542,
            accuracy = 10f
        )

        assertEquals("Nguyễn Minh Quân", report.studentName)
        assertEquals("10A1", report.className)
        assertEquals(90, report.batteryLevel)
        assertTrue(report.charging == true)
        assertEquals(21.0285, report.latitude!!, 0.0001)
    }
}
