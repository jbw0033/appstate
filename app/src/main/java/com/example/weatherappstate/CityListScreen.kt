package com.example.weatherappstate

import android.content.Intent
import androidx.appstate.AppState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Composable
fun CityListScreen(appState: AppState, onSelectedCity: (City) -> Unit) {
    val cities = appState.cityList().value
    val isLoading = appState.isLoading().value
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add City")
            }
        }
    ) { padding ->
        if (isLoading && cities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cities.isEmpty()) {
            Text(
                text = "Weather currently not available",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(cities, key = { it.name }) { city ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    
                    if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            appState.removeCity(city)
                        }
                    }
                    
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = false,
                        enableDismissFromStartToEnd = true,
                        backgroundContent = {
                            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Color.Red else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(if (appState.selectedCity().value == city) Color.LightGray else Color.White)
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
    }

    if (showAddDialog) {
        var cityName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add City") },
            text = {
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("City Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (cityName.isNotBlank()) {
                        val intent = Intent(context, WeatherService::class.java).apply {
                            action = "ADD_CITY"
                            putExtra("city_name", cityName.trim())
                        }
                        context.startService(intent)
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Serializable
object CityList
