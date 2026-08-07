package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SmsPermissionScreen
import com.example.ui.theme.InventoryTheme
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            InventoryTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearSnackbar()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            is Screen.Login -> {
                                LoginScreen(viewModel = viewModel)
                            }
                            is Screen.Inventory -> {
                                InventoryScreen(
                                    viewModel = viewModel,
                                    onNavigateToSmsSettings = {
                                        viewModel.navigateTo(Screen.SmsPermission)
                                    }
                                )
                            }
                            is Screen.SmsPermission -> {
                                SmsPermissionScreen(
                                    viewModel = viewModel,
                                    onBackToInventory = {
                                        viewModel.navigateTo(Screen.Inventory)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
