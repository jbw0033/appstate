package com.example.appstate.content

import android.app.Application
import android.content.Intent
import androidx.appstate.datastore.syncToDataStore
import androidx.appstate.statestore.StateStore
import androidx.datastore.dataStoreFile
import com.example.appstate.weatherappstate.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    val stateStore = StateStore()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Load the initial selected city and city list from DataStore
        scope.launch {
            // Start the listener in a child coroutine so it doesn't block the outer scope
            launch {
                try {
                    stateStore.syncToDataStore(dataStoreFile("settings").absolutePath, this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Start the WeatherService immediately in parallel
            val serviceIntent = Intent(this@MyApplication, WeatherService::class.java)
            startService(serviceIntent)
        }
    }
}