package com.example.weatherappstate

import android.app.Application
import com.example.appstate.AppState

class MyApplication : Application() {
    val appState = AppState()
}