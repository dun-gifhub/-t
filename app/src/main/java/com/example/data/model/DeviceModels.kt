package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "token") val token: String? = null,
    @Json(name = "accessToken") val accessToken: String? = null,
    @Json(name = "access_token") val accessTokenSnake: String? = null,
    @Json(name = "jwt") val jwt: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "user") val user: UserInfo? = null,
    @Json(name = "data") val data: LoginData? = null
) {
    fun extractToken(): String? =
        token ?: accessToken ?: accessTokenSnake ?: jwt
            ?: data?.token ?: data?.accessToken ?: data?.accessTokenSnake ?: data?.jwt
}

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "fullName") val fullName: String = name,
    @Json(name = "username") val username: String = name
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "accessToken") val accessToken: String? = null,
    @Json(name = "access_token") val accessTokenSnake: String? = null,
    @Json(name = "jwt") val jwt: String? = null,
    @Json(name = "user") val user: UserInfo? = null,
    @Json(name = "data") val data: LoginData? = null
) {
    fun extractToken(): String? =
        token ?: accessToken ?: accessTokenSnake ?: jwt
            ?: data?.token ?: data?.accessToken ?: data?.accessTokenSnake ?: data?.jwt
}

@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "token") val token: String? = null,
    @Json(name = "accessToken") val accessToken: String? = null,
    @Json(name = "access_token") val accessTokenSnake: String? = null,
    @Json(name = "jwt") val jwt: String? = null,
    @Json(name = "user") val user: UserInfo? = null
)

@JsonClass(generateAdapter = true)
data class UserInfo(
    @Json(name = "id") val id: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "deviceUuid") val deviceUuid: String,
    @Json(name = "platform") val platform: String = "Android",
    @Json(name = "osVersion") val osVersion: String,
    @Json(name = "appVersion") val appVersion: String
)

@JsonClass(generateAdapter = true)
data class DeviceRegisterResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "device") val device: DeviceData? = null,
    @Json(name = "data") val data: DeviceData? = null
) {
    fun extractDeviceId(): String? = data?.id ?: device?.id ?: id
}

@JsonClass(generateAdapter = true)
data class DeviceData(
    @Json(name = "id") val id: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "deviceUuid") val deviceUuid: String? = null,
    @Json(name = "studentName") val studentName: String? = null,
    @Json(name = "studentId") val studentId: String? = null,
    @Json(name = "schoolName") val schoolName: String? = null,
    @Json(name = "grade") val grade: String? = null,
    @Json(name = "className") val className: String? = null,
    @Json(name = "parentPhone") val parentPhone: String? = null,
    @Json(name = "platform") val platform: String? = null,
    @Json(name = "osVersion") val osVersion: String? = null,
    @Json(name = "appVersion") val appVersion: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "batteryLevel") val batteryLevel: Int? = null,
    @Json(name = "charging") val charging: Boolean? = null,
    @Json(name = "networkType") val networkType: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "accuracy") val accuracy: Float? = null,
    @Json(name = "lastSeen") val lastSeen: String? = null,
    @Json(name = "isUninstalled") val isUninstalled: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class DeviceTelemetryReportRequest(
    @Json(name = "deviceUuid") val deviceUuid: String,
    @Json(name = "name") val name: String,
    @Json(name = "studentName") val studentName: String? = null,
    @Json(name = "studentId") val studentId: String? = null,
    @Json(name = "schoolName") val schoolName: String? = null,
    @Json(name = "grade") val grade: String? = null,
    @Json(name = "className") val className: String? = null,
    @Json(name = "parentPhone") val parentPhone: String? = null,
    @Json(name = "platform") val platform: String = "Android",
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "accuracy") val accuracy: Float? = null,
    @Json(name = "batteryLevel") val batteryLevel: Int? = null,
    @Json(name = "charging") val charging: Boolean? = null,
    @Json(name = "networkType") val networkType: String? = null,
    @Json(name = "currentApp") val currentApp: String? = "Device Monitor Android",
    @Json(name = "currentWebsite") val currentWebsite: String? = "qu-n-l-s1k1.onrender.com",
    @Json(name = "status") val status: String = "ONLINE",
    @Json(name = "isUninstalled") val isUninstalled: Boolean = false,
    @Json(name = "isNoNetwork") val isNoNetwork: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DeviceTelemetryReportResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: DeviceData? = null
)

@JsonClass(generateAdapter = true)
data class DeviceUninstallRequest(
    @Json(name = "deviceUuid") val deviceUuid: String,
    @Json(name = "studentName") val studentName: String? = null,
    @Json(name = "isUninstalled") val isUninstalled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class DeviceUninstallResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: DeviceData? = null
)

@JsonClass(generateAdapter = true)
data class DeviceListResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "devices") val devices: List<DeviceData>? = null,
    @Json(name = "data") val data: List<DeviceData>? = null
) {
    fun extractDevices(): List<DeviceData>? = data ?: devices
}

@JsonClass(generateAdapter = true)
data class HeartbeatRequest(
    @Json(name = "batteryLevel") val batteryLevel: Int?,
    @Json(name = "charging") val charging: Boolean?,
    @Json(name = "networkType") val networkType: String,
    @Json(name = "locationPermission") val locationPermission: Boolean,
    @Json(name = "locationSharing") val locationSharing: Boolean,
    @Json(name = "status") val status: String = "ONLINE"
)

@JsonClass(generateAdapter = true)
data class HeartbeatResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class LocationUploadRequest(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "accuracy") val accuracy: Float,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class LocationUploadResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null
)

data class BatteryInfo(
    val level: Int?,
    val isCharging: Boolean?
)

data class TelemetryState(
    val batteryLevel: Int? = null,
    val isCharging: Boolean? = null,
    val networkType: String = "UNKNOWN",
    val locationPermissionGranted: Boolean = false,
    val locationSharingEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val locationTimestamp: String? = null,
    val lastSyncTime: Long? = null,
    val syncStatus: DeviceConnectionStatus = DeviceConnectionStatus.IDLE,
    val lastError: String? = null
)

enum class DeviceConnectionStatus {
    ONLINE,
    OFFLINE,
    IDLE,
    SYNCING
}
