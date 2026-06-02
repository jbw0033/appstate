package com.example.weatherappstate.initializer

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.glance.appwidget.updateAll
import androidx.startup.Initializer
import com.example.weatherappstate.MyApplication
import com.example.weatherappstate.widget.WeatherWidget
import com.example.weatherappstate.widget.WeatherWidgetAppStateKey
import com.example.weatherappstate.widget.WidgetData
import com.example.weatherappstate.selectedCity

class WeatherWidgetInitializer : Initializer<WeatherWidget> {
    override fun create(context: Context): WeatherWidget {
        val application = context.applicationContext as MyApplication
        val appState = application.appState
        val widget = WeatherWidget()

        appState.addAppStateListener {
            val cityState = appState.selectedCity()
            val city = cityState.value ?: return@addAppStateListener
            val data = WidgetData(city.name, city.temperature)
            LaunchedEffect(data) {
                appState.setState(WeatherWidgetAppStateKey, data)
                widget.updateAll(context)
            }
        }
        return widget
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
