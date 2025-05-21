package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserDetailViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel()
) {
    val userDetailsList by userDetailViewModel.userDetailsList.observeAsState(emptyList())
    val isLoading by userDetailViewModel.isLoading.observeAsState(false)
    val error by userDetailViewModel.error.observeAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Cargar usuarios con role_id = 1 (clientes)
        userDetailViewModel.fetchUserDetailsByRole(1L)
    }

    LaunchedEffect(error) {
        if (error != null && (error!!.contains("No se pudo conectar") || error!!.contains("599"))) {
            showNetworkError = true
            networkErrorMessage = error ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF23242A),
                        Color(0xFF23242A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Gestión de Clientes",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFF6E7DF))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userDetailsList) { userDetail ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF26272B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = userDetail.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFF6E7DF),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userDetail.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                )
                                userDetail.settings?.let { settings ->
                                    Text(
                                        text = "Teléfono: ${settings.phone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Ciudad: ${settings.city}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Dirección: ${settings.address}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    userDetailViewModel.fetchUserDetailsByRole(1L)
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }
} 