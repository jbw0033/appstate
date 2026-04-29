package com.example.weatherappstate

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WeatherService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null // We are a started service, not a bound service
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appState = (application as MyApplication).appState
        val country = intent?.getStringExtra("country") ?: "US"

        serviceScope.launch {
            // Simulate a background delay (like network fetch)
            delay(1000)
            
            val cities = listOf(
                City("Boston", 60),
                City("New York", 50),
                City("Los Angeles", 72),
                City("Chicago", 45),
                City("Miami", 85),
                City("Seattle", 55)
            )
            
            launch(Dispatchers.Main) {
                appState.setState(CitiesAppStateKey(country), cities)
            }
        }

        return START_STICKY
    }
}
