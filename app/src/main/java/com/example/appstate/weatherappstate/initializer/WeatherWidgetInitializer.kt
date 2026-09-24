package com.example.appstate.weatherappstate.initializer

import android.content.Context
import androidx.startup.Initializer
import com.example.appstate.weatherappstate.widget.WeatherWidget

class WeatherWidgetInitializer : Initializer<WeatherWidget> {
    override fun create(context: Context): WeatherWidget {
        WeatherWidget.startTransform(context.applicationContext)
        return WeatherWidget
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
