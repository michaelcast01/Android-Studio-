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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalance
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
import com.example.tiendasuplementacion.model.PaymentDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionScreen(
    navController: NavController,
    paymentViewModel: PaymentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onPaymentSelected: (Payment) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val paymentDetails = paymentViewModel.paymentDetails.observeAsState(initial = emptyList()).value
    val isLoading by paymentViewModel.isLoading.observeAsState(initial = false)
    val error by paymentViewModel.error.observeAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId ->
            paymentViewModel.fetchPaymentDetails(userId)
        }
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
            } else if (paymentDetails.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No tienes métodos de pago configurados",
                            color = Color(0xFFF6E7DF),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("payment_config") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF6E7DF),
                                contentColor = Color(0xFF23242A)
                            )
                        ) {
                            Text("Agregar Método de Pago")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(paymentDetails) { paymentDetail ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPaymentSelected(paymentDetail.payment) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF26272B)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (paymentDetail.payment.method) {
                                            PaymentMethods.CREDIT_CARD, PaymentMethods.DEBIT_CARD -> Icons.Default.CreditCard
                                            else -> Icons.Default.AccountBalance
                                        },
                                        contentDescription = paymentDetail.payment.name,
                                        tint = Color(0xFFF6E7DF),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = paymentDetail.payment.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFFF6E7DF)
                                        )
                                        if (paymentDetail.cardNumber != null) {
                                            Text(
                                                text = "•••• " + paymentDetail.cardNumber.takeLast(4),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (paymentDetail.cardholderName != null) {
                                    Text(
                                        text = paymentDetail.cardholderName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                }
                                
                                if (paymentDetail.expirationDate != null) {
                                    Text(
                                        text = "Vence: ${paymentDetail.expirationDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.5f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "${paymentDetail.addressLine1}, ${paymentDetail.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${paymentDetail.stateOrProvince}, ${paymentDetail.country} ${paymentDetail.postalCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { navController.navigate("payment_config") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF6E7DF),
                        contentColor = Color(0xFF23242A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Agregar Nuevo Método de Pago")
                }
            }
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    currentUser?.id?.let { userId ->
                        paymentViewModel.fetchPaymentDetails(userId)
                    }
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }
} 