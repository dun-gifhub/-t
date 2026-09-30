package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.UUID

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "device_monitor_secure_prefs"
        private const val KEY_AUTH_TOKEN = "key_auth_token"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_API_BASE_URL = "key_api_base_url"
        private const val KEY_DEVICE_UUID = "key_device_uuid"
        private const val KEY_DEVICE_ID = "key_device_id"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_LOCATION_SHARING = "key_location_sharing"
        private const val KEY_HEARTBEAT_INTERVAL = "key_heartbeat_interval"
        private const val KEY_BG_SERVICE = "key_bg_service"
        private const val KEY_LAST_SYNC_TIME = "key_last_sync_time"
        private const val KEY_LAST_STATUS = "key_last_status"
        private const val KEY_LAST_LATITUDE = "key_last_latitude"
        private const val KEY_LAST_LONGITUDE = "key_last_longitude"
        private const val KEY_LAST_ACCURACY = "key_last_accuracy"

        const val DEFAULT_API_BASE_URL = "https://qu-n-l-s1k1.onrender.com"
    }

    init {
        val savedUrl = prefs.getString(KEY_API_BASE_URL, null)
        if (savedUrl != null && (savedUrl.contains("ais-dev-") || savedUrl.contains("run.app"))) {
            prefs.edit().putString(KEY_API_BASE_URL, "$DEFAULT_API_BASE_URL/").apply()
        }
    }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var apiBaseUrl: String
        get() {
            val saved = prefs.getString(KEY_API_BASE_URL, null)
            if (saved.isNullOrBlank() || saved.contains("ais-dev-") || saved.contains("run.app")) {
                return DEFAULT_API_BASE_URL
            }
            return saved.trimEnd('/')
        }
        set(value) {
            val trimmed = value.trim()
            val withScheme = if (trimmed.isNotEmpty() && !trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
            val cleanUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
            prefs.edit().putString(KEY_API_BASE_URL, cleanUrl).apply()
        }

    val deviceUuid: String
        get() {
            var uuid = prefs.getString(KEY_DEVICE_UUID, null)
            if (uuid.isNullOrBlank()) {
                uuid = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply()
            }
            return uuid
        }

    var deviceId: String?
        get() = prefs.getString(KEY_DEVICE_ID, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var deviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, null) ?: "${Build.MANUFACTURER} ${Build.MODEL}"
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()

    var isLocationSharingEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCATION_SHARING, false) // Default OFF per user requirement
        set(value) = prefs.edit().putBoolean(KEY_LOCATION_SHARING, value).apply()

    var heartbeatIntervalSeconds: Int
        get() = prefs.getInt(KEY_HEARTBEAT_INTERVAL, 30) // 30s foreground, 300s background
        set(value) = prefs.edit().putInt(KEY_HEARTBEAT_INTERVAL, value).apply()

    var isBackgroundServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_BG_SERVICE, true)
        set(value) = prefs.edit().putBoolean(KEY_BG_SERVICE, value).apply()

    var lastSyncTime: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TIME, value).apply()

    var lastStatus: String
        get() = prefs.getString(KEY_LAST_STATUS, "IDLE") ?: "IDLE"
        set(value) = prefs.edit().putString(KEY_LAST_STATUS, value).apply()

    fun saveLastCoordinates(lat: Double, lng: Double, accuracy: Float) {
        prefs.edit()
            .putString(KEY_LAST_LATITUDE, lat.toString())
            .putString(KEY_LAST_LONGITUDE, lng.toString())
            .putFloat(KEY_LAST_ACCURACY, accuracy)
            .apply()
    }

    val lastLatitude: Double?
        get() = prefs.getString(KEY_LAST_LATITUDE, null)?.toDoubleOrNull()

    val lastLongitude: Double?
        get() = prefs.getString(KEY_LAST_LONGITUDE, null)?.toDoubleOrNull()

    val lastAccuracy: Float?
        get() = if (prefs.contains(KEY_LAST_ACCURACY)) prefs.getFloat(KEY_LAST_ACCURACY, 0f) else null

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_DEVICE_ID)
            .apply()
    }
}
