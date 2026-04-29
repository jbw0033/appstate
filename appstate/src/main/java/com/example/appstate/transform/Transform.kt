package com.example.appstate.transform

import androidx.compose.runtime.Composable
import com.example.appstate.AppState
import com.example.appstate.AppStateChangedListener
import com.example.transform.transform
import androidx.compose.runtime.State

fun <R> AppState.transformOnce(
    onAppStateChangedListener: AppStateChangedListener,
    defaultValue: R,
    onUpdate: @Composable () -> R
) : State<R> {
    addAppStateListener {
        onAppStateChangedListener.onAppStateChanged(it)
    }
    return transform(defaultValue = defaultValue) {
        onUpdate()
    }
}