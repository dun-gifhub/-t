package com.example.telemetry

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings

data class ForegroundAppInfo(
    val packageName: String,
    val appName: String,
    val isPermissionGranted: Boolean
)

object AppUsageHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps?.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps?.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun getForegroundApp(context: Context): ForegroundAppInfo {
        val hasPerm = hasUsageStatsPermission(context)
        if (!hasPerm) {
            return ForegroundAppInfo(
                packageName = context.packageName,
                appName = "Device Monitor (Cần cấp quyền Theo dõi ứng dụng)",
                isPermissionGranted = false
            )
        }

        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usm != null) {
                val endTime = System.currentTimeMillis()
                val beginTime = endTime - 60000 // 60 seconds
                val events = usm.queryEvents(beginTime, endTime)
                val event = UsageEvents.Event()

                var lastResumedPackage: String? = null
                var lastTimestamp = 0L

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                        event.eventType == 1 // MOVE_TO_FOREGROUND
                    ) {
                        if (event.timeStamp >= lastTimestamp) {
                            lastTimestamp = event.timeStamp
                            lastResumedPackage = event.packageName
                        }
                    }
                }

                if (!lastResumedPackage.isNullOrBlank()) {
                    val appLabel = getAppNameFromPackage(context, lastResumedPackage)
                    return ForegroundAppInfo(
                        packageName = lastResumedPackage,
                        appName = appLabel,
                        isPermissionGranted = true
                    )
                }
            }
        } catch (_: Exception) {}

        return ForegroundAppInfo(
            packageName = context.packageName,
            appName = "Device Monitor",
            isPermissionGranted = true
        )
    }

    fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            // Friendly fallbacks for common packages
            when {
                packageName.contains("youtube") -> "YouTube"
                packageName.contains("facebook") -> "Facebook"
                packageName.contains("chrome") -> "Google Chrome"
                packageName.contains("tiktok") || packageName.contains("musically") -> "TikTok"
                packageName.contains("zalo") -> "Zalo"
                packageName.contains("messenger") -> "Messenger"
                packageName.contains("browser") -> "Trình duyệt Web"
                packageName.contains("camera") -> "Máy ảnh"
                packageName.contains("gallery") || packageName.contains("photos") -> "Bộ sưu tập"
                packageName.contains("game") -> "Trò chơi"
                packageName.contains("launcher") -> "Màn hình chính"
                packageName.contains("settings") -> "Cài đặt hệ thống"
                else -> packageName.substringAfterLast('.')
            }
        }
    }
}
