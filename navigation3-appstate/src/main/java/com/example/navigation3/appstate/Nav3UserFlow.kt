package com.example.navigation3.appstate

import androidx.appstate.statestore.StateStore
import androidx.appstate.statestore.StateStoreKey
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

fun StateStore.startUserFlow(key: String, userFlow: Any) {
    val nav3UserFlow = getState(Nav3StateStoreKey(key), Nav3UserFlow())
    nav3UserFlow.value.backStack += userFlow
}

fun StateStore.popUserFlow(key: String) {
    val userFlow = getState(Nav3StateStoreKey(key), Nav3UserFlow())
    userFlow.value.backStack.removeLastOrNull()
}

fun StateStore.userFlow(key: String): List<Any> {
    val userFlow = getState(Nav3StateStoreKey(key), Nav3UserFlow())
    return userFlow.value.backStack
}

data class Nav3StateStoreKey(val backStackName: String) : StateStoreKey<Nav3UserFlow>(Nav3UserFlow())

data class Nav3UserFlow(val backStack: SnapshotStateList<Any> = mutableStateListOf())