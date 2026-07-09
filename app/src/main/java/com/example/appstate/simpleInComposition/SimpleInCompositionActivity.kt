package com.example.appstate.simpleInComposition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appstate.transform.transform
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.appstate.content.First
import com.example.appstate.content.MyApplication
import com.example.appstate.content.Second
import com.example.appstate.weatherappstate.ui.theme.AppStateTheme
import com.example.navigation3.appstate.popUserFlow
import com.example.navigation3.appstate.startUserFlow
import com.example.navigation3.appstate.userFlow

class SimpleInCompositionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appState = (application as MyApplication).appState
        setContent {
            AppStateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val backStack by transform(defaultValue = listOf(First)) {
                        val userFlow = appState.userFlow("main")
                        if (userFlow.firstOrNull() != First) {
                            listOf(First) + userFlow
                        } else {
                            userFlow
                        }
                    }


                    NavDisplay(
                        backStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = {
                            appState.popUserFlow("main")
                        },
                        entryProvider = entryProvider {
                            entry<First> {
                                Column {
                                    Text("First")
                                    Button(onClick = {
                                        appState.startUserFlow("main", Second("my Id"))
                                    }) {
                                        Text("Go to Second")
                                    }
                                }
                            }
                            entry<Second> {
                                Column {
                                    Text("Second")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}