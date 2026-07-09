package com.example.appstate.loginAppState

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
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.appstate.content.First
import com.example.appstate.content.Login
import com.example.appstate.content.LoginStateKey
import com.example.appstate.content.MyApplication
import com.example.appstate.content.Second
import com.example.appstate.theme.AppStateTheme
import com.example.navigation3.appstate.popUserFlow
import com.example.navigation3.appstate.startUserFlow
import com.example.navigation3.appstate.userFlow

class BasicLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appState = (application as MyApplication).appState
        setContent {
            AppStateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val mainBackStack = rememberDecoratedNavEntries(transform(defaultValue = listOf(First)) {
                        val userFlow = appState.userFlow("main")
                        if (userFlow.firstOrNull() != First) {
                            listOf(First) + userFlow
                        } else {
                            userFlow
                        }
                    }.value, listOf(rememberSaveableStateHolderNavEntryDecorator()), entryProvider =
                        entryProvider {
                            entry<First> {
                                Column {
                                    Text("First")
                                    Button(onClick = {
                                        appState.startUserFlow("main", Second("my Id"))
                                    }) {
                                        Text("Go to Second")
                                    }
                                    Button(onClick = {
                                        appState.updateState(LoginStateKey, false) {
                                            !it
                                        }
                                    }) {
                                        Text("Change login state")
                                    }
                                }
                            }
                            entry<Second> {
                                Column {
                                    Text("Second")
                                    Button(onClick = {
                                        appState.updateState(LoginStateKey, false) {
                                            !it
                                        }
                                    }) {
                                        Text("Change login state")
                                    }
                                }
                            }

                        })

                    val loginBackStack = rememberDecoratedNavEntries(transform(defaultValue = listOf(Login))  {
                        appState.userFlow("login").ifEmpty {
                            listOf(Login)
                        }
                    }.value,
                        listOf(rememberSaveableStateHolderNavEntryDecorator()),
                        entryProvider =
                        entryProvider {
                        entry<Login> {
                            Column {
                                Text("Ready to login")
                                Button(onClick = {
                                    appState.setState(LoginStateKey, true)
                                }) {
                                    Text("Login")
                                }
                            }
                        }
                    })

                    val isLoggedIn = appState.getState(LoginStateKey, false).value

                    val currentBackStack = if (!isLoggedIn) {
                        loginBackStack
                    } else {
                        mainBackStack
                    }

                    NavDisplay(
                        currentBackStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = {
                            if (isLoggedIn) {
                                appState.popUserFlow("main")
                            } else {
                                appState.popUserFlow("login")
                            }
                        }
                    )
                }
            }
        }
    }
}
