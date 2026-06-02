package com.example.weatherappstate

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appState = (context.applicationContext as MyApplication).appState

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val city by appState.selectedCity()
                if (city == null) {
                    Text("No city selected")
                } else {
                    Text(
                        text = city!!.name,
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    val weatherEmoji = when {
                        city!!.temperature >= 70 -> "☀️"
                        city!!.temperature >= 50 -> "⛅"
                        else -> "❄️"
                    }
                    Text(
                        text = weatherEmoji,
                        style = TextStyle(fontSize = 48.sp)
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "${city!!.temperature}\u2109",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    )
                }
            }
        }
    }
}
