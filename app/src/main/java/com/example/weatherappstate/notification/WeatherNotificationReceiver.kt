package com.example.weatherappstate.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weatherappstate.MyApplication
import com.example.weatherappstate.cityList
import com.example.weatherappstate.selectedCity
import com.example.weatherappstate.setSelectedCity

class WeatherNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "NEXT_CITY") {
            val appState = (context.applicationContext as MyApplication).appState
            val currentCity = appState.selectedCity().value
            val cities = appState.cityList("US").value

            if (cities.isNotEmpty()) {
                val currentIndex = cities.indexOf(currentCity)
                val nextIndex = (currentIndex + 1) % cities.size
                appState.setSelectedCity(cities[nextIndex])
            }
        }
    }
}
