package com.example.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.model.BatteryInfo

object BatteryHelper {

    fun getBatteryInfo(context: Context): BatteryInfo {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                val batteryPct = if (level >= 0 && scale > 0) {
                    ((level / scale.toFloat()) * 100).toInt()
                } else null

                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                BatteryInfo(level = batteryPct, isCharging = isCharging)
            } else {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val isCharging = bm?.isCharging
                BatteryInfo(level = if (level != null && level in 0..100) level else null, isCharging = isCharging)
            }
        } catch (e: Exception) {
            BatteryInfo(level = null, isCharging = null)
        }
    }
}
