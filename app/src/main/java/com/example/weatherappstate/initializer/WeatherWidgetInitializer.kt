package com.example.weatherappstate.initializer

import android.content.Context
import androidx.startup.Initializer
import com.example.weatherappstate.widget.WeatherWidget

class WeatherWidgetInitializer : Initializer<WeatherWidget> {
    override fun create(context: Context): WeatherWidget {
        val widget = WeatherWidget()
        widget.startTransform(context)
        return widget
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
