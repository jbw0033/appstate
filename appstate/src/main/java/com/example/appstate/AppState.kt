package com.example.appstate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.example.transform.transform
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.collections.getOrPut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.UUID
import kotlin.coroutines.CoroutineContext


class AppState {

    private val stateStore: MutableMap<AppStateKey<*>, MutableState<*>> = mutableMapOf()

    private val appStateListeners = mutableMapOf<UUID, CoroutineScope>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getState(key: AppStateKey<T>, defaultValue: T): MutableState<T> {
        return stateStore.getOrPut(key) { mutableStateOf(defaultValue) } as MutableState<T>
    }

    fun <T> updateState(key: AppStateKey<T>, defaultValue: T, update: (T) -> T) {
        val currentState = getState(key, defaultValue)
        setState(key, update(currentState.value))
    }

    fun <T> setState(stateKey: AppStateKey<T>, value: T) {
        if (
            !stateStore.contains(stateKey) && stateKey.autoClearKey != null) {
            addAppStateListener { map ->
                val key = stateKey.autoClearKey
                val currentValue = map[key]?.value
                val initialValue = remember { currentValue }
                if (currentValue != initialValue && stateKey.predicate(this@AppState)) {
                    LaunchedEffect(currentValue) {
                        stateStore.remove(stateKey)
                        removeAppStateListener(this@addAppStateListener)
                    }
                }
            }
        }
        getState(stateKey, value).value = value
    }

    fun addAppStateListener(
        context: CoroutineContext = Dispatchers.Default,
        listener: @Composable UUID.(Map<AppStateKey<*>, State<*>>) -> Unit
    ): UUID {
        val scope = CoroutineScope(context + SupervisorJob())
        val uuid = UUID.randomUUID()
        transform(scope = scope, defaultValue = Unit) {
            listener(uuid, stateStore)
        }
        appStateListeners[uuid] = scope
        return uuid
    }

    fun removeAppStateListener(id: UUID) {
        val scope = appStateListeners.remove(id)
        scope?.cancel()
    }
}

@Serializable
open class AppStateKey<T> {
    @Transient val autoClearKey: Any? = null
    @Transient val predicate: (AppState) -> Boolean = { true }
}
