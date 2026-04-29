package com.example.weatherappstate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appstate.AppState
import kotlinx.serialization.Serializable

@Composable
fun CityListScreen(appState: AppState, onSelectedCity: (City) -> Unit) {
    val cities = appState.cityList()
    if (cities.isEmpty()) {
        Text("Weather currently not available")
    }
    else {
        LazyColumn {
            items(cities) { city ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(if (appState.selectedCity().value == city) Color.LightGray else Color.Transparent)
                        .clickable {
                            appState.setSelectedCity(city)
                            onSelectedCity(city)
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(city.name)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(city.temperature.toString() + "\u2109")
                }
            }
        }
    }
}

@Serializable
object CityList
