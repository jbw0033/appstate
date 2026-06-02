package com.example.weatherappstate

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.glance.appwidget.updateAll
import androidx.startup.Initializer

class WeatherWidgetInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val application = context.applicationContext as MyApplication
        val appState = application.appState

        appState.addAppStateListener {
            val cityState = appState.selectedCity()
            val city = cityState.value ?: return@addAppStateListener
            val data = WidgetData(city.name, city.temperature)
            LaunchedEffect(data) {
                appState.setState(WeatherWidgetAppStateKey, data)
                WeatherWidget().updateAll(context)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
