package com.example.data.repository

import android.content.Context
import android.os.Build
import com.example.BuildConfig
import com.example.data.api.ApiClient
import com.example.data.local.PreferenceManager
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.DeviceRegisterRequest
import com.example.data.model.HeartbeatRequest
import com.example.data.model.LocationUploadRequest
import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.TelemetryState
import com.example.telemetry.BatteryHelper
import com.example.telemetry.LocationHelper
import com.example.telemetry.NetworkHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.net.SocketTimeoutException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int? = null, val message: String) : ApiResult<Nothing>()
}

class DeviceRepository(
    private val context: Context,
    val prefs: PreferenceManager
) {

    private val _telemetryState = MutableStateFlow(
        TelemetryState(
            locationSharingEnabled = prefs.isLocationSharingEnabled,
            latitude = prefs.lastLatitude,
            longitude = prefs.lastLongitude,
            accuracy = prefs.lastAccuracy,
            lastSyncTime = if (prefs.lastSyncTime > 0) prefs.lastSyncTime else null,
            syncStatus = DeviceConnectionStatus.IDLE
        )
    )
    val telemetryState: StateFlow<TelemetryState> = _telemetryState.asStateFlow()

    private var consecutiveFailures = 0

    fun updateTelemetrySnapshot() {
        val battery = BatteryHelper.getBatteryInfo(context)
        val network = NetworkHelper.getNetworkType(context)
        val hasLocPerm = LocationHelper.hasLocationPermission(context)
        val locSharing = prefs.isLocationSharingEnabled

        _telemetryState.update { current ->
            current.copy(
                batteryLevel = battery.level,
                isCharging = battery.isCharging,
                networkType = network,
                locationPermissionGranted = hasLocPerm,
                locationSharingEnabled = locSharing
            )
        }
    }

    suspend fun register(name: String, email: String, pass: String): ApiResult<String> {
        return try {
            val api = ApiClient.getApiService(prefs)
            var response = api.register(RegisterRequest(name = name.trim(), email = email.trim(), password = pass))
            if (response.code() == 404) {
                response = api.signup(RegisterRequest(name = name.trim(), email = email.trim(), password = pass))
            }

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token ?: body?.data?.token
                if (!token.isNullOrBlank()) {
                    prefs.authToken = token
                    prefs.userEmail = email.trim()
                    consecutiveFailures = 0
                    syncDeviceRegistration()
                    ApiResult.Success(token)
                } else {
                    // Registration succeeded, try immediate auto-login
                    val loginRes = login(email, pass)
                    if (loginRes is ApiResult.Success) {
                        loginRes
                    } else {
                        ApiResult.Success("Đăng ký thành công! Đang chuyển đến đăng nhập...")
                    }
                }
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Thông tin không hợp lệ hoặc email đã tồn tại."
                    409 -> "Email này đã được sử dụng. Vui lòng đăng nhập."
                    422 -> "Dữ liệu đăng ký không đúng định dạng."
                    429 -> "Quá nhiều yêu cầu. Vui lòng thử lại sau giây lát."
                    500 -> "Lỗi hệ thống máy chủ (500). Vui lòng thử lại sau."
                    else -> "Đăng ký thất bại (Mã lỗi: ${response.code()})"
                }
                ApiResult.Error(response.code(), errorMsg)
            }
        } catch (e: SocketTimeoutException) {
            ApiResult.Error(null, "Không thể kết nối đến máy chủ (Hết thời gian phản hồi)")
        } catch (e: IOException) {
            ApiResult.Error(null, "Mất kết nối mạng hoặc máy chủ không phản hồi")
        } catch (e: Exception) {
            ApiResult.Error(null, e.localizedMessage ?: "Lỗi không xác định")
        }
    }

    suspend fun login(email: String, pass: String): ApiResult<String> {
        return try {
            val api = ApiClient.getApiService(prefs)
            val response = api.login(LoginRequest(email = email.trim(), password = pass))

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token ?: body?.data?.token
                if (!token.isNullOrBlank()) {
                    prefs.authToken = token
                    prefs.userEmail = email.trim()
                    consecutiveFailures = 0

                    // Check existing linked devices or prepare link
                    syncDeviceRegistration()
                    ApiResult.Success(token)
                } else {
                    ApiResult.Error(response.code(), "Không nhận được mã xác thực hợp lệ từ máy chủ")
                }
            } else {
                val msg = when (response.code()) {
                    401 -> "Tài khoản hoặc mật khẩu không chính xác."
                    403 -> "Tài khoản bị từ chối truy cập."
                    429 -> "Quá nhiều yêu cầu. Vui lòng thử lại sau giây lát."
                    500 -> "Lỗi hệ thống máy chủ (500). Vui lòng thử lại sau."
                    else -> "Đăng nhập thất bại (Mã lỗi: ${response.code()})"
                }
                ApiResult.Error(response.code(), msg)
            }
        } catch (e: SocketTimeoutException) {
            ApiResult.Error(null, "Không thể kết nối đến máy chủ (Hết thời gian phản hồi)")
        } catch (e: IOException) {
            ApiResult.Error(null, "Mất kết nối mạng hoặc máy chủ không phản hồi")
        } catch (e: Exception) {
            ApiResult.Error(null, e.localizedMessage ?: "Lỗi không xác định")
        }
    }

    suspend fun syncDeviceRegistration(): ApiResult<String> {
        val existingDeviceId = prefs.deviceId
        val api = ApiClient.getApiService(prefs)

        return try {
            // Check existing devices for this user
            val listResp = api.getDevices()
            if (listResp.isSuccessful) {
                val devices = listResp.body()?.data
                val matched = devices?.find { it.deviceUuid == prefs.deviceUuid }
                if (matched?.id != null) {
                    prefs.deviceId = matched.id
                    prefs.deviceName = matched.name ?: prefs.deviceName
                    return ApiResult.Success(matched.id)
                }
            }

            // Register device if not found
            val registerResp = api.registerDevice(
                DeviceRegisterRequest(
                    name = prefs.deviceName,
                    deviceUuid = prefs.deviceUuid,
                    platform = "Android",
                    osVersion = Build.VERSION.RELEASE ?: "Android",
                    appVersion = BuildConfig.VERSION_NAME
                )
            )

            if (registerResp.isSuccessful) {
                val devId = registerResp.body()?.data?.id
                if (!devId.isNullOrBlank()) {
                    prefs.deviceId = devId
                    ApiResult.Success(devId)
                } else {
                    ApiResult.Error(registerResp.code(), "Đăng ký thành công nhưng thiếu Device ID")
                }
            } else {
                ApiResult.Error(registerResp.code(), "Không thể liên kết thiết bị (${registerResp.code()})")
            }
        } catch (e: Exception) {
            if (existingDeviceId != null) {
                ApiResult.Success(existingDeviceId)
            } else {
                ApiResult.Error(null, e.localizedMessage ?: "Lỗi kết nối khi liên kết thiết bị")
            }
        }
    }

    suspend fun registerDeviceExplicit(name: String): ApiResult<String> {
        prefs.deviceName = name.trim()
        val api = ApiClient.getApiService(prefs)

        return try {
            val registerResp = api.registerDevice(
                DeviceRegisterRequest(
                    name = name.trim(),
                    deviceUuid = prefs.deviceUuid,
                    platform = "Android",
                    osVersion = Build.VERSION.RELEASE ?: "Android",
                    appVersion = BuildConfig.VERSION_NAME
                )
            )

            if (registerResp.isSuccessful) {
                val devId = registerResp.body()?.data?.id
                if (!devId.isNullOrBlank()) {
                    prefs.deviceId = devId
                    ApiResult.Success(devId)
                } else {
                    ApiResult.Error(registerResp.code(), "Đăng ký thành công nhưng không có Device ID")
                }
            } else {
                val msg = when (registerResp.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                    403 -> "Bạn không có quyền đăng ký thiết bị."
                    else -> "Lỗi đăng ký thiết bị (Mã lỗi: ${registerResp.code()})"
                }
                ApiResult.Error(registerResp.code(), msg)
            }
        } catch (e: SocketTimeoutException) {
            ApiResult.Error(null, "Không thể kết nối đến máy chủ (Timeout)")
        } catch (e: Exception) {
            ApiResult.Error(null, e.localizedMessage ?: "Lỗi mạng khi đăng ký thiết bị")
        }
    }

    suspend fun sendHeartbeat(status: String = "ONLINE"): ApiResult<Boolean> {
        val devId = prefs.deviceId ?: return ApiResult.Error(null, "Thiết bị chưa được liên kết")
        val battery = BatteryHelper.getBatteryInfo(context)
        val network = NetworkHelper.getNetworkType(context)
        val hasLocPerm = LocationHelper.hasLocationPermission(context)
        val locSharing = prefs.isLocationSharingEnabled

        _telemetryState.update { it.copy(syncStatus = DeviceConnectionStatus.SYNCING) }

        return try {
            val api = ApiClient.getApiService(prefs)
            val response = api.sendHeartbeat(
                id = devId,
                body = HeartbeatRequest(
                    batteryLevel = battery.level,
                    charging = battery.isCharging,
                    networkType = network,
                    locationPermission = hasLocPerm,
                    locationSharing = locSharing,
                    status = status
                )
            )

            if (response.isSuccessful) {
                consecutiveFailures = 0
                val now = System.currentTimeMillis()
                prefs.lastSyncTime = now
                prefs.lastStatus = status

                _telemetryState.update {
                    it.copy(
                        batteryLevel = battery.level,
                        isCharging = battery.isCharging,
                        networkType = network,
                        locationPermissionGranted = hasLocPerm,
                        locationSharingEnabled = locSharing,
                        lastSyncTime = now,
                        syncStatus = DeviceConnectionStatus.ONLINE,
                        lastError = null
                    )
                }
                ApiResult.Success(true)
            } else {
                consecutiveFailures++
                val errMsg = when (response.code()) {
                    401 -> "Phiên đăng nhập hết hạn (401)"
                    403 -> "Không có quyền cập nhật thiết bị này (403)"
                    404 -> "Thiết bị không tồn tại trên hệ thống (404)"
                    429 -> "Gửi quá nhanh, đang chờ giãn cách (429)"
                    else -> "Lỗi gửi heartbeat (${response.code()})"
                }
                _telemetryState.update {
                    it.copy(
                        syncStatus = DeviceConnectionStatus.OFFLINE,
                        lastError = errMsg
                    )
                }
                ApiResult.Error(response.code(), errMsg)
            }
        } catch (e: Exception) {
            consecutiveFailures++
            val errMsg = "Mất kết nối máy chủ"
            _telemetryState.update {
                it.copy(
                    syncStatus = DeviceConnectionStatus.OFFLINE,
                    lastError = errMsg
                )
            }
            ApiResult.Error(null, errMsg)
        }
    }

    suspend fun sendLocationTelemetry(): ApiResult<Boolean> {
        val devId = prefs.deviceId ?: return ApiResult.Error(null, "Thiết bị chưa được liên kết")

        if (!prefs.isLocationSharingEnabled) {
            return ApiResult.Error(null, "Chia sẻ vị trí đang tắt")
        }
        if (!LocationHelper.hasLocationPermission(context)) {
            return ApiResult.Error(null, "Chưa cấp quyền truy cập vị trí")
        }

        val location = LocationHelper.getCurrentLocation(context)
            ?: return ApiResult.Error(null, "Không thể lấy tín hiệu GPS lúc này")

        return try {
            val api = ApiClient.getApiService(prefs)
            val response = api.sendLocation(
                id = devId,
                body = LocationUploadRequest(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = location.timestamp
                )
            )

            if (response.isSuccessful) {
                prefs.saveLastCoordinates(location.latitude, location.longitude, location.accuracy)
                _telemetryState.update {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        locationTimestamp = location.timestamp
                    )
                }
                ApiResult.Success(true)
            } else {
                ApiResult.Error(response.code(), "Lỗi gửi vị trí (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error(null, e.localizedMessage ?: "Lỗi mạng khi gửi vị trí")
        }
    }

    suspend fun syncAllNow(): Boolean {
        updateTelemetrySnapshot()
        val hb = sendHeartbeat("ONLINE")
        if (prefs.isLocationSharingEnabled && LocationHelper.hasLocationPermission(context)) {
            sendLocationTelemetry()
        }
        return hb is ApiResult.Success
    }

    fun setLocationSharing(enabled: Boolean) {
        prefs.isLocationSharingEnabled = enabled
        updateTelemetrySnapshot()
    }

    fun logout() {
        prefs.clearAuth()
        ApiClient.invalidate()
        _telemetryState.update {
            TelemetryState(
                syncStatus = DeviceConnectionStatus.IDLE
            )
        }
    }

    fun getBackoffDelayMillis(): Long {
        val factor = consecutiveFailures.coerceAtMost(5)
        return (1000L * (1 shl factor)).coerceIn(1000L, 30000L)
    }
}
