package com.example.tiendasuplementacion.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val paymentDetails by viewModel.paymentDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    // PaymentDetail is not trivially saveable; keep nullable object references in memory only
    var showDeleteConfirmation by remember { mutableStateOf<PaymentDetail?>(null) }
    var paymentToEdit by remember { mutableStateOf<PaymentDetail?>(null) }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showNetworkError by rememberSaveable { mutableStateOf(false) }
    var networkErrorMessage by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect ViewModel one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.example.tiendasuplementacion.viewmodel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
                is com.example.tiendasuplementacion.viewmodel.UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
                is com.example.tiendasuplementacion.viewmodel.UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }
                com.example.tiendasuplementacion.viewmodel.UiEvent.NavigateBack -> {
                    navController.navigateUp()
                }
            }
        }
    }

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            viewModel.fetchPaymentDetails(userId)
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
            Text(
                text = "Métodos de Pago",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF6E7DF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                    Text(
                        text = "No hay métodos de pago registrados",
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
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                        modifier = Modifier.weight(1f),
                                        softWrap = true,
                                        overflow = TextOverflow.Visible
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { paymentToEdit = paymentDetail }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar método de pago",
                                                tint = Color(0xFFF6E7DF)
                                            )
                                        }
                                        IconButton(
                                            onClick = { showDeleteConfirmation = paymentDetail }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar método de pago",
                                                tint = Color(0xFFF6E7DF)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (paymentDetail.cardNumber != null) {
                                    Text(
                                        text = "•••• •••• •••• ${paymentDetail.cardNumber.takeLast(4)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                        softWrap = true,
                                        overflow = TextOverflow.Visible
                                    )
                                    Text(
                                        text = "Vence: ${paymentDetail.expirationDate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                        softWrap = true,
                                        overflow = TextOverflow.Visible
                                    )
                                    Text(
                                        text = "Titular: ${paymentDetail.cardholderName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                        softWrap = true,
                                        overflow = TextOverflow.Visible
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Dirección de Facturación",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color(0xFFF6E7DF),
                                    fontWeight = FontWeight.Bold,
                                    softWrap = true,
                                    overflow = TextOverflow.Visible
                                )
                                Text(
                                    text = paymentDetail.addressLine1 ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                    softWrap = true,
                                    overflow = TextOverflow.Visible
                                )
                                paymentDetail.addressLine2?.let { addressLine2 ->
                                    Text(
                                        text = addressLine2,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                        softWrap = true,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                                Text(
                                    text = "${paymentDetail.city ?: ""}, ${paymentDetail.stateOrProvince ?: ""}".trim().trimStart(','),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                    softWrap = true,
                                    overflow = TextOverflow.Visible
                                )
                                Text(
                                    text = "${paymentDetail.country ?: ""} ${paymentDetail.postalCode ?: ""}".trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                    softWrap = true,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("payment_config") },
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearError()
            },
            title = { Text(text = "Error") },
            text = { Text(text = error ?: "Ha ocurrido un error") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text(text = "OK")
                }
            }
        )
    }

    showDeleteConfirmation?.let { paymentDetail ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text(text = "Confirmar Eliminación") },
            text = { 
                Text(text = "¿Estás seguro que deseas eliminar este método de pago?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePaymentDetail(paymentDetail.id)
                        showDeleteConfirmation = null
                    }
                ) {
                    Text(text = "Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    // Edit Dialog
    paymentToEdit?.let { paymentDetail ->
    var editedCardNumber by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.cardNumber ?: "") }
    var editedExpirationDate by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.expirationDate ?: "") }
    var editedCardholderName by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.cardholderName ?: "") }
    var editedCountry by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.country ?: "") }
    var editedAddressLine1 by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.addressLine1 ?: "") }
    var editedAddressLine2 by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.addressLine2 ?: "") }
    var editedCity by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.city ?: "") }
    var editedStateProvince by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.stateOrProvince ?: "") }
    var editedPostalCode by rememberSaveable(paymentDetail.id) { mutableStateOf(paymentDetail.postalCode ?: "") }

        AlertDialog(
            onDismissRequest = { paymentToEdit = null },
            title = { Text(text = "Editar Método de Pago") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (paymentDetail.payment.name in listOf("DEBITO", "CREDITO")) {
                        OutlinedTextField(
                            value = editedCardNumber,
                            onValueChange = { editedCardNumber = it },
                            label = { Text("Número de Tarjeta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedExpirationDate,
                            onValueChange = { editedExpirationDate = it },
                            label = { Text("Fecha de Vencimiento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editedCardholderName,
                            onValueChange = { editedCardholderName = it },
                            label = { Text("Nombre del Titular") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = editedCountry,
                        onValueChange = { editedCountry = it },
                        label = { Text("País") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedAddressLine1,
                        onValueChange = { editedAddressLine1 = it },
                        label = { Text("Dirección Línea 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedAddressLine2,
                        onValueChange = { editedAddressLine2 = it },
                        label = { Text("Dirección Línea 2 (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedCity,
                        onValueChange = { editedCity = it },
                        label = { Text("Ciudad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedStateProvince,
                        onValueChange = { editedStateProvince = it },
                        label = { Text("Estado/Provincia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedPostalCode,
                        onValueChange = { editedPostalCode = it },
                        label = { Text("Código Postal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedPaymentDetail = paymentDetail.copy(
                            cardNumber = if (paymentDetail.payment.name in listOf("DEBITO", "CREDITO")) editedCardNumber else null,
                            expirationDate = if (paymentDetail.payment.name in listOf("DEBITO", "CREDITO")) editedExpirationDate else null,
                            cardholderName = if (paymentDetail.payment.name in listOf("DEBITO", "CREDITO")) editedCardholderName else null,
                            country = editedCountry,
                            addressLine1 = editedAddressLine1,
                            addressLine2 = editedAddressLine2.ifBlank { null },
                            city = editedCity,
                            stateOrProvince = editedStateProvince,
                            postalCode = editedPostalCode
                        )
                        viewModel.updatePaymentDetail(updatedPaymentDetail)
                        paymentToEdit = null
                    }
                ) {
                    Text(text = "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToEdit = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}


