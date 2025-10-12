package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import com.example.tiendasuplementacion.model.PaymentMethods
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.viewmodel.UiEvent
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreen(
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    val payments by viewModel.payments.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)
    var showNetworkError by rememberSaveable { mutableStateOf(false) }
    var networkErrorMessage by rememberSaveable { mutableStateOf("") }
    var showAddPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var showEditPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var newPaymentName by rememberSaveable { mutableStateOf("") }
    var selectedPaymentId by rememberSaveable { mutableStateOf<Long?>(null) }

    var selectedPayment = remember(payments, selectedPaymentId) { selectedPaymentId?.let { id -> payments.find { it.id == id } } }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchPayments()
    }

    LaunchedEffect(error) {
        error?.let {
            showNetworkError = true
            networkErrorMessage = it
        }
    }

    // Collect events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowError -> {
                    showNetworkError = true
                    networkErrorMessage = event.message
                }
                is UiEvent.Navigate -> navController.navigate(event.route) { launchSingleTop = true }
                is UiEvent.NavigateBack -> navController.popBackStack()
            }
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
                text = "Gestión de Métodos de Pago",
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
                    items(payments) { payment ->
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (payment.method) {
                                        PaymentMethods.CREDIT_CARD, PaymentMethods.DEBIT_CARD -> Icons.Default.CreditCard
                                        else -> Icons.Default.AccountBalance
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFFF6E7DF),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = payment.name ?: "Sin nombre",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFF6E7DF)
                                    )
                                    Text(
                                        text = when {
                                            payment.method != null -> payment.method
                                            payment.name.contains("PSE", ignoreCase = true) -> PaymentMethods.PSE
                                            payment.name.contains("CREDITO", ignoreCase = true) -> PaymentMethods.CREDIT_CARD
                                            payment.name.contains("DEBITO", ignoreCase = true) -> PaymentMethods.DEBIT_CARD
                                            payment.name.contains("EFECTIVO", ignoreCase = true) -> PaymentMethods.CASH
                                            else -> "Método de pago"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        selectedPaymentId = payment.id
                                        newPaymentName = payment.name ?: ""
                                        showEditPaymentDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar método de pago",
                                        tint = Color(0xFFF6E7DF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón flotante para agregar nuevo método de pago
        FloatingActionButton(
            onClick = { showAddPaymentDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFFF6E7DF),
            contentColor = Color(0xFF23242A)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar método de pago")
        }

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    viewModel.fetchPayments()
                },
                onDismiss = { showNetworkError = false }
            )
        }

        if (showAddPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddPaymentDialog = false
                    newPaymentName = ""
                },
                title = {
                    Text(
                        "Agregar Método de Pago",
                        color = Color(0xFFF6E7DF)
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPaymentName,
                            onValueChange = { newPaymentName = it },
                            label = { Text("Nombre del método de pago") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFF6E7DF),
                                unfocusedTextColor = Color(0xFFF6E7DF),
                                focusedBorderColor = Color(0xFFF6E7DF),
                                unfocusedBorderColor = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                focusedLabelColor = Color(0xFFF6E7DF),
                                unfocusedLabelColor = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                cursorColor = Color(0xFFF6E7DF)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPaymentName.isNotBlank()) {
                                viewModel.createPayment(Payment(name = newPaymentName))
                                showAddPaymentDialog = false
                                newPaymentName = ""
                            }
                        }
                    ) {
                        Text("Agregar", color = Color(0xFFF6E7DF))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddPaymentDialog = false
                            newPaymentName = ""
                        }
                    ) {
                        Text("Cancelar", color = Color(0xFFF6E7DF))
                    }
                },
                containerColor = Color(0xFF26272B),
                shape = RoundedCornerShape(16.dp)
            )
        }

                if (showEditPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showEditPaymentDialog = false
                    newPaymentName = ""
                            selectedPaymentId = null
                },
                title = {
                    Text(
                        "Editar Método de Pago",
                        color = Color(0xFFF6E7DF)
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPaymentName,
                            onValueChange = { newPaymentName = it },
                            label = { Text("Nombre del método de pago") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFFF6E7DF),
                                unfocusedTextColor = Color(0xFFF6E7DF),
                                focusedBorderColor = Color(0xFFF6E7DF),
                                unfocusedBorderColor = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                focusedLabelColor = Color(0xFFF6E7DF),
                                unfocusedLabelColor = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                cursorColor = Color(0xFFF6E7DF)
                            )
                        )
                    }
                },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (newPaymentName.isNotBlank()) {
                                        selectedPayment?.let { sp ->
                                            viewModel.update(sp.id, sp.copy(name = newPaymentName))
                                        }
                                        showEditPaymentDialog = false
                                        newPaymentName = ""
                                        selectedPaymentId = null
                                    }
                                }
                            ) {
                                Text("Guardar", color = Color(0xFFF6E7DF))
                            }
                        },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditPaymentDialog = false
                            newPaymentName = ""
                            selectedPayment = null
                        }
                    ) {
                        Text("Cancelar", color = Color(0xFFF6E7DF))
                    }
                },
                containerColor = Color(0xFF26272B),
                shape = RoundedCornerShape(16.dp)
            )
        }
        // Snackbar host for one-shot messages
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
} 