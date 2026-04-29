package com.example.weatherappstate

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class WeatherResponse(
    @SerialName("current_weather")
    val currentWeather: CurrentWeather? = null
)

@Serializable
data class CurrentWeather(
    val temperature: Double
)

@Serializable
data class GeocodeResponse(
    val results: List<GeocodeResult>? = null
)

@Serializable
data class GeocodeResult(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

class WeatherService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appState = (application as MyApplication).appState
        val country = intent?.getStringExtra("country") ?: "US"
        val action = intent?.action

        serviceScope.launch {
            if (action == "ADD_CITY") {
                val cityName = intent.getStringExtra("city_name") ?: return@launch
                
                try {
                    appState.setIsLoading(true)
                    val json = Json { ignoreUnknownKeys = true }
                    
                    // 1. Geocode the city name to get lat/lon
                    val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${cityName.replace(" ", "+")}&count=1&language=en&format=json"
                    val geoJsonString = URL(geoUrl).readText()
                    val geoResponse = json.decodeFromString<GeocodeResponse>(geoJsonString)
                    val result = geoResponse.results?.firstOrNull()
                    
                    if (result != null) {
                        // 2. Fetch the weather
                        val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=${result.latitude}&longitude=${result.longitude}&current_weather=true&temperature_unit=fahrenheit"
                        val weatherJsonString = URL(weatherUrl).readText()
                        val weatherResponse = json.decodeFromString<WeatherResponse>(weatherJsonString)
                        
                        val temp = weatherResponse.currentWeather?.temperature?.toInt() ?: 0
                        val newCity = City(result.name, temp, result.latitude, result.longitude)
                        
                        launch(Dispatchers.Main) {
                            appState.addCity(newCity, country)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    appState.setIsLoading(false)
                }
            } else {
                try {
                    appState.setIsLoading(true)
                    
                    var currentCities = appState.cityList(country).value
                    if (currentCities.isEmpty()) {
                        currentCities = listOf(
                            City("Boston", 0, 42.3601, -71.0589),
                            City("New York", 0, 40.7128, -74.0060),
                            City("Los Angeles", 0, 34.0522, -118.2437),
                            City("Chicago", 0, 41.8781, -87.6298),
                            City("Miami", 0, 25.7617, -80.1918),
                            City("Seattle", 0, 47.6062, -122.3321)
                        )
                    }
                    
                    if (currentCities.isNotEmpty()) {
                        val latitudes = currentCities.joinToString(",") { it.latitude.toString() }
                        val longitudes = currentCities.joinToString(",") { it.longitude.toString() }
                        
                        val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$latitudes&longitude=$longitudes&current_weather=true&temperature_unit=fahrenheit"
                        val jsonString = URL(urlString).readText()
                        
                        val json = Json { ignoreUnknownKeys = true }
                        
                        // When only 1 location is requested, Open-Meteo returns a single object instead of an array.
                        // We need to handle both cases or always deserialize as JsonElement and check if array.
                        // But since we can use kotlinx.serialization, let's parse it correctly.
                        // Actually, OpenMeteo returns a single object if only 1 lat/lon pair is passed. 
                        // Let's just catch that or force it to be an array? No, open-meteo doesn't have a force array param.
                        val responses = if (currentCities.size == 1) {
                            listOf(json.decodeFromString<WeatherResponse>(jsonString))
                        } else {
                            json.decodeFromString<List<WeatherResponse>>(jsonString)
                        }
                        
                        val updatedCities = currentCities.mapIndexed { index, city ->
                            val temp = responses.getOrNull(index)?.currentWeather?.temperature?.toInt() ?: city.temperature
                            city.copy(temperature = temp)
                        }
                        
                        launch(Dispatchers.Main) {
                            appState.setState(CitiesAppStateKey(country), updatedCities)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    appState.setIsLoading(false)
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We are a started service, not a bound service
    }
}
