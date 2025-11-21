package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.util.PaymentValidation
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
    
    // Estados de validación para cada campo
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var expirationDateError by remember { mutableStateOf<String?>(null) }
    var cvcError by remember { mutableStateOf<String?>(null) }
    var cardholderNameError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var stateError by remember { mutableStateOf<String?>(null) }
    var postalCodeError by remember { mutableStateOf<String?>(null) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    val payments by viewModel.payments.observeAsState(emptyList())
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
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
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = payments.find { it.id == selectedPaymentId }?.name ?: "",
                    onValueChange = { },
                    label = { Text("Método de Pago", color = Color(0xFFF6E7DF)) },
                    readOnly = true,
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        focusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        focusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedTextColor = Color(0xFFF6E7DF)
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(Color(0xFF23242A))
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
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color(0xFFF6E7DF),
                                leadingIconColor = Color(0xFFF6E7DF),
                                trailingIconColor = Color(0xFFF6E7DF)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val selectedPayment = payments.find { it.id == selectedPaymentId }
            val isCardPayment = selectedPayment?.name in listOf("debito", "credito", "credit_card", "debit_card", "Debito", "Credito")
            
            if (isCardPayment) {
                // Título para los campos de tarjeta
                Text(
                    text = "Información de la Tarjeta",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF6E7DF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->
                        // Extraer solo dígitos del nuevo valor
                        val digitsOnly = newValue.filter { it.isDigit() }
                        if (digitsOnly.length <= PaymentValidation.FieldLimits.CARD_NUMBER_MAX) {
                            cardNumber = PaymentValidation.formatCardNumber(digitsOnly)
                            // Validar en tiempo real
                            if (digitsOnly.isNotEmpty()) {
                                val validation = PaymentValidation.validateCardNumber(digitsOnly)
                                cardNumberError = if (!validation.isValid) validation.errorMessage else null
                            } else {
                                cardNumberError = null
                            }
                        }
                    },
                    label = { Text("Número de Tarjeta", color = Color(0xFFF6E7DF)) },
                    isError = cardNumberError != null,
                    supportingText = {
                        if (cardNumberError != null) {
                            Text(cardNumberError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("${cardNumber.filter { it.isDigit() }.length}/${PaymentValidation.FieldLimits.CARD_NUMBER_MAX}", 
                                color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        focusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        focusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedTextColor = Color(0xFFF6E7DF),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorLabelColor = Color(0xFFFF6B6B)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = expirationDate,
                        onValueChange = { newValue ->
                            // Extraer solo dígitos
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 4) {
                                expirationDate = PaymentValidation.formatExpirationDate(digitsOnly)
                                // Validar si tiene 4 dígitos (MMAA)
                                if (digitsOnly.length == 4) {
                                    val validation = PaymentValidation.validateExpirationDate(digitsOnly)
                                    expirationDateError = if (!validation.isValid) validation.errorMessage else null
                                } else {
                                    expirationDateError = null
                                }
                            }
                        },
                        label = { Text("Vencimiento (MM/AA)", color = Color(0xFFF6E7DF)) },
                        isError = expirationDateError != null,
                        supportingText = {
                            if (expirationDateError != null) {
                                Text(expirationDateError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFF6E7DF),
                            focusedBorderColor = Color(0xFFF6E7DF),
                            unfocusedLabelColor = Color(0xFFF6E7DF),
                            focusedLabelColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedTextColor = Color(0xFFF6E7DF),
                            errorBorderColor = Color(0xFFFF6B6B),
                            errorLabelColor = Color(0xFFFF6B6B)
                        )
                    )

                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { newValue ->
                            // Solo permitir dígitos, máximo 4
                            val cleaned = newValue.filter { it.isDigit() }
                            if (cleaned.length <= PaymentValidation.FieldLimits.CVC_MAX) {
                                cvc = cleaned
                                // Validar
                                if (cleaned.isNotEmpty()) {
                                    val validation = PaymentValidation.validateCVC(cleaned)
                                    cvcError = if (!validation.isValid) validation.errorMessage else null
                                } else {
                                    cvcError = null
                                }
                            }
                        },
                        label = { Text("CVC", color = Color(0xFFF6E7DF)) },
                        isError = cvcError != null,
                        supportingText = {
                            if (cvcError != null) {
                                Text(cvcError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("${cvc.length}/${PaymentValidation.FieldLimits.CVC_MAX}", 
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFF6E7DF),
                            focusedBorderColor = Color(0xFFF6E7DF),
                            unfocusedLabelColor = Color(0xFFF6E7DF),
                            focusedLabelColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedTextColor = Color(0xFFF6E7DF),
                            errorBorderColor = Color(0xFFFF6B6B),
                            errorLabelColor = Color(0xFFFF6B6B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { newValue ->
                        // Limitar longitud
                        if (newValue.length <= PaymentValidation.FieldLimits.CARDHOLDER_NAME_MAX) {
                            cardholderName = newValue.uppercase()
                            // Validar
                            if (newValue.isNotBlank()) {
                                val validation = PaymentValidation.validateCardholderName(newValue)
                                cardholderNameError = if (!validation.isValid) validation.errorMessage else null
                            } else {
                                cardholderNameError = null
                            }
                        }
                    },
                    label = { Text("Nombre del Titular", color = Color(0xFFF6E7DF)) },
                    isError = cardholderNameError != null,
                    supportingText = {
                        if (cardholderNameError != null) {
                            Text(cardholderNameError!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("${cardholderName.length}/${PaymentValidation.FieldLimits.CARDHOLDER_NAME_MAX}", 
                                color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        focusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        focusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedTextColor = Color(0xFFF6E7DF),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorLabelColor = Color(0xFFFF6B6B)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
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
                onValueChange = { newValue ->
                    if (newValue.length <= PaymentValidation.FieldLimits.COUNTRY_MAX) {
                        country = newValue
                        if (newValue.isNotBlank()) {
                            val validation = PaymentValidation.validateCountry(newValue)
                            countryError = if (!validation.isValid) validation.errorMessage else null
                        } else {
                            countryError = null
                        }
                    }
                },
                label = { Text("País *", color = Color(0xFFF6E7DF)) },
                isError = countryError != null,
                supportingText = {
                    if (countryError != null) {
                        Text(countryError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${country.length}/${PaymentValidation.FieldLimits.COUNTRY_MAX}", 
                            color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    focusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    focusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF),
                    focusedTextColor = Color(0xFFF6E7DF),
                    errorBorderColor = Color(0xFFFF6B6B),
                    errorLabelColor = Color(0xFFFF6B6B)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressLine1,
                onValueChange = { newValue ->
                    if (newValue.length <= PaymentValidation.FieldLimits.ADDRESS_MAX) {
                        addressLine1 = newValue
                        if (newValue.isNotBlank()) {
                            val validation = PaymentValidation.validateAddress(newValue)
                            addressError = if (!validation.isValid) validation.errorMessage else null
                        } else {
                            addressError = null
                        }
                    }
                },
                label = { Text("Dirección Línea 1 *", color = Color(0xFFF6E7DF)) },
                isError = addressError != null,
                supportingText = {
                    if (addressError != null) {
                        Text(addressError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${addressLine1.length}/${PaymentValidation.FieldLimits.ADDRESS_MAX}", 
                            color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    focusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    focusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF),
                    focusedTextColor = Color(0xFFF6E7DF),
                    errorBorderColor = Color(0xFFFF6B6B),
                    errorLabelColor = Color(0xFFFF6B6B)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressLine2,
                onValueChange = { newValue ->
                    if (newValue.length <= PaymentValidation.FieldLimits.ADDRESS_MAX) {
                        addressLine2 = newValue
                    }
                },
                label = { Text("Dirección Línea 2 (Opcional)", color = Color(0xFFF6E7DF)) },
                supportingText = {
                    Text("${addressLine2.length}/${PaymentValidation.FieldLimits.ADDRESS_MAX}", 
                        color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    focusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    focusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF),
                    focusedTextColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { newValue ->
                    if (newValue.length <= PaymentValidation.FieldLimits.CITY_MAX) {
                        city = newValue
                        if (newValue.isNotBlank()) {
                            val validation = PaymentValidation.validateCity(newValue)
                            cityError = if (!validation.isValid) validation.errorMessage else null
                        } else {
                            cityError = null
                        }
                    }
                },
                label = { Text("Ciudad *", color = Color(0xFFF6E7DF)) },
                isError = cityError != null,
                supportingText = {
                    if (cityError != null) {
                        Text(cityError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${city.length}/${PaymentValidation.FieldLimits.CITY_MAX}", 
                            color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF6E7DF),
                    focusedBorderColor = Color(0xFFF6E7DF),
                    unfocusedLabelColor = Color(0xFFF6E7DF),
                    focusedLabelColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF),
                    focusedTextColor = Color(0xFFF6E7DF),
                    errorBorderColor = Color(0xFFFF6B6B),
                    errorLabelColor = Color(0xFFFF6B6B)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = stateOrProvince,
                    onValueChange = { newValue ->
                        if (newValue.length <= PaymentValidation.FieldLimits.STATE_MAX) {
                            stateOrProvince = newValue
                            if (newValue.isNotBlank()) {
                                val validation = PaymentValidation.validateState(newValue)
                                stateError = if (!validation.isValid) validation.errorMessage else null
                            } else {
                                stateError = null
                            }
                        }
                    },
                    label = { Text("Estado/Provincia *", color = Color(0xFFF6E7DF)) },
                    isError = stateError != null,
                    supportingText = {
                        if (stateError != null) {
                            Text(stateError!!, color = MaterialTheme.colorScheme.error, maxLines = 1)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        focusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        focusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedTextColor = Color(0xFFF6E7DF),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorLabelColor = Color(0xFFFF6B6B)
                    )
                )

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { newValue ->
                        if (newValue.length <= PaymentValidation.FieldLimits.POSTAL_CODE_MAX) {
                            postalCode = newValue.uppercase()
                            if (newValue.isNotBlank()) {
                                val validation = PaymentValidation.validatePostalCode(newValue)
                                postalCodeError = if (!validation.isValid) validation.errorMessage else null
                            } else {
                                postalCodeError = null
                            }
                        }
                    },
                    label = { Text("Código Postal *", color = Color(0xFFF6E7DF)) },
                    isError = postalCodeError != null,
                    supportingText = {
                        if (postalCodeError != null) {
                            Text(postalCodeError!!, color = MaterialTheme.colorScheme.error, maxLines = 1)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFF6E7DF),
                        focusedBorderColor = Color(0xFFF6E7DF),
                        unfocusedLabelColor = Color(0xFFF6E7DF),
                        focusedLabelColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedTextColor = Color(0xFFF6E7DF),
                        errorBorderColor = Color(0xFFFF6B6B),
                        errorLabelColor = Color(0xFFFF6B6B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Validación del método de pago seleccionado
                    if (selectedPaymentId == null) {
                        errorMessage = "Por favor seleccione un método de pago"
                        showError = true
                        return@Button
                    }

                    val selectedPayment = payments.find { it.id == selectedPaymentId }
                    val isCardPayment = selectedPayment?.requiresCardInfo() ?: false
                    
                    // Validaciones de campos de tarjeta si es necesario
                    if (isCardPayment) {
                        // Validar número de tarjeta
                        if (cardNumber.isBlank()) {
                            errorMessage = "El número de tarjeta es requerido"
                            showError = true
                            return@Button
                        }
                        val cardValidation = PaymentValidation.validateCardNumber(cardNumber.filter { it.isDigit() })
                        if (!cardValidation.isValid) {
                            errorMessage = cardValidation.errorMessage ?: "Número de tarjeta inválido"
                            showError = true
                            return@Button
                        }

                        // Validar fecha de expiración
                        if (expirationDate.isBlank()) {
                            errorMessage = "La fecha de expiración es requerida"
                            showError = true
                            return@Button
                        }
                        val expDateValidation = PaymentValidation.validateExpirationDate(expirationDate.filter { it.isDigit() })
                        if (!expDateValidation.isValid) {
                            errorMessage = expDateValidation.errorMessage ?: "Fecha de expiración inválida"
                            showError = true
                            return@Button
                        }

                        // Validar CVC
                        if (cvc.isBlank()) {
                            errorMessage = "El CVC es requerido"
                            showError = true
                            return@Button
                        }
                        val cvcValidation = PaymentValidation.validateCVC(cvc)
                        if (!cvcValidation.isValid) {
                            errorMessage = cvcValidation.errorMessage ?: "CVC inválido"
                            showError = true
                            return@Button
                        }

                        // Validar nombre del titular
                        if (cardholderName.isBlank()) {
                            errorMessage = "El nombre del titular es requerido"
                            showError = true
                            return@Button
                        }
                        val nameValidation = PaymentValidation.validateCardholderName(cardholderName)
                        if (!nameValidation.isValid) {
                            errorMessage = nameValidation.errorMessage ?: "Nombre del titular inválido"
                            showError = true
                            return@Button
                        }
                    }

                    // Validaciones de campos de dirección (siempre requeridos)
                    if (country.isBlank()) {
                        errorMessage = "El país es requerido"
                        showError = true
                        return@Button
                    }
                    val countryValidation = PaymentValidation.validateCountry(country)
                    if (!countryValidation.isValid) {
                        errorMessage = countryValidation.errorMessage ?: "País inválido"
                        showError = true
                        return@Button
                    }

                    if (addressLine1.isBlank()) {
                        errorMessage = "La dirección es requerida"
                        showError = true
                        return@Button
                    }
                    val addressValidation = PaymentValidation.validateAddress(addressLine1)
                    if (!addressValidation.isValid) {
                        errorMessage = addressValidation.errorMessage ?: "Dirección inválida"
                        showError = true
                        return@Button
                    }

                    if (city.isBlank()) {
                        errorMessage = "La ciudad es requerida"
                        showError = true
                        return@Button
                    }
                    val cityValidation = PaymentValidation.validateCity(city)
                    if (!cityValidation.isValid) {
                        errorMessage = cityValidation.errorMessage ?: "Ciudad inválida"
                        showError = true
                        return@Button
                    }

                    if (stateOrProvince.isBlank()) {
                        errorMessage = "El estado/provincia es requerido"
                        showError = true
                        return@Button
                    }
                    val stateValidation = PaymentValidation.validateState(stateOrProvince)
                    if (!stateValidation.isValid) {
                        errorMessage = stateValidation.errorMessage ?: "Estado/provincia inválido"
                        showError = true
                        return@Button
                    }

                    if (postalCode.isBlank()) {
                        errorMessage = "El código postal es requerido"
                        showError = true
                        return@Button
                    }
                    val postalValidation = PaymentValidation.validatePostalCode(postalCode)
                    if (!postalValidation.isValid) {
                        errorMessage = postalValidation.errorMessage ?: "Código postal inválido"
                        showError = true
                        return@Button
                    }

                    // Si todas las validaciones pasan, guardar
                    currentUser?.id?.let { userId ->
                        isSubmitting = true
                        val paymentDetail = PaymentDetail(
                            id = 0,
                            active = true,
                            payment = selectedPayment!!,
                            payment_id = selectedPaymentId!!,
                            user = currentUser!!,
                            user_id = userId,
                            cardNumber = if (isCardPayment) cardNumber.filter { it.isDigit() } else null,
                            expirationDate = if (isCardPayment) expirationDate else null,
                            cvc = if (isCardPayment) cvc else null,
                            cardholderName = if (isCardPayment) cardholderName else null,
                            country = country.trim(),
                            addressLine1 = addressLine1.trim(),
                            addressLine2 = addressLine2.trim().ifBlank { null },
                            city = city.trim(),
                            stateOrProvince = stateOrProvince.trim(),
                            postalCode = postalCode.trim()
                        )
                        
                        paymentToSave = paymentDetail
                        shouldSave = true
                    } ?: run {
                        errorMessage = "Error: Usuario no encontrado"
                        showError = true
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6E7DF),
                    contentColor = Color(0xFF23242A),
                    disabledContainerColor = Color(0xFFF6E7DF).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF23242A).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF23242A)
                    )
                } else {
                    Text("Guardar Método de Pago", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                containerColor = Color(0xFF23242A),
                titleContentColor = Color(0xFFF6E7DF),
                textContentColor = Color(0xFFF6E7DF),
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(
                        onClick = { showError = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFF6E7DF)
                        )
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}