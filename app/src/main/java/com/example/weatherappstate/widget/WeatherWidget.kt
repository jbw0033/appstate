package com.example.weatherappstate.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
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
import com.example.weatherappstate.selectedCity
import kotlinx.serialization.Serializable

class WeatherWidget : AppStateGlanceWidget<WidgetData>() {

    @Composable
    override fun provideData(
        context: Context,
        id: GlanceId
    ): WidgetData {
        val appState = (context.applicationContext as MyApplication).appState
        val cityState = appState.selectedCity()
        val city = cityState.value
        return if (city == null) WidgetData("No city selected", 0)
        else WidgetData(city.name, city.temperature)
    }

    override suspend fun provideGlance(context: Context, data: MutableState<WidgetData>) {
        val widgetData by data
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
                Text(
                    text = widgetData.name,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = widgetData.weatherEmoji,
                    style = TextStyle(fontSize = 48.sp)
                )

                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "${widgetData.temperature}\u2109",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp)
                )
            }
        }
    }
}

@Serializable
data class WidgetData(val name: String, val temperature: Int) {
    val weatherEmoji = when {
        temperature >= 70 -> "☀️"
        temperature >= 50 -> "⛅"
        else -> "❄️"
    }
}
