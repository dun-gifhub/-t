package com.example.data.api

import com.example.data.model.DeviceListResponse
import com.example.data.model.DeviceRegisterRequest
import com.example.data.model.DeviceRegisterResponse
import com.example.data.model.HeartbeatRequest
import com.example.data.model.HeartbeatResponse
import com.example.data.model.LocationUploadRequest
import com.example.data.model.LocationUploadResponse
import com.example.data.model.LoginRequest
import com.example.data.model.LoginResponse
import com.example.data.model.RegisterRequest
import com.example.data.model.RegisterResponse
import com.example.data.model.DeviceTelemetryReportRequest
import com.example.data.model.DeviceTelemetryReportResponse
import com.example.data.model.DeviceUninstallRequest
import com.example.data.model.DeviceUninstallResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceMonitorApiService {

    @POST("api/devices/report")
    suspend fun reportTelemetry(
        @Body body: DeviceTelemetryReportRequest
    ): Response<DeviceTelemetryReportResponse>

    @POST("api/devices/uninstall")
    suspend fun notifyUninstall(
        @Body body: DeviceUninstallRequest
    ): Response<DeviceUninstallResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/signup")
    suspend fun signup(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/register")
    suspend fun registerFallback(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("api/login")
    suspend fun loginFallback(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @GET("api/devices")
    suspend fun getDevices(): Response<DeviceListResponse>

    @POST("api/devices")
    suspend fun registerDevice(
        @Body body: DeviceRegisterRequest
    ): Response<DeviceRegisterResponse>

    @GET("api/devices/{id}")
    suspend fun getDeviceDetail(
        @Path("id") id: String
    ): Response<DeviceRegisterResponse>

    @POST("api/devices/{id}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("id") id: String,
        @Body body: HeartbeatRequest
    ): Response<HeartbeatResponse>

    @POST("api/devices/{id}/location")
    suspend fun sendLocation(
        @Path("id") id: String,
        @Body body: LocationUploadRequest
    ): Response<LocationUploadResponse>
}
