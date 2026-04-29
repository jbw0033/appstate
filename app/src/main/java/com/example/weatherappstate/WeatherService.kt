package com.example.weatherappstate

import com.example.appstate.AppState

class WeatherService(private val appState: AppState) {
    fun fetchCities(country: String = "US") {
        val cities = listOf(
            City("Boston", 60),
            City("New York", 50),
            City("Los Angeles", 72),
            City("Chicago", 45),
            City("Miami", 85),
            City("Seattle", 55)
        )
        appState.setState(CitiesAppStateKey(country), cities)
    }
}
