package com.example.appstate

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.collections.getOrPut

class AppState {

    private val stateStore: MutableMap<AppStateKey<*>, MutableState<*>> = mutableMapOf()

    private val appStateListeners = mutableListOf<AppStateChangedListener>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getState(key: AppStateKey<T>, defaultValue: T): MutableState<T> {
        return stateStore.getOrPut(key) { mutableStateOf(defaultValue) } as MutableState<T>
    }

    fun <T> updateState(key: AppStateKey<T>, defaultValue: T, update: (T) -> T) {
        val currentState = getState(key, defaultValue)
        setState(key, update(currentState.value))
    }

    fun <T> setState(stateKey: AppStateKey<T>, value: T) {
        if (!stateStore.contains(stateKey) && stateKey.autoClearKey != null) {
            val listener = object : AppStateChangedListener {
                override fun onAppStateChanged(key: Any) {
                    if (key == stateKey.autoClearKey) {
                        if(stateKey.predictate(this@AppState)) {
                            stateStore.remove(key)
                            removeAppStateListener(this)
                        }
                    }
                }
            }
            addAppStateListener(listener)
        }
        getState(stateKey, value).value = value

        appStateListeners.forEach { it.onAppStateChanged(stateKey) }
    }

    fun addAppStateListener(listener: AppStateChangedListener) {
        appStateListeners.add(listener)
    }

    fun removeAppStateListener(listener: AppStateChangedListener) {
        appStateListeners.remove(listener)
    }
}

fun interface AppStateChangedListener {
    fun onAppStateChanged(key: Any)
}

@Serializable
open class AppStateKey<T> {
    @Transient val autoClearKey: Any? = null
    @Transient val predictate: (AppState) -> Boolean = { true }
}
