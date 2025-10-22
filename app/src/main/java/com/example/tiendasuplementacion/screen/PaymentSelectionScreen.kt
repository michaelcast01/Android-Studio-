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
import com.example.tiendasuplementacion.model.PaymentMethods
import com.example.tiendasuplementacion.model.PaymentDetail
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionScreen(
    navController: NavController,
    paymentViewModel: PaymentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onPaymentSelected: (PaymentDetail) -> Unit
) {
    val paymentDetails by paymentViewModel.paymentDetails.observeAsState(emptyList())
    val isLoading by paymentViewModel.isLoading.observeAsState(false)
    val paymentDetailsUiState by paymentViewModel.paymentDetailsUiState.collectAsState()
    val error by paymentViewModel.error.observeAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.id) {
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFFF6E7DF))
                }
                Text(
                    text = "Seleccionar Método de Pago",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFF6E7DF),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (paymentDetailsUiState) {
                is com.example.tiendasuplementacion.viewmodel.UiState.Loading -> {
                    // skeleton placeholders
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(4) { PaymentPlaceholderCard() }
                    }
                }
                is com.example.tiendasuplementacion.viewmodel.UiState.Success -> {
                    val list = (paymentDetailsUiState as com.example.tiendasuplementacion.viewmodel.UiState.Success).data
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "No hay métodos de pago configurados", color = Color(0xFFF6E7DF), style = MaterialTheme.typography.bodyLarge)
                                Button(onClick = { navController.navigate("payment_config") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                    Text("Agregar Método de Pago")
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(list, key = { it.id }) { paymentDetail ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onPaymentSelected(paymentDetail) },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26272B)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                                                Text(text = paymentDetail.payment.name, style = MaterialTheme.typography.titleMedium, color = Color(0xFFF6E7DF))
                                                if (paymentDetail.cardNumber != null) {
                                                    Text(text = "•••• " + paymentDetail.cardNumber.takeLast(4), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF6E7DF).copy(alpha = 0.7f))
                                                    Text(text = "Vence: ${paymentDetail.expirationDate}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                                    Text(text = "Titular: ${paymentDetail.cardholderName}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = "Dirección de facturación:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                                Text(text = buildString {
                                                    append(paymentDetail.addressLine1)
                                                    if (!paymentDetail.addressLine2.isNullOrBlank()) append(", ${paymentDetail.addressLine2}")
                                                }, style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                                Text(text = "${paymentDetail.city}, ${paymentDetail.stateOrProvince}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                                Text(text = "${paymentDetail.country} ${paymentDetail.postalCode}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is com.example.tiendasuplementacion.viewmodel.UiState.Error -> {
                    val msg = (paymentDetailsUiState as com.example.tiendasuplementacion.viewmodel.UiState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error cargando métodos de pago", color = Color(0xFFF6E7DF))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = msg, color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { currentUser?.id?.let { paymentViewModel.fetchPaymentDetails(it) } }) { Text("Reintentar") }
                        }
                    }
                }
            }
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onDismiss = { showNetworkError = false },
                onRetry = {
                    showNetworkError = false
                    currentUser?.id?.let { userId -> paymentViewModel.fetchPaymentDetails(userId) }
                }
            )
        }
    }
}

@Composable
fun PaymentPlaceholderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26272B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp).background(Color(0xFF333438)).alpha(0.9f))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp).background(Color(0xFF333438)).alpha(0.9f))
            }
        }
    }
}