package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner

@Composable
fun PaymentMethodsEditScreen(
    navController: NavController,
    settingViewModel: SettingViewModel = viewModel()
) {
    val settingDetail by settingViewModel.settingDetail.collectAsState()
    val error by settingViewModel.error.collectAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        settingViewModel.fetchAvailablePaymentMethods()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Text(
                    text = "Editar Métodos de Pago",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(48.dp)) // Para balancear el layout
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selecciona los métodos de pago disponibles",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.padding(16.dp))
                    
                    // Aquí irá la lista de métodos de pago
                    // La implementaremos en la siguiente fase
                }
            }
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    settingViewModel.fetchAvailablePaymentMethods()
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }
}

// helper removed; use settingViewModel.fetchAvailablePaymentMethods() directly from LaunchedEffect
