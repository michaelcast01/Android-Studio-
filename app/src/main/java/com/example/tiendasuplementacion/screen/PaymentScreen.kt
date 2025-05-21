package com.example.tiendasuplementacion.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.tiendasuplementacion.model.PaymentDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: PaymentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val paymentDetails by viewModel.paymentDetails.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.id) {
        Log.d("PaymentScreen", "Current user: $currentUser")
        currentUser?.id?.let { userId ->
            Log.d("PaymentScreen", "Fetching payment details for user ID: $userId")
            viewModel.fetchPaymentDetails(userId)
        } ?: run {
            Log.e("PaymentScreen", "No user ID available")
        }
    }

    LaunchedEffect(paymentDetails) {
        Log.d("PaymentScreen", "Payment details updated: $paymentDetails")
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
                text = "Métodos de Pago",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF6E7DF),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (paymentDetails.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay métodos de pago registrados",
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(paymentDetails) { paymentDetail ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF26272B)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = paymentDetail.payment.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFF6E7DF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        when (paymentDetail.payment.name) {
                                            "DEBITO" -> Icons.Default.CreditCard
                                            "CREDITO" -> Icons.Default.CreditCard
                                            "CASH" -> Icons.Default.AttachMoney
                                            else -> Icons.Default.Payment
                                        },
                                        contentDescription = "Tipo de pago",
                                        tint = Color(0xFFF6E7DF)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (!paymentDetail.cardNumber.isNullOrEmpty()) {
                                    Text(
                                        text = "Número de tarjeta: ${paymentDetail.cardNumber}",
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                    )
                                }
                                
                                if (!paymentDetail.cardholderName.isNullOrEmpty()) {
                                    Text(
                                        text = "Titular: ${paymentDetail.cardholderName}",
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                    )
                                }

                                if (!paymentDetail.country.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Dirección de facturación:",
                                        color = Color(0xFFF6E7DF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = paymentDetail.addressLine1 ?: "",
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                    )
                                    if (!paymentDetail.addressLine2.isNullOrEmpty()) {
                                        Text(
                                            text = paymentDetail.addressLine2,
                                            color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        text = "${paymentDetail.city}, ${paymentDetail.stateOrProvince}",
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "${paymentDetail.country} ${paymentDetail.postalCode}",
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Agregar FloatingActionButton
        FloatingActionButton(
            onClick = {
                navController.navigate("payment_config")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFFF6E7DF),
            contentColor = Color(0xFF23242A)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Agregar método de pago"
            )
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    currentUser?.id?.let { userId ->
                        viewModel.fetchPaymentDetails(userId)
                    }
                },
                onDismiss = { showNetworkError = false }
            )
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearError()
            },
            title = { Text("Error") },
            text = { Text(error ?: "Ha ocurrido un error") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}


