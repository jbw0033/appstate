package com.example.weatherappstate

import android.app.Application
import android.content.Context
import androidx.appstate.AppState
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.dataStoreFile
import com.example.appstate.datastore.AppStatePreferences
import com.example.appstate.datastore.AppStateSerializer
import com.example.appstate.datastore.addAppStateToDataStoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath

private lateinit var dataStoreInstance: DataStore<AppStatePreferences>

val Context.dataStore: DataStore<AppStatePreferences>
    get() {
        synchronized(this) {
            if (!::dataStoreInstance.isInitialized) {
                dataStoreInstance = DataStoreFactory.create(
                    storage = OkioStorage(FileSystem.SYSTEM, AppStateSerializer) {
                        dataStoreFile("settings").absolutePath.toPath()
                    }
                )
            }
            return dataStoreInstance
        }
    }

class MyApplication : Application() {
    val appState = AppState()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Load the initial selected city and city list from DataStore
        scope.launch {
            // Start the listener in a child coroutine so it doesn't block the outer scope
            launch {
                try {
                    appState.addAppStateToDataStoreListener(dataStore)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Start the WeatherService immediately in parallel
            val serviceIntent = android.content.Intent(this@MyApplication, WeatherService::class.java)
            startService(serviceIntent)
        }
    }
}
