package com.example.navigation3.appstate

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.appstate.AppState
import com.example.appstate.AppStateKey

fun AppState.startUserFlow(key: String, userFlow: Any) {
    val nav3UserFlow = getState(Nav3AppStateKey(key), Nav3UserFlow())
    nav3UserFlow.value.backStack += userFlow
}

fun AppState.popUserFlow(key: String) {
    val userFlow = getState(Nav3AppStateKey(key), Nav3UserFlow())
    userFlow.value.backStack.removeLastOrNull()
}

fun AppState.userFlow(key: String): List<Any> {
    val userFlow = getState(Nav3AppStateKey(key), Nav3UserFlow())
    return userFlow.value.backStack
}

data class Nav3AppStateKey(val backStackName: String) : AppStateKey<Nav3UserFlow>()

data class Nav3UserFlow(val backStack: SnapshotStateList<Any> = mutableStateListOf())