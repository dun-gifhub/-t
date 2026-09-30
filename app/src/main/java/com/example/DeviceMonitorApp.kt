package com.example

import android.app.Application
import com.example.data.local.PreferenceManager
import com.example.data.repository.DeviceRepository

class DeviceMonitorApp : Application() {

    lateinit var prefs: PreferenceManager
        private set

    lateinit var repository: DeviceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(this)
        repository = DeviceRepository(this, prefs)
    }
}
