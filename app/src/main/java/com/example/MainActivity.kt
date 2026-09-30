package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.DeviceLinkScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DeviceMonitorAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }
}

@Composable
fun DeviceMonitorAppContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetryState.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        AppScreen.LOGIN -> {
            LoginScreen(
                state = uiState,
                onLogin = { email, pass, apiUrl ->
                    viewModel.login(email, pass, apiUrl)
                },
                onRegister = { name, email, pass, passConfirm, apiUrl ->
                    viewModel.register(name, email, pass, passConfirm, apiUrl)
                }
            )
        }
        AppScreen.LINK_DEVICE -> {
            BackHandler {
                viewModel.logout()
            }
            DeviceLinkScreen(
                state = uiState,
                onLinkDevice = { name ->
                    viewModel.registerDevice(name)
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        }
        AppScreen.DASHBOARD -> {
            if (uiState.selectedTab != 0) {
                BackHandler {
                    viewModel.setTab(0)
                }
            }
            MainScreen(
                uiState = uiState,
                telemetry = telemetry,
                onTabSelected = { viewModel.setTab(it) },
                onToggleLocationSharing = { viewModel.setLocationSharing(it) },
                onSyncNow = { viewModel.syncNow() },
                onUpdateApiUrl = { viewModel.updateApiUrl(it) },
                onUpdateHeartbeatInterval = { viewModel.updateHeartbeatInterval(it) },
                onToggleBackgroundService = { viewModel.setBackgroundService(it) },
                onLogout = { viewModel.logout() },
                onClearMessages = { viewModel.clearMessages() }
            )
        }
    }
}
