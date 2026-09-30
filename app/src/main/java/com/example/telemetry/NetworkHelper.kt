package com.example.telemetry

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkHelper {

    fun getNetworkType(context: Context): String {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return "UNKNOWN"

            val activeNetwork = connectivityManager.activeNetwork ?: return "NONE"
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "NONE"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "WIFI"
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> "MOBILE"
                else -> "UNKNOWN"
            }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }

    fun isConnected(context: Context): Boolean {
        val type = getNetworkType(context)
        return type == "WIFI" || type == "MOBILE"
    }
}
