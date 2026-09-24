package com.example.appstatewatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.appstate.statestore.StateStore
import com.example.appstatewatch.theme.AppStateWatchTheme
import com.example.navigation3.appstate.popUserFlow
import com.example.navigation3.appstate.startUserFlow
import com.example.navigation3.appstate.userFlow
import androidx.appstate.transform.transform
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap.TAG
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class PhoneActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val stateStore = (application as MyApplication).stateStore
        setContent {
            AppStateWatchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val backStack by transform(initialValue = listOf<Any>("A")) {
                        val userFlow = stateStore.userFlow("main")
                        if (userFlow.firstOrNull() != "A") {
                            listOf("A") + userFlow
                        } else {
                            userFlow
                        }
                    }

                    NavDisplay(
                        backStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = {
                            stateStore.popUserFlow("main")
                            sendKey(stateStore = stateStore)
                        },
                        entryProvider = entryProvider {
                            entry("A") {
                                Column {
                                    Text("First")
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "B")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to B")
                                    }
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "C")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to C")
                                    }
                                }
                            }
                            entry("B") {
                                Column {
                                    Text("Second")
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "A")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to A")
                                    }
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "C")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to C")
                                    }
                                }
                            }
                            entry("C") {
                                Column {
                                    Text("Third")
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "A")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to A")
                                    }
                                    Button(onClick = {
                                        stateStore.startUserFlow("main", "B")
                                        sendKey(stateStore = stateStore)
                                    }) {
                                        Text("Go to B")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    private fun sendKey(stateStore: StateStore) {
        lifecycleScope.launch {
        try {
            val currentState = stateStore.userFlow("main").lastOrNull() as? String ?: "end"
            val request =
                PutDataMapRequest
                    .create("/appstate")
                    .apply {
                        dataMap.putString("appstate", currentState)
                    }.asPutDataRequest()
                    .setUrgent()

            val result = dataClient.putDataItem(request).await()

            Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
            }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                val stateStore = (application as MyApplication).stateStore
                val key = event.dataItem.data?.decodeToString()
                    ?.substringAfter("appstate")
                    ?.substringBefore("time")
                    ?.filter { it.isLetterOrDigit() }

                if (key == "end") {
                    finish()
                }

                if (key != null && key != "end") {
                    stateStore.startUserFlow("main", key)
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}