package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.viewmodel.OrderViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PSEPaymentScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedBank by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var personType by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotalPrice()
    val currentUser by authViewModel.currentUser.collectAsState()

    val banks = listOf(
        "Bancolombia",
        "Banco de Bogotá",
        "Banco Popular",
        "Banco de Occidente",
        "Banco AV Villas",
        "Banco Caja Social",
        "Banco GNB Sudameris",
        "Banco Pichincha",
        "Banco Santander",
        "Banco BBVA Colombia"
    )

    val documentTypes = listOf(
        "Cédula de Ciudadanía",
        "Cédula de Extranjería",
        "Pasaporte",
        "NIT"
    )

    val personTypes = listOf(
        "Persona Natural",
        "Persona Jurídica"
    )

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
                .verticalScroll(rememberScrollState())
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
                    text = "Pago PSE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF26272B)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de la Compra",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFF6E7DF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total a Pagar: $${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF6E7DF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Información de Pago",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFF6E7DF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBank,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Seleccionar Banco", color = Color(0xFFF6E7DF)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF26272B),
                        unfocusedContainerColor = Color(0xFF26272B),
                        focusedTextColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedIndicatorColor = Color(0xFFF6E7DF),
                        unfocusedIndicatorColor = Color(0xFFF6E7DF)
                    )
                )

                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    banks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank, color = Color(0xFFF6E7DF)) },
                            onClick = { selectedBank = bank }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = documentType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de Documento", color = Color(0xFFF6E7DF)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF26272B),
                        unfocusedContainerColor = Color(0xFF26272B),
                        focusedTextColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedIndicatorColor = Color(0xFFF6E7DF),
                        unfocusedIndicatorColor = Color(0xFFF6E7DF)
                    )
                )

                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    documentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = Color(0xFFF6E7DF)) },
                            onClick = { documentType = type }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = documentNumber,
                onValueChange = { documentNumber = it },
                label = { Text("Número de Documento", color = Color(0xFFF6E7DF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF26272B),
                    unfocusedContainerColor = Color(0xFF26272B),
                    focusedTextColor = Color(0xFFF6E7DF),
                    unfocusedTextColor = Color(0xFFF6E7DF),
                    focusedIndicatorColor = Color(0xFFF6E7DF),
                    unfocusedIndicatorColor = Color(0xFFF6E7DF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = personType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de Persona", color = Color(0xFFF6E7DF)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF26272B),
                        unfocusedContainerColor = Color(0xFF26272B),
                        focusedTextColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedIndicatorColor = Color(0xFFF6E7DF),
                        unfocusedIndicatorColor = Color(0xFFF6E7DF)
                    )
                )

                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    personTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = Color(0xFFF6E7DF)) },
                            onClick = { personType = type }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (selectedBank.isBlank() || documentType.isBlank() || 
                        documentNumber.isBlank() || personType.isBlank()) {
                        errorMessage = "Por favor complete todos los campos"
                        showError = true
                    } else {
                        isLoading = true
                        // Aquí iría la lógica para procesar el pago
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6E7DF)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF23242A)
                    )
                } else {
                    Text(
                        "Pagar $${String.format("%.2f", total)}",
                        color = Color(0xFF23242A)
                    )
                }
            }
        }
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("¡Pago Exitoso!") },
            text = { Text("Tu pago ha sido procesado correctamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate("products") {
                            launchSingleTop = true
                            popUpTo("cart") { inclusive = true }
                        }
                    }
                ) {
                    Text("Volver a Productos")
                }
            }
        )
    }
} 