package com.example.appstate.content

import androidx.appstate.statestore.StateStoreKey

object LoginStateKey : StateStoreKey<Boolean>(false)

object First
data class Second(val id: String)
object Login