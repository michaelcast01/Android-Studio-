package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.model.PaymentMethods
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentConfigScreen(
    navController: NavController,
    viewModel: PaymentViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedPaymentId by remember { mutableStateOf<Long?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateOrProvince by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var shouldSave by remember { mutableStateOf(false) }
    var paymentToSave by remember { mutableStateOf<PaymentDetail?>(null) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    val payments by viewModel.payments.observeAsState(emptyList())
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.fetchPayments()
    }

    LaunchedEffect(shouldSave) {
        if (shouldSave && paymentToSave != null) {
            val success = viewModel.savePaymentDetail(paymentToSave!!)
            if (success) {
                navController.navigateUp()
            }
            shouldSave = false
            paymentToSave = null
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
                .verticalScroll(scrollState)
        ) {
            // Barra superior con botón de retroceso
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFFF6E7DF)
                    )
                }
                Text(
                    text = "Configurar Método de Pago",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFF6E7DF),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de método de pago
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = payments.find { it.id == selectedPaymentId }?.name ?: "",
                    onValueChange = { },
                    label = { Text("Método de Pago", color = Color(0xFFF6E7DF)) },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    payments.forEach { payment ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = payment.name,
                                    color = Color(0xFFF6E7DF)
                                ) 
                            },
                            onClick = { 
                                selectedPaymentId = payment.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campos para tarjeta
            val selectedPayment = payments.find { it.id == selectedPaymentId }
            if (selectedPayment?.name in listOf("DEBITO", "CREDITO")) {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Número de Tarjeta", color = Color(0xFFF6E7DF)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = expirationDate,
                        onValueChange = { expirationDate = it },
                        label = { Text("Fecha Exp.", color = Color(0xFFF6E7DF)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFF6E7DF),
                            unfocusedLabelColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF)
                        )
                    )

                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it },
                        label = { Text("CVC", color = Color(0xFFF6E7DF)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFF6E7DF),
                            unfocusedLabelColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    label = { Text("Nombre del Titular", color = Color(0xFFF6E7DF)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campos de dirección
            Text(
                text = "Dirección de Facturación",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("País", color = Color(0xFFF6E7DF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                label = { Text("Dirección Línea 1", color = Color(0xFFF6E7DF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                label = { Text("Dirección Línea 2 (Opcional)", color = Color(0xFFF6E7DF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ciudad", color = Color(0xFFF6E7DF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = stateOrProvince,
                    onValueChange = { stateOrProvince = it },
                    label = { Text("Estado/Provincia", color = Color(0xFFF6E7DF)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF)
                    )
                )

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Código Postal", color = Color(0xFFF6E7DF)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedPaymentId == null) {
                        errorMessage = "Por favor seleccione un método de pago"
                        showError = true
                        return@Button
                    }

                    val selectedPayment = payments.find { it.id == selectedPaymentId }
                    if (selectedPayment?.name in listOf("DEBITO", "CREDITO")) {
                        if (cardNumber.isBlank() || expirationDate.isBlank() || cvc.isBlank() || cardholderName.isBlank()) {
                            errorMessage = "Por favor complete todos los campos de la tarjeta"
                            showError = true
                            return@Button
                        }
                    }

                    if (country.isBlank() || addressLine1.isBlank() || city.isBlank() || 
                        stateOrProvince.isBlank() || postalCode.isBlank()) {
                        errorMessage = "Por favor complete todos los campos de dirección requeridos"
                        showError = true
                        return@Button
                    }

                    currentUser?.id?.let { userId ->
                        val paymentDetail = PaymentDetail(
                            id = 0,
                            payment = selectedPayment!!,
                            payment_id = selectedPaymentId!!,
                            user = currentUser!!,
                            user_id = userId,
                            cardNumber = if (selectedPayment.name in listOf("DEBITO", "CREDITO")) cardNumber else null,
                            expirationDate = if (selectedPayment.name in listOf("DEBITO", "CREDITO")) expirationDate else null,
                            cvc = if (selectedPayment.name in listOf("DEBITO", "CREDITO")) cvc else null,
                            cardholderName = if (selectedPayment.name in listOf("DEBITO", "CREDITO")) cardholderName else null,
                            country = country,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2.ifBlank { null },
                            city = city,
                            stateOrProvince = stateOrProvince,
                            postalCode = postalCode
                        )
                        
                        paymentToSave = paymentDetail
                        shouldSave = true
                    } ?: run {
                        errorMessage = "Error: Usuario no encontrado"
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6E7DF),
                    contentColor = Color(0xFF23242A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar Método de Pago", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
} 