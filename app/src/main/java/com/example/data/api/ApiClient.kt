package com.example.data.api

import android.util.Log
import com.example.data.local.PreferenceManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "DeviceMonitorApi"
    private var currentBaseUrl: String? = null
    private var cachedService: DeviceMonitorApiService? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Synchronized
    fun getApiService(prefs: PreferenceManager): DeviceMonitorApiService {
        val rawUrl = prefs.apiBaseUrl.trim().ifEmpty { PreferenceManager.DEFAULT_API_BASE_URL }
        val withScheme = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }
        val targetUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        if (cachedService != null && currentBaseUrl == targetUrl) {
            return cachedService!!
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            val token = prefs.authToken
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            
            // Safe logging without exposing passwords or tokens
            val safeUrl = request.url.encodedPath
            Log.d(TAG, "--> ${request.method} $safeUrl")

            val response: Response = chain.proceed(request)
            Log.d(TAG, "<-- ${response.code} $safeUrl")

            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(targetUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(DeviceMonitorApiService::class.java)
        currentBaseUrl = targetUrl
        cachedService = service
        return service
    }

    @Synchronized
    fun invalidate() {
        cachedService = null
        currentBaseUrl = null
    }
}
