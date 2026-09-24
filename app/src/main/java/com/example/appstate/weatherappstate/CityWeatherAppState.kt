package com.example.appstate.weatherappstate

import androidx.appstate.statestore.StateStore
import androidx.appstate.statestore.StateStoreKey
import androidx.compose.runtime.State
import androidx.appstate.datastore.PersistToDataStore
import kotlinx.serialization.Serializable

fun StateStore.cityList(country: String = "US"): State<List<City>> {
    return getState(CitiesStateStoreKey(country), emptyList())
}

fun StateStore.isLoading(): State<Boolean> {
    return getState(IsLoadingStateStoreKey, false)
}

fun StateStore.setIsLoading(loading: Boolean) {
    setState(IsLoadingStateStoreKey, loading)
}

fun StateStore.addCity(city: City, country: String = "US") {
    val currentList = cityList(country).value
    setState(CitiesStateStoreKey(country), currentList + city)
}

fun StateStore.removeCity(city: City, country: String = "US") {
    val currentList = cityList(country).value
    setState(CitiesStateStoreKey(country), currentList - city)
}

fun StateStore.setSelectedCity(city: City) {
    return setState(
        stateKey = SelectedCityStateStoreKey,
        value = city
    )
}

fun StateStore.selectedCity(): State<City?> {
    return getState(
        stateKey = SelectedCityStateStoreKey,
        null as City?
    )
}

@Serializable
data class CitiesStateStoreKey(val country: String) : StateStoreKey<List<City>>(emptyList()), PersistToDataStore

@Serializable
object SelectedCityStateStoreKey : StateStoreKey<City?>(null), PersistToDataStore

object IsLoadingStateStoreKey : StateStoreKey<Boolean>(false)
