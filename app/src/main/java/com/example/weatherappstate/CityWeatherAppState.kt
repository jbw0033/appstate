package com.example.weatherappstate

import androidx.appstate.AppState
import androidx.appstate.AppStateKey
import androidx.compose.runtime.State
import com.example.appstate.datastore.PersistToDataStore
import kotlinx.serialization.Serializable

fun AppState.cityList(country: String = "US"): State<List<City>> {
    return getState(CitiesAppStateKey(country), emptyList())
}

fun AppState.isLoading(): State<Boolean> {
    return getState(IsLoadingAppStateKey, false)
}

fun AppState.setIsLoading(loading: Boolean) {
    setState(IsLoadingAppStateKey, loading)
}

fun AppState.addCity(city: City, country: String = "US") {
    val currentList = cityList(country).value
    setState(CitiesAppStateKey(country), currentList + city)
}

fun AppState.removeCity(city: City, country: String = "US") {
    val currentList = cityList(country).value
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
        stateKey = SelectedCityAppStateKey,
        null as City?
    )
}

@Serializable
@PersistToDataStore
data class CitiesAppStateKey(val country: String) : AppStateKey<List<City>>()

@Serializable
@PersistToDataStore
object SelectedCityAppStateKey : AppStateKey<City?>()

object IsLoadingAppStateKey : AppStateKey<Boolean>()
