package com.example.weatherappstate

import com.example.appstate.AppState
import com.example.appstate.AppStateKey
import androidx.compose.runtime.State

fun AppState.cityList(country: String = "US"): List<City> {
    return getState(CitiesAppStateKey(country), cities).value
}

fun AppState.setSelectedCity(city: City) {
    return setState(
        stateKey = SelectedCityAppStateKey,
        value = city
    )
}

fun AppState.selectedCity(): State<City> {
    return getState(
        key = SelectedCityAppStateKey,
        cityList().first()
    )
}

data class CitiesAppStateKey(val country: String) : AppStateKey<List<City>>()

object SelectedCityAppStateKey : AppStateKey<City>()

val cities = listOf(
    City("Boston", 60),
    City("NewYork", 50),
    City("Los Angeles", 72)
)