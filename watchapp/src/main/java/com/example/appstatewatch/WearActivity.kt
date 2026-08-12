/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up-to-date changes to the libraries and their usages.
 */

package com.example.appstatewatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.appstate.AppState
import com.example.appstatewatch.presentation.theme.AppStateWatchTheme
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

class WearActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val appState = (application as MyApplication).appState
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val key = loadInitialState()
            if (key != null && key != "end") {
                appState.startUserFlow("main", key)
            }
        }

        setContent {
            WearApp(
                "Android",
                appState
            ) { sendKey(appState) }
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    private fun sendKey(appState: AppState) {
        lifecycleScope.launch {
            try {
                val currentState = appState.userFlow("main").lastOrNull() as? String ?: "end"
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
            Log.d(TAG, "DataItem saved: $event")
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                val appState = (application as MyApplication).appState
                val key = event.dataItem.data?.decodeToString()
                    ?.substringAfter("appstate")
                    ?.substringBefore("time")
                    ?.filter { it.isLetterOrDigit() }

                if (key == "end") {
                    finish()
                }

                if (key != null && key != "end") {
                    appState.startUserFlow("main", key)
                }
                Log.d(TAG, "DataItem saved: $key")
            } else if (event.type == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private suspend fun loadInitialState(): String? {
        try {
            val items = dataClient.dataItems.await()

            val dataItem = items.first()

            Log.d(TAG, "DataItem saved: $dataItem in loadInitialState")
            return dataItem.data?.decodeToString()
                ?.substringAfter("appstate")
                ?.substringBefore("time")
                ?.filter { it.isLetterOrDigit() }


        } catch (e: Exception) {
            Log.d(TAG, "dataitem failed", e)
        }
        return null
    }
}

@Composable
fun WearApp(
    greetingName: String,
    appState: AppState,
    sendKey: () -> Unit = { }) {
    AppStateWatchTheme {
        AppScaffold {
            val backStack by transform(defaultValue = listOf("home")) {
                val userFlow = appState.userFlow("main")
                if (userFlow.firstOrNull() != "home") {
                    listOf("home") + userFlow
                } else {
                    userFlow
                }
            }

            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("More")
                    }
                },
            ) { contentPadding -> // ScreenScaffold provides default padding; adjust as needed
                NavDisplay(
                    backStack,
                    modifier = Modifier.padding(contentPadding),
                    onBack = {
                        appState.popUserFlow("main")
                        sendKey()
                    },
                    entryProvider = entryProvider {
                        entry("home") {
                            TransformingLazyColumn(state = listState) {
                                item {
                                    ListHeader(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .transformedHeight(this, transformationSpec),
                                        transformation = SurfaceTransformation(transformationSpec),
                                    ) {
                                        Text(text = stringResource(R.string.hello_world, greetingName))
                                    }
                                }
                                item {
                                    Button(
                                        onClick = {
                                            appState.startUserFlow("main", "A")
                                            sendKey()
                                                  },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .transformedHeight(this, transformationSpec),
                                        transformation = SurfaceTransformation(transformationSpec),
                                    ) {
                                        Text("Button A")
                                    }
                                }
                                item {
                                    Button(
                                        onClick = { appState.startUserFlow("main", "B")
                                            sendKey()},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .transformedHeight(this, transformationSpec),
                                        transformation = SurfaceTransformation(transformationSpec),
                                    ) {
                                        Text("Button B")
                                    }
                                }
                                item {
                                    Button(
                                        onClick = { appState.startUserFlow("main", "C")
                                            sendKey()},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .transformedHeight(this, transformationSpec),
                                        transformation = SurfaceTransformation(transformationSpec),
                                    ) {
                                        Text("Button C")
                                    }
                                }
                            }
                        }
                        entry("A") {
                            Column {
                                Text("We are now on A", modifier = Modifier.padding(contentPadding))
                                Button(
                                    onClick = { appState.startUserFlow("main", "B")
                                        sendKey()},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button B")
                                }
                                Button(
                                    onClick = { appState.startUserFlow("main", "C")
                                        sendKey()},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button C")
                                }
                            }
                        }
                        entry("B") {
                            Column {
                                Text("This is B", modifier = Modifier.padding(contentPadding))
                                Button(
                                    onClick = {
                                        appState.startUserFlow("main", "A")
                                        sendKey()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button A")
                                }
                                Button(
                                    onClick = { appState.startUserFlow("main", "C")
                                        sendKey()},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button C")
                                }
                            }
                        }
                        entry("C") {
                            Column {
                                Text("This is C", modifier = Modifier.padding(contentPadding))
                                Button(
                                    onClick = {
                                        appState.startUserFlow("main", "A")
                                        sendKey()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button A")
                                }
                                Button(
                                    onClick = { appState.startUserFlow("main", "B")
                                        sendKey()},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text("Button B")
                                }
                            }
                        }
                    })
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp("Preview Android", AppState())
}