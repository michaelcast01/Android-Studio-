package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner

@Composable
fun SettingsScreen(
    navController: NavController,
    settingViewModel: SettingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val settingDetail by settingViewModel.settingDetail.observeAsState()
    val error by settingViewModel.error.observeAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var showPaymentMethods by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.setting_id) {
        currentUser?.setting_id?.let { settingId ->
            settingViewModel.fetchSettingDetails(settingId)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            showNetworkError = true
            networkErrorMessage = it
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Configuraciones",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.padding(16.dp))
            
            Button(
                onClick = { showPaymentMethods = !showPaymentMethods },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Ver Métodos de Pago")
            }
            
            if (showPaymentMethods) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Métodos de Pago Disponibles",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        settingDetail?.payments?.forEach { payment ->
                            Text("• ${payment.name}")
                        } ?: Text("No hay métodos de pago configurados")
                    }
                }
            }
            
            Spacer(modifier = Modifier.padding(16.dp))
            
            settingDetail?.let { detail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("Nombre: ${detail.name}")
                        Text("Apodo: ${detail.nickname}")
                        Text("Teléfono: ${detail.phone}")
                        Text("Ciudad: ${detail.city}")
                        Text("Dirección: ${detail.address}")
                    }
                }
            } ?: run {
                Text("No se encontraron configuraciones")
            }
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    currentUser?.setting_id?.let { settingId ->
                        settingViewModel.fetchSettingDetails(settingId)
                    }
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }
}

