package com.example.weatherappstate

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.updateAll
import androidx.startup.Initializer
import com.example.transform.transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WeatherWidgetInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val application = context.applicationContext as MyApplication
        val appState = application.appState
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        transform(scope = scope, defaultValue = Unit) {
            val city by appState.selectedCity()
            val cities by appState.cityList("US")

            LaunchedEffect(city, cities) {
                WeatherWidget().updateAll(context)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
