package com.example.weatherappstate

import android.app.Application
import android.content.Context
import androidx.appstate.AppState
import androidx.appstate.transform.listener
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.dataStoreFile
import com.example.appstate.datastore.AppStatePreferences
import com.example.appstate.datastore.AppStateSerializer
import com.example.appstate.datastore.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val selectedKey = SelectedCityAppStateKey
        val stateKey = CitiesAppStateKey("US")

        // Load the initial selected city and city list from DataStore
        scope.launch {
            try {
                val state = dataStore.data.first()
                
                // Load selected city
                val city = state[selectedKey]
                if (city != null) {
                    appState.setSelectedCity(city)
                }
                
                // Load city list
                val cities = state[stateKey]
                if (cities != null) {
                    appState.setState(stateKey, cities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Start the WeatherService after loading DataStore
                val serviceIntent = android.content.Intent(this@MyApplication, WeatherService::class.java)
                startService(serviceIntent)
            }
        }

        scope.launch {
            // Save states to DataStore whenever they change
            listener {
                val city by appState.selectedCity()
                val cities by appState.cityList("US")
                LaunchedEffect(city, cities) {
                    try {
                        withContext(Dispatchers.IO) {
                            dataStore.edit { settings ->
                                settings[selectedKey] = city
                                settings[stateKey] = cities
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
