package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DeviceMonitorApp
import com.example.data.repository.ApiResult
import com.example.service.DeviceMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    LINK_DEVICE,
    DASHBOARD
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.LOGIN,
    val selectedTab: Int = 0, // 0: Home, 1: Location, 2: Settings
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSyncingNow: Boolean = false,
    val userEmail: String = "",
    val deviceName: String = "",
    val deviceId: String = "",
    val deviceUuid: String = "",
    val apiBaseUrl: String = "",
    val isLocationSharingEnabled: Boolean = false,
    val isBackgroundServiceEnabled: Boolean = true,
    val heartbeatInterval: Int = 30
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DeviceMonitorApp
    private val repo = app.repository
    private val prefs = app.prefs

    val telemetryState = repo.telemetryState

    private val _uiState = MutableStateFlow(
        MainUiState(
            currentScreen = if (!prefs.authToken.isNullOrBlank() && !prefs.deviceId.isNullOrBlank()) {
                AppScreen.DASHBOARD
            } else if (!prefs.authToken.isNullOrBlank()) {
                AppScreen.LINK_DEVICE
            } else {
                AppScreen.LOGIN
            },
            userEmail = prefs.userEmail ?: "",
            deviceName = prefs.deviceName,
            deviceId = prefs.deviceId ?: "",
            deviceUuid = prefs.deviceUuid,
            apiBaseUrl = prefs.apiBaseUrl,
            isLocationSharingEnabled = prefs.isLocationSharingEnabled,
            isBackgroundServiceEnabled = prefs.isBackgroundServiceEnabled,
            heartbeatInterval = prefs.heartbeatIntervalSeconds
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshState()
        // If logged in, start service and perform initial telemetry sync
        if (_uiState.value.currentScreen == AppScreen.DASHBOARD) {
            startMonitoringServiceIfNeeded()
            syncNow()
        }
    }

    fun refreshState() {
        repo.updateTelemetrySnapshot()
        _uiState.update {
            it.copy(
                userEmail = prefs.userEmail ?: "",
                deviceName = prefs.deviceName,
                deviceId = prefs.deviceId ?: "",
                deviceUuid = prefs.deviceUuid,
                apiBaseUrl = prefs.apiBaseUrl,
                isLocationSharingEnabled = prefs.isLocationSharingEnabled,
                isBackgroundServiceEnabled = prefs.isBackgroundServiceEnabled,
                heartbeatInterval = prefs.heartbeatIntervalSeconds
            )
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        repo.updateTelemetrySnapshot()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun login(email: String, pass: String, customBaseUrl: String?) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ Email và Mật khẩu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            if (!customBaseUrl.isNullOrBlank()) {
                prefs.apiBaseUrl = customBaseUrl.trim()
                _uiState.update { it.copy(apiBaseUrl = prefs.apiBaseUrl) }
            }

            when (val res = repo.login(email, pass)) {
                is ApiResult.Success -> {
                    val deviceId = prefs.deviceId
                    if (!deviceId.isNullOrBlank()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentScreen = AppScreen.DASHBOARD,
                                userEmail = email.trim(),
                                deviceId = deviceId,
                                successMessage = "Đăng nhập thành công!"
                            )
                        }
                        startMonitoringServiceIfNeeded()
                        syncNow()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentScreen = AppScreen.LINK_DEVICE,
                                userEmail = email.trim(),
                                successMessage = "Đăng nhập thành công! Vui lòng liên kết thiết bị."
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun register(name: String, email: String, pass: String, passConfirm: String, customBaseUrl: String?) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ Họ tên, Email và Mật khẩu.") }
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Địa chỉ email không đúng định dạng.") }
            return
        }
        if (pass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu phải có tối thiểu 6 ký tự.") }
            return
        }
        if (pass != passConfirm) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (!customBaseUrl.isNullOrBlank()) {
                prefs.apiBaseUrl = customBaseUrl.trim()
                _uiState.update { it.copy(apiBaseUrl = prefs.apiBaseUrl) }
            }

            when (val res = repo.register(name, email, pass)) {
                is ApiResult.Success -> {
                    val deviceId = prefs.deviceId
                    if (!prefs.authToken.isNullOrBlank()) {
                        if (!deviceId.isNullOrBlank()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    currentScreen = AppScreen.DASHBOARD,
                                    userEmail = email.trim(),
                                    deviceId = deviceId,
                                    successMessage = "Đăng ký và kết nối tài khoản thành công!"
                                )
                            }
                            startMonitoringServiceIfNeeded()
                            syncNow()
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    currentScreen = AppScreen.LINK_DEVICE,
                                    userEmail = email.trim(),
                                    successMessage = "Đăng ký thành công! Hãy liên kết thiết bị này."
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = res.data
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun registerDevice(name: String) {
        val trimmed = name.trim().ifEmpty { prefs.deviceName }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = repo.registerDeviceExplicit(trimmed)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            deviceName = trimmed,
                            deviceId = res.data,
                            currentScreen = AppScreen.DASHBOARD,
                            successMessage = "Thiết bị đã được liên kết thành công!"
                        )
                    }
                    startMonitoringServiceIfNeeded()
                    syncNow()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                    }
                }
            }
        }
    }

    fun setLocationSharing(enabled: Boolean) {
        repo.setLocationSharing(enabled)
        _uiState.update { it.copy(isLocationSharingEnabled = enabled) }
        viewModelScope.launch {
            // Send updated heartbeat immediately to notify backend of status
            repo.sendHeartbeat("ONLINE")
            if (enabled) {
                repo.sendLocationTelemetry()
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingNow = true, errorMessage = null) }
            val ok = repo.syncAllNow()
            _uiState.update {
                it.copy(
                    isSyncingNow = false,
                    successMessage = if (ok) "Đã đồng bộ thông tin mới nhất lên Web" else null,
                    errorMessage = if (!ok) "Đồng bộ thất bại. Kiểm tra kết nối mạng." else null
                )
            }
        }
    }

    fun updateApiUrl(url: String) {
        if (url.isNotBlank()) {
            prefs.apiBaseUrl = url.trim()
            _uiState.update { it.copy(apiBaseUrl = prefs.apiBaseUrl, successMessage = "Đã lưu địa chỉ API máy chủ mới") }
        }
    }

    fun updateHeartbeatInterval(seconds: Int) {
        prefs.heartbeatIntervalSeconds = seconds
        _uiState.update { it.copy(heartbeatInterval = seconds) }
    }

    fun setBackgroundService(enabled: Boolean) {
        prefs.isBackgroundServiceEnabled = enabled
        _uiState.update { it.copy(isBackgroundServiceEnabled = enabled) }
        if (enabled) {
            DeviceMonitorService.startService(getApplication())
        } else {
            DeviceMonitorService.stopService(getApplication())
        }
    }

    private fun startMonitoringServiceIfNeeded() {
        if (prefs.isBackgroundServiceEnabled && !prefs.authToken.isNullOrBlank() && !prefs.deviceId.isNullOrBlank()) {
            DeviceMonitorService.startService(getApplication())
        }
    }

    fun logout() {
        DeviceMonitorService.stopService(getApplication())
        repo.logout()
        _uiState.update {
            MainUiState(
                currentScreen = AppScreen.LOGIN,
                apiBaseUrl = prefs.apiBaseUrl,
                deviceUuid = prefs.deviceUuid,
                deviceName = prefs.deviceName
            )
        }
    }
}
