package com.example.appstatewatch

import android.app.Application
import androidx.appstate.statestore.StateStore

class MyApplication : Application() {
    val stateStore = StateStore()
}