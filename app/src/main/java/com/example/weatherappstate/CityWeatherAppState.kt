package com.example.weatherappstate

import com.example.appstate.AppState
import com.example.appstate.AppStateKey
import androidx.compose.runtime.State

fun AppState.cityList(country: String = "US"): List<City> {
    return getState(CitiesAppStateKey(country), emptyList()).value
}

fun AppState.isLoading(): State<Boolean> {
    return getState(IsLoadingAppStateKey, false)
}

fun AppState.setIsLoading(loading: Boolean) {
    setState(IsLoadingAppStateKey, loading)
}

fun AppState.addCity(city: City, country: String = "US") {
    val currentList = cityList(country)
    setState(CitiesAppStateKey(country), currentList + city)
}

fun AppState.removeCity(city: City, country: String = "US") {
    val currentList = cityList(country)
    setState(CitiesAppStateKey(country), currentList - city)
}

fun AppState.setSelectedCity(city: City) {
    return setState(
        stateKey = SelectedCityAppStateKey,
        value = city
    )
}

fun AppState.selectedCity(): State<City?> {
    return getState(
        key = SelectedCityAppStateKey,
        null as City?
    )
}

data class CitiesAppStateKey(val country: String) : AppStateKey<List<City>>()

object SelectedCityAppStateKey : AppStateKey<City?>()

object IsLoadingAppStateKey : AppStateKey<Boolean>()
