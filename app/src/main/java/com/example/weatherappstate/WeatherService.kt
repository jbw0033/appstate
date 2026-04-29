package com.example.weatherappstate

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class WeatherResponse(
    val temperature: Double? = null
)

class WeatherService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appState = (application as MyApplication).appState
        val country = intent?.getStringExtra("country") ?: "US"

        serviceScope.launch {
            try {
                // Fetch real data from Open-Meteo for our 6 default cities
                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=42.3601,40.7128,34.0522,41.8781,25.7617,47.6062&longitude=-71.0589,-74.0060,-118.2437,-87.6298,-80.1918,-122.3321&current_weather=true&temperature_unit=fahrenheit"
                val jsonString = URL(urlString).readText()
                
                val json = Json { ignoreUnknownKeys = true }
                val responses = json.decodeFromString<List<WeatherResponse>>(jsonString)
                
                val cityNames = listOf("Boston", "New York", "Los Angeles", "Chicago", "Miami", "Seattle")
                
                val cities = cityNames.mapIndexed { index, name ->
                    val temp = responses.getOrNull(index)?.temperature?.toInt() ?: 0
                    City(name, temp)
                }
                
                launch(Dispatchers.Main) {
                    appState.setState(CitiesAppStateKey(country), cities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We are a started service, not a bound service
    }
}
