package com.example.appstatewatch

import android.app.Application
import androidx.appstate.AppState

class MyApplication : Application() {
    val appState = AppState()
}