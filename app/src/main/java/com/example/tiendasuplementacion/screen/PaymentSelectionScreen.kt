package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
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
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentMethods

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionScreen(
    navController: NavController,
    paymentViewModel: PaymentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onPaymentSelected: (Payment) -> Unit
) {
    val payments by paymentViewModel.payments.observeAsState(emptyList())
    val isLoading by paymentViewModel.isLoading.observeAsState(false)
    val error by paymentViewModel.error.observeAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        paymentViewModel.fetchPayments()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFFF6E7DF)
                    )
                }
                Text(
                    text = "Seleccionar Método de Pago",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFF6E7DF)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PSE Option
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onPaymentSelected(
                                        Payment(
                                            name = "PSE",
                                            method = PaymentMethods.PSE
                                        )
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF26272B)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = "PSE",
                                    tint = Color(0xFFF6E7DF),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "PSE",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFF6E7DF)
                                    )
                                    Text(
                                        text = "Pago Seguros en Línea",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Other payment methods
                    items(payments.filter { it.isActive }) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPaymentSelected(payment) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF26272B)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = payment.name,
                                    tint = Color(0xFFF6E7DF),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = payment.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFF6E7DF)
                                    )
                                    Text(
                                        text = when (payment.method) {
                                            PaymentMethods.CREDIT_CARD -> "Tarjeta de Crédito"
                                            PaymentMethods.DEBIT_CARD -> "Tarjeta de Débito"
                                            PaymentMethods.CASH -> "Efectivo"
                                            else -> payment.method
                                        },
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
                    paymentViewModel.fetchPayments()
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }
} 