package com.example.weatherappstate

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.startup.AppInitializer

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    lateinit var context: Context
    override val glanceAppWidget: GlanceAppWidget by lazy {
        AppInitializer.getInstance(context).initializeComponent(
            WeatherWidgetInitializer::class.java
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        super.onReceive(context, intent)
        // Access context here for custom intent routing
    }
}
