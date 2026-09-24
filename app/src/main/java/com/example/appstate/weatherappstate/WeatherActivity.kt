package com.example.appstate.weatherappstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appstate.content.MyApplication
import com.example.appstate.theme.AppStateTheme

class WeatherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val stateStore = (application as MyApplication).stateStore
        enableEdgeToEdge()
        setContent {
            AppStateTheme {
                WeatherApp(stateStore)
            }
        }
    }
}


