package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.TelemetryState
import com.example.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    telemetry: TelemetryState,
    onTabSelected: (Int) -> Unit,
    onToggleLocationSharing: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onUpdateApiUrl: (String) -> Unit,
    onUpdateHeartbeatInterval: (Int) -> Unit,
    onToggleBackgroundService: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onClearMessages: () -> Unit,
    onUpdateStudentProfile: (
        studentName: String,
        studentId: String,
        schoolName: String,
        grade: String,
        className: String,
        parentPhone: String,
        deviceName: String
    ) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.selectedTab) {
                            0 -> "Device Monitor"
                            1 -> "Vị trí của tôi"
                            else -> "Cài đặt & Cấu hình"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (uiState.selectedTab == 0) {
                        IconButton(
                            onClick = onSyncNow,
                            enabled = !uiState.isSyncingNow,
                            modifier = Modifier.testTag("appbar_sync_button")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Đồng bộ",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Trang chủ"
                        )
                    },
                    label = { Text("Trang chủ") },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == 1) Icons.Default.LocationOn else Icons.Outlined.LocationOn,
                            contentDescription = "Vị trí"
                        )
                    },
                    label = { Text("Vị trí") },
                    modifier = Modifier.testTag("nav_tab_location")
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == 2) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Cài đặt"
                        )
                    },
                    label = { Text("Cài đặt") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    telemetry = telemetry,
                    onToggleLocationSharing = onToggleLocationSharing,
                    onSyncNow = onSyncNow,
                    onNavigateLocation = { onTabSelected(1) },
                    onUpdateStudentProfile = onUpdateStudentProfile
                )
                1 -> LocationScreen(
                    telemetry = telemetry,
                    onToggleLocationSharing = onToggleLocationSharing,
                    onForceLocationRefresh = onSyncNow
                )
                2 -> SettingsScreen(
                    uiState = uiState,
                    onUpdateApiUrl = onUpdateApiUrl,
                    onUpdateHeartbeatInterval = onUpdateHeartbeatInterval,
                    onToggleBackgroundService = onToggleBackgroundService,
                    onToggleLocationSharing = onToggleLocationSharing,
                    onLogout = onLogout
                )
            }
        }
    }
}
