package com.example.weatherappstate

import com.example.appstate.AppState
import com.example.appstate.AppStateKey

fun AppState.cityList(country: String = "US"): List<City> {
    return getState(CitiesAppStateKey(country), cities).value
}

fun AppState.setSelectedCity(city: City) {
    return setState(
        stateKey = SelectedCityAppStateKey(),
        value = city
    )
}

fun AppState.selectedCity(): City {
    return getState(
        key = SelectedCityAppStateKey(),
        City("Atlanta", 75)
    ).value
}

data class CitiesAppStateKey(val country: String) : AppStateKey<List<City>>()

class SelectedCityAppStateKey : AppStateKey<City>()

val cities = listOf(
    City("Boston", 60),
    City("NewYork", 50),
    City("Los Angeles", 72)
)