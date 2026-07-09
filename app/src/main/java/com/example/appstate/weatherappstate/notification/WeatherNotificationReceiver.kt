package com.example.appstate.weatherappstate.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appstate.weatherappstate.MyApplication
import com.example.appstate.weatherappstate.cityList
import com.example.appstate.weatherappstate.selectedCity
import com.example.appstate.weatherappstate.setSelectedCity

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
