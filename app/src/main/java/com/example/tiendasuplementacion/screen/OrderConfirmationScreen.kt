package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.viewmodel.OrderViewModel
import com.example.tiendasuplementacion.viewmodel.OrderProductViewModel
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.model.TestPaymentRequest
import com.example.tiendasuplementacion.model.TestTokens
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import com.example.tiendasuplementacion.util.CurrencyFormatter
import androidx.compose.ui.platform.LocalContext

private fun isCardPaymentMethod(method: String?): Boolean {
    val cardMethods = listOf(
        "credito", "credit", "credit_card", "credito_tarjeta",
        "debito", "debit", "debit_card", "debito_tarjeta"
    )
    val isCard = method?.lowercase() in cardMethods
    Log.d("OrderConfirmation", "Checking payment method: '$method', lowercase: '${method?.lowercase()}', isCard: $isCard")
    return isCard
}

private fun isCardPaymentMethodByName(name: String?): Boolean {
    val cardNames = listOf(
        "debito", "credito", "credit_card", "debit_card",
        "DEBITO", "CREDITO", "Debito", "Credito",
        "credit", "debit", "credito_tarjeta", "debito_tarjeta"
    )
    val isCard = name in cardNames
    Log.d("OrderConfirmation", "Checking payment name: '$name', isCard: $isCard")
    return isCard
}

private fun getTestTokenFromCardNumber(cardNumber: String?): String {
    Log.d("OrderConfirmation", "Getting test token for card number: $cardNumber")

    if (cardNumber == null || cardNumber.length < 4) {
        Log.d("OrderConfirmation", "Card number is null or too short, using default VISA token")
        return TestTokens.VISA // Default token
    }

    val lastDigit = cardNumber.last().digitToIntOrNull() ?: 0
    Log.d("OrderConfirmation", "Last digit of card: $lastDigit")

    val token = when (lastDigit) {
        in 0..3 -> TestTokens.VISA
        in 4..6 -> TestTokens.CHARGE_DECLINED
        in 7..9 -> TestTokens.CHARGE_DECLINED_INSUFFICIENT_FUNDS
        else -> TestTokens.VISA
    }

    Log.d("OrderConfirmation", "Selected token: $token")
    return token
}

