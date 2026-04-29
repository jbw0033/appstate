package com.example.weatherappstate

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Composable
fun CityWeatherScreen(city: City) {
    Column {
        Text(city.name)
        Text(city.temperature.toString())
    }
}

@Serializable
data class City(val name: String, val temperature: Int)