package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import com.example.tiendasuplementacion.util.PaymentValidation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    var method by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var methodError by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    LaunchedEffect(error) {
        if (error != null) {
            android.util.Log.e("PaymentFormScreen", "Error en PaymentViewModel: $error")
            errorMessage = "No se pudo guardar el método de pago. Intenta nuevamente."
            showError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .align(Alignment.Center),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Agregar Método de Pago",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF)
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        if (newValue.length <= PaymentValidation.FieldLimits.PAYMENT_NAME_MAX) {
                            name = newValue
                            // Validar en tiempo real
                            if (newValue.isNotBlank()) {
                                val validation = PaymentValidation.validatePaymentName(newValue)
                                nameError = if (!validation.isValid) validation.errorMessage else null
                            } else {
                                nameError = null
                            }
                        }
                    },
                    label = { Text("Nombre del Método de Pago *") },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(nameError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "${name.length}/${PaymentValidation.FieldLimits.PAYMENT_NAME_MAX}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = method,
                    onValueChange = { newValue ->
                        if (newValue.length <= PaymentValidation.FieldLimits.PAYMENT_NAME_MAX) {
                            method = newValue
                            // Validación simple
                            if (newValue.isNotBlank()) {
                                methodError = if (newValue.length < 2) {
                                    "El método debe tener al menos 2 caracteres"
                                } else null
                            } else {
                                methodError = null
                            }
                        }
                    },
                    label = { Text("Tipo/Descripción del Método *") },
                    isError = methodError != null,
                    supportingText = {
                        if (methodError != null) {
                            Text(methodError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                "${method.length}/${PaymentValidation.FieldLimits.PAYMENT_NAME_MAX}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    placeholder = { Text("Ej: Tarjeta, PSE, Efectivo", style = MaterialTheme.typography.bodyMedium) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    )
                )

                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Validar nombre
                        if (name.isBlank()) {
                            errorMessage = "El nombre del método de pago es requerido"
                            showError = true
                            return@Button
                        }
                        
                        val nameValidation = PaymentValidation.validatePaymentName(name)
                        if (!nameValidation.isValid) {
                            errorMessage = nameValidation.errorMessage ?: "Nombre inválido"
                            showError = true
                            return@Button
                        }

                        // Validar método
                        if (method.isBlank()) {
                            errorMessage = "El tipo/descripción del método es requerido"
                            showError = true
                            return@Button
                        }

                        if (method.length < 2) {
                            errorMessage = "El método debe tener al menos 2 caracteres"
                            showError = true
                            return@Button
                        }

                        // Si todas las validaciones pasan, crear el pago
                        try {
                            viewModel.createPayment(
                                payment = Payment(
                                    name = name.trim(), 
                                    method = method.trim()
                                ),
                                onSuccess = {
                                    showSuccessDialog = true
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("PaymentFormScreen", "Error guardando método de pago", e)
                            errorMessage = "No se pudo guardar el método de pago. Intenta nuevamente."
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading && nameError == null && methodError == null && 
                             name.isNotBlank() && method.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar Método de Pago", color = Color.White)
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Éxito") },
            text = { Text("Método de pago agregado correctamente") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
} 