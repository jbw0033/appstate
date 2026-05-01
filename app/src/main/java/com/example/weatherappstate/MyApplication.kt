package com.example.weatherappstate

import android.app.Application
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MyApplication : Application() {
    val appState = AppState()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val prefKey = stringPreferencesKey("selected_city")
        val citiesKey = stringPreferencesKey("city_list")

        // Load the initial selected city and city list from DataStore
        scope.launch {
            try {
                val preferences = dataStore.data.first()
                
                // Load selected city
                val cityJson = preferences[prefKey]
                if (cityJson != null) {
                    val city = Json.decodeFromString<City>(cityJson)
                    appState.setSelectedCity(city)
                }
                
                // Load city list
                val cityListJson = preferences[citiesKey]
                if (cityListJson != null) {
                    val cities = Json.decodeFromString<List<City>>(cityListJson)
                    appState.setState(CitiesAppStateKey("US"), cities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Start the WeatherService after loading DataStore
                val serviceIntent = android.content.Intent(this@MyApplication, WeatherService::class.java)
                startService(serviceIntent)
            }
        }

        // Save states to DataStore whenever they change
        appState.addAppStateListener {
            val city by appState.selectedCity()
            val cities by appState.cityList("US")
            LaunchedEffect(city, cities) {
                try {
                    val cityJson = Json.encodeToString(city)
                    val citiesJson = Json.encodeToString(cities)
                    withContext(Dispatchers.IO) {
                        dataStore.edit { settings ->
                            settings[prefKey] = cityJson
                            settings[citiesKey] = citiesJson
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
