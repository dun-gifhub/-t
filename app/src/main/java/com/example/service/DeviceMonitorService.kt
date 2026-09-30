package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.DeviceMonitorApp
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DeviceMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var syncJob: Job? = null

    companion object {
        const val CHANNEL_ID = "device_monitor_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.START"
        const val ACTION_STOP = "com.example.service.STOP"
        const val ACTION_SYNC_NOW = "com.example.service.SYNC_NOW"

        fun startService(context: Context) {
            val intent = Intent(context, DeviceMonitorService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DeviceMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSync()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SYNC_NOW -> {
                serviceScope.launch {
                    val app = application as? DeviceMonitorApp
                    app?.repository?.syncAllNow()
                    updateNotification()
                }
            }
            else -> {
                startForegroundWithNotification()
                startSyncLoop()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Device Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi và đồng bộ trạng thái thiết bị theo thời gian thực"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val app = application as? DeviceMonitorApp
        val devName = app?.prefs?.deviceName ?: "Thiết bị"
        val isLocSharing = app?.prefs?.isLocationSharingEnabled == true
        val locText = if (isLocSharing) "GPS: Bật" else "GPS: Tắt"

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Device Monitor • $devName")
            .setContentText("Trạng thái: Hoạt động • $locText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val app = application as? DeviceMonitorApp
            if (app?.prefs?.isLocationSharingEnabled == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                serviceType
            )
        } catch (e: Exception) {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun startSyncLoop() {
        syncJob?.cancel()
        syncJob = serviceScope.launch {
            val app = application as? DeviceMonitorApp ?: return@launch
            while (isActive) {
                // Check if user is logged in and device is registered
                if (!app.prefs.authToken.isNullOrBlank() && !app.prefs.deviceId.isNullOrBlank()) {
                    try {
                        app.repository.syncAllNow()
                        updateNotification()
                    } catch (e: Exception) {
                        // ignore and back off
                    }
                }

                val intervalSeconds = app.prefs.heartbeatIntervalSeconds.coerceAtLeast(15)
                val delayTime = (intervalSeconds * 1000L) + app.repository.getBackoffDelayMillis()
                delay(delayTime)
            }
        }
    }

    private fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSync()
    }
}
