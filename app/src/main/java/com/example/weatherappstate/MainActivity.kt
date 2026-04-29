package com.example.weatherappstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weatherappstate.ui.theme.WeatherAppStateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appState = (application as MyApplication).appState
        enableEdgeToEdge()
        setContent {
            WeatherAppStateTheme {
                WeatherApp(appState)
            }
        }
    }
}