private fun mapPaymentResponseToStatus(
    response: com.example.tiendasuplementacion.model.TestPaymentResponse?,
    isSuccess: Boolean
): Long {
    Log.d("OrderConfirmation", "=== MAPPING PAYMENT RESPONSE TO STATUS ===")
    Log.d("OrderConfirmation", "Response object: $response")
    Log.d("OrderConfirmation", "isSuccess parameter: $isSuccess")
    Log.d("OrderConfirmation", "Response status: '${response?.status}'")
    Log.d("OrderConfirmation", "Response message: '${response?.message}'")

    val status = if (isSuccess && response?.status == "succeeded") {
        Log.d("OrderConfirmation", "Condition matched: isSuccess && status == 'succeeded' -> returning 2L")
        2L // Exitoso
    } else if (response?.message?.contains("insufficient funds", ignoreCase = true) == true) {
        Log.d("OrderConfirmation", "Condition matched: message contains 'insufficient funds' -> returning 3L")
        3L // Sin fondos
    } else {
        Log.d("OrderConfirmation", "No conditions matched -> returning 4L (denegado)")
        4L // Denegado
    }

    Log.d("OrderConfirmation", "Final mapped status: $status")
    return status
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    selectedPaymentDetail: PaymentDetail,
    authViewModel: AuthViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    orderProductViewModel: OrderProductViewModel = viewModel(),
    paymentRepository: PaymentRepository = PaymentRepository()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotalPrice()
    val totalProducts = cartItems.sumOf { it.quantity }
    val currentUser by authViewModel.currentUser.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) } // Previene ejecución múltiple
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var createdOrderId by remember { mutableStateOf<Long?>(null) }
    var finalOrderTotal by remember { mutableStateOf(0.0) }
    var orderStatus by remember { mutableStateOf(1L) } // Para guardar el status de la orden
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Column(
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
            .padding(12.dp)
    ) {
        Text(
            text = "Confirmar Compra",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFF6E7DF),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2B31)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Método de Pago",
                        tint = Color(0xFFF6E7DF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF6E7DF)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = selectedPaymentDetail.payment.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF6E7DF)
                )
                selectedPaymentDetail.payment.method?.let { method ->
                    Text(
                        text = method,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                }
                if (selectedPaymentDetail.cardNumber != null) {
                    Text(
                        text = "•••• " + selectedPaymentDetail.cardNumber.takeLast(4),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Vence: ${selectedPaymentDetail.expirationDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Titular: ${selectedPaymentDetail.cardholderName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dirección de facturación:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF6E7DF),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildString {
                        append(selectedPaymentDetail.addressLine1)
                        if (!selectedPaymentDetail.addressLine2.isNullOrBlank()) {
                            append(", ${selectedPaymentDetail.addressLine2}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                )
                Text(
                    text = "${selectedPaymentDetail.city}, ${selectedPaymentDetail.stateOrProvince}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                )
                Text(
                    text = "${selectedPaymentDetail.country} ${selectedPaymentDetail.postalCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = "Productos",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF6E7DF),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(cartItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2B31)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(item.product.url_image),
                            contentDescription = item.product.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFF6E7DF)
                            )
                            Text(
                                text = "Cantidad: ${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Precio: ${CurrencyFormatter.format(item.product.price * item.quantity)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF6E7DF)
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2B31)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Resumen de la Orden",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF6E7DF)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.format(total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cantidad de productos:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$totalProducts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = Color(0xFFF6E7DF).copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total a pagar:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF6E7DF)
                    )
                    Text(
                        text = CurrencyFormatter.format(total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF6E7DF)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "La orden será procesada usando ${selectedPaymentDetail.payment.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Prevenir múltiples ejecuciones simultáneas
                if (isProcessing) {
                    Log.w("OrderConfirmation", "Already processing, ignoring click")
                    return@Button
                }
                
                coroutineScope.launch {
                    isProcessing = true
                    isLoading = true
                    try {
                        var finalStatusId = 1L // Default: pendiente

                        Log.d("OrderConfirmation", "=== STARTING PAYMENT PROCESSING ===")
                        Log.d("OrderConfirmation", "Initial finalStatusId: $finalStatusId")
                        Log.d("OrderConfirmation", "Payment Detail Object: $selectedPaymentDetail")
                        Log.d("OrderConfirmation", "Payment Name: '${selectedPaymentDetail.payment.name}'")
                        Log.d("OrderConfirmation", "Payment Method: '${selectedPaymentDetail.payment.method}'")
                        Log.d("OrderConfirmation", "Card Number: '${selectedPaymentDetail.cardNumber}'")
                        Log.d("OrderConfirmation", "Card Number Length: ${selectedPaymentDetail.cardNumber?.length ?: 0}")

                        // Verificar si es un método de pago con tarjeta (principalmente por cardNumber)
                        Log.d("OrderConfirmation", "=== CHECKING CARD PAYMENT METHOD ===")
                        val hasCardNumber = !selectedPaymentDetail.cardNumber.isNullOrBlank()
                        val isCardByMethod = isCardPaymentMethod(selectedPaymentDetail.payment.method)
                        val isCardByName = isCardPaymentMethodByName(selectedPaymentDetail.payment.name)

                        // Si tiene cardNumber, es automáticamente un pago con tarjeta
                        val isCard = hasCardNumber || isCardByMethod || isCardByName

                        Log.d("OrderConfirmation", "hasCardNumber: $hasCardNumber")
                        Log.d("OrderConfirmation", "isCardByMethod: $isCardByMethod")
                        Log.d("OrderConfirmation", "isCardByName: $isCardByName")
                        Log.d("OrderConfirmation", "final isCard: $isCard (primary validation: cardNumber exists)")

                        if (isCard) {
                            Log.d("OrderConfirmation", "=== CARD PAYMENT DETECTED ===")
                            Log.d("OrderConfirmation", "Entering card payment processing branch")

                            // Verificar si tenemos número de tarjeta
                            if (selectedPaymentDetail.cardNumber.isNullOrBlank()) {
                                Log.w("OrderConfirmation", "=== NO CARD NUMBER AVAILABLE ===")
                                Log.w("OrderConfirmation", "Card payment detected but no card number available. Using default status.")
                                finalStatusId = 1L // Mantener como pendiente si no hay número de tarjeta
                                Log.d("OrderConfirmation", "Set finalStatusId to: $finalStatusId (no card number)")
                            } else {
                                Log.d("OrderConfirmation", "=== CARD NUMBER AVAILABLE ===")
                                Log.d("OrderConfirmation", "Card number found, proceeding with test payment API")

                                // Determinar el token basado en el número de tarjeta
                                val testToken = getTestTokenFromCardNumber(selectedPaymentDetail.cardNumber)
                                Log.d("OrderConfirmation", "Generated test token: $testToken")

                                try {
                                    Log.d("OrderConfirmation", "=== CREATING TEST PAYMENT REQUEST ===")

                                    val testPaymentRequest = TestPaymentRequest(
                                        amount = (total * 100).toInt(), // Convertir a centavos
                                        currency = "usd",
                                        description = "Compra en tienda de suplementación - Orden #${System.currentTimeMillis()}",
                                        customerEmail = currentUser?.email ?: "customer@example.com",
                                        customerName = selectedPaymentDetail.cardholderName ?: "${currentUser?.username ?: "Cliente"}",
                                        testToken = testToken
                                    )

                                    Log.d("OrderConfirmation", "Test payment request created: $testPaymentRequest")
                                    Log.d("OrderConfirmation", "About to call createTestPayment API...")

                                    val paymentResponse = paymentRepository.createTestPayment(testPaymentRequest)
                                    Log.d("OrderConfirmation", "=== TEST PAYMENT API RESPONSE ===")
                                    Log.d("OrderConfirmation", "Payment response received: $paymentResponse")
                                    Log.d("OrderConfirmation", "Response status: '${paymentResponse.status}'")
                                    Log.d("OrderConfirmation", "Response message: '${paymentResponse.message}'")

                                    Log.d("OrderConfirmation", "=== MAPPING RESPONSE TO STATUS ===")
                                    val mappedStatus = mapPaymentResponseToStatus(paymentResponse, true)
                                    Log.d("OrderConfirmation", "Mapped status from response: $mappedStatus")
                                    finalStatusId = mappedStatus
                                    Log.d("OrderConfirmation", "Set finalStatusId to: $finalStatusId (from API response)")

                                } catch (paymentException: Exception) {
                                    Log.e("OrderConfirmation", "=== PAYMENT API EXCEPTION ===")
                                    Log.e("OrderConfirmation", "Payment validation failed with exception: ${paymentException.javaClass.simpleName}")
                                    Log.e("OrderConfirmation", "Exception message: '${paymentException.message}'")
                                    Log.e("OrderConfirmation", "Exception stacktrace:", paymentException)

                                    val errorBasedStatus = if (paymentException.message?.contains("insufficient funds", ignoreCase = true) == true) {
                                        Log.d("OrderConfirmation", "Exception indicates insufficient funds")
                                        3L // Sin fondos
                                    } else {
                                        Log.d("OrderConfirmation", "Exception indicates general decline")
                                        4L // Denegado
                                    }
                                    finalStatusId = errorBasedStatus
                                    Log.d("OrderConfirmation", "Set finalStatusId to: $finalStatusId (from exception)")
                                }
                            }
                        } else {
                            Log.d("OrderConfirmation", "=== NON-CARD PAYMENT METHOD ===")
                            Log.d("OrderConfirmation", "Non-card payment method detected, keeping status as pending (1)")
                            Log.d("OrderConfirmation", "finalStatusId remains: $finalStatusId")
                        }

                        Log.d("OrderConfirmation", "=== FINAL PAYMENT PROCESSING RESULT ===")
                        Log.d("OrderConfirmation", "Final status ID before order creation: $finalStatusId")

                        Log.d("OrderConfirmation", "=== CREATING ORDER ===")
                        val order = Order(
                            order_id = 0,
                            total = total,
                            date_order = "",
                            user_id = currentUser?.id ?: 0L,
                            status_id = finalStatusId,
                            total_products = totalProducts,
                            additional_info_payment_id = selectedPaymentDetail.id
                        )

                        Log.d("OrderConfirmation", "Order object created with status_id: ${order.status_id}")
                        Log.d("OrderConfirmation", "Order object: $order")

                        Log.d("OrderConfirmation", "Calling orderViewModel.createOrder...")
                        val createdOrder = orderViewModel.createOrder(order)
                        Log.d("OrderConfirmation", "=== ORDER CREATED ===")
                        Log.d("OrderConfirmation", "Created order: $createdOrder")
                        Log.d("OrderConfirmation", "Created order status_id: ${createdOrder.status_id}")

                        createdOrderId = createdOrder.order_id
                        finalOrderTotal = total
                        orderStatus = finalStatusId

                        Log.d("OrderConfirmation", "Set orderStatus to: $orderStatus")
                        Log.d("OrderConfirmation", "Set createdOrderId to: $createdOrderId")

                        // Crear OrderProducts secuencialmente para garantizar descuento de stock
                        Log.d("OrderConfirmation", "Creating ${cartItems.size} OrderProducts...")
                        for (item in cartItems) {
                            val orderProductRequest = com.example.tiendasuplementacion.model.CreateOrderProductRequest(
                                order_id = createdOrderId ?: 0L,
                                product_id = item.product.id,
                                quantity = item.quantity,
                                price = item.product.price
                            )
                            try {
                                // Esperar cada llamada para garantizar que el stock se descuente
                                val orderProduct = orderProductViewModel.createOrderProduct(orderProductRequest)
                                Log.d("OrderConfirmation", "OrderProduct created for ${item.product.name}: $orderProduct")
                            } catch (e: Exception) {
                                Log.e("OrderConfirmation", "Error creating OrderProduct for ${item.product.name}", e)
                                throw e // Propagar error para que se maneje en el catch principal
                            }
                        }

                        Log.d("OrderConfirmation", "All OrderProducts created successfully")
                        cartViewModel.clearCart()
                        showSuccess = true

                    } catch (e: Exception) {
                        Log.e("OrderConfirmation", "Error creando orden", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("No se pudo procesar la orden. Por favor intenta nuevamente.")
                        }
                    } finally {
                        isLoading = false
                        isProcessing = false // Permitir nuevos intentos solo después de completar o fallar
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF6E7DF),
                contentColor = Color(0xFF23242A)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && !isProcessing // Deshabilitar mientras se procesa
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF23242A)
                )
            } else {
                Text(
                    "Confirmar Compra",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFF6E7DF)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(Color(0xFFF6E7DF))
            )
        ) {
            Text("Cancelar")
        }
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

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                navController.navigate("products") {
                    launchSingleTop = true
                    popUpTo("cart") { inclusive = true }
                }
            },
            containerColor = Color(0xFF23242A),
            iconContentColor = Color(0xFFF6E7DF),
            titleContentColor = Color(0xFFF6E7DF),
            textContentColor = Color(0xFFF6E7DF),
            icon = {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    when (orderStatus) {
                        2L -> "¡Pago Exitoso!"
                        3L -> "Pago Rechazado - Fondos Insuficientes"
                        4L -> "Pago Denegado"
                        else -> "Orden Creada"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        when (orderStatus) {
                            2L -> "Tu pago ha sido procesado exitosamente y tu orden está confirmada."
                            3L -> "Tu pago fue rechazado por fondos insuficientes. Tu orden está pendiente."
                            4L -> "Tu pago fue denegado. Tu orden está pendiente."
                            else -> "Tu orden ha sido creada y está pendiente de procesamiento."
                        }
                    )
                    Text(
                        text = "Número de orden: ${createdOrderId}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Total: ${CurrencyFormatter.format(finalOrderTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = when (orderStatus) {
                            2L -> "Estado: Pago Confirmado"
                            3L -> "Estado: Pendiente - Sin Fondos"
                            4L -> "Estado: Pendiente - Pago Denegado"
                            else -> "Estado: Pendiente"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (orderStatus) {
                            2L -> Color(0xFF4CAF50) // Verde para éxito
                            3L, 4L -> Color(0xFFFF9800) // Naranja para pendiente
                            else -> Color(0xFFF6E7DF).copy(alpha = 0.7f)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccess = false
                        navController.navigate("products") {
                            launchSingleTop = true
                            popUpTo("cart") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFF6E7DF)
                    )
                ) {
                    Text("Continuar")
                }
            }
        )
    }
}