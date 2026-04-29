package com.example.weatherappstate

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appstate.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MyApplication : Application() {
    val appState = AppState()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        
        // Start the WeatherService
        val serviceIntent = android.content.Intent(this, WeatherService::class.java)
        startService(serviceIntent)

        val prefKey = stringPreferencesKey("selected_city")

        // Load the initial selected city from DataStore
        scope.launch {
            try {
                val preferences = dataStore.data.first()
                val cityJson = preferences[prefKey]
                if (cityJson != null) {
                    val city = Json.decodeFromString<City>(cityJson)
                    appState.setSelectedCity(city)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Save the selected city to DataStore whenever it changes
        appState.addAppStateListener { key ->
            if (key == SelectedCityAppStateKey) {
                val city = appState.selectedCity().value
                if (city != null) {
                    scope.launch {
                        try {
                            val cityJson = Json.encodeToString(city)
                            dataStore.edit { settings ->
                                settings[prefKey] = cityJson
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
