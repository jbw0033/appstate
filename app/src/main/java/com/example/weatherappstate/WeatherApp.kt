package com.example.weatherappstate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.appstate.AppState
import com.example.navigation3.appstate.popUserFlow
import com.example.navigation3.appstate.startUserFlow
import com.example.navigation3.appstate.userFlow
import com.example.transform.transform

@Composable
fun WeatherApp(appState: AppState) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val backStack by transform(defaultValue = listOf(CityList)) {
            val userFlow = appState.userFlow("main")
            if (userFlow.firstOrNull() != CityList) {
                listOf(CityList) + userFlow
            } else {
                userFlow
            }
        }
        NavDisplay(
            backStack,
            modifier = Modifier.padding(innerPadding),
            sceneStrategies = listOf(rememberListDetailSceneStrategy()),
            onBack = {
                appState.popUserFlow("main")
            },
            entryProvider = entryProvider {
                entry<CityList>(metadata = ListDetailScene.listPane()) {
                    CityListScreen(appState) {
                        appState.startUserFlow("main", it)
                    }
                }
                entry<City>(metadata = ListDetailScene.detailPane()) {
                    CityWeatherScreen(it)
                }
            }
        )
    }
}