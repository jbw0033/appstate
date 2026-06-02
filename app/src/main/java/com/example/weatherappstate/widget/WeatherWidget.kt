package com.example.weatherappstate.widget

import android.content.Context
import android.content.Intent
import androidx.appstate.AppStateKey
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
import com.example.weatherappstate.MainActivity
import com.example.weatherappstate.MyApplication
import kotlinx.serialization.Serializable

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
                val data by appState.getState(WeatherWidgetAppStateKey, WidgetData("No city selected", 0))
                Text(
                    text = data.name,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = data.weatherEmoji,
                    style = TextStyle(fontSize = 48.sp)
                )

                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "${data.temperature}\u2109",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp)
                )
            }
        }
    }
}

@Serializable
object WeatherWidgetAppStateKey : AppStateKey<WidgetData>()

@Serializable
data class WidgetData(val name: String, val temperature: Int) {
    val weatherEmoji = when {
        temperature >= 70 -> "☀️"
        temperature >= 50 -> "⛅"
        else -> "❄️"
    }
}
