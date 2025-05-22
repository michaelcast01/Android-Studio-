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
import com.example.tiendasuplementacion.model.Payment
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
import com.example.tiendasuplementacion.model.PaymentDetail
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    selectedPaymentDetail: PaymentDetail,
    authViewModel: AuthViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel(),
    orderProductViewModel: OrderProductViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotalPrice()
    val totalProducts = cartItems.sumOf { it.quantity }
    val currentUser by authViewModel.currentUser.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var createdOrderId by remember { mutableStateOf<Long?>(null) }
    var finalOrderTotal by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()

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
                                text = "Precio: $${item.product.price * item.quantity}",
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
                        text = "$${String.format("%.2f", total)}",
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
                        text = "$${String.format("%.2f", total)}",
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
                coroutineScope.launch {
                    isLoading = true
                    try {
                        // Crear la orden con el método de pago seleccionado
                        val order = Order(
                            order_id = 0, // El backend asignará el ID
                            total = total,
                            date_order = "", // El backend maneja la fecha
                            user_id = currentUser?.id ?: 0L,
                            status_id = 1L, // Estado inicial: pendiente
                            total_products = totalProducts,
                            additional_info_payment_id = selectedPaymentDetail.id
                        )
                        
                        // Crear la orden en el backend
                        val createdOrder = orderViewModel.createOrder(order)
                        createdOrderId = createdOrder.order_id
                        finalOrderTotal = total // Guardamos el total final
                        
                        // Crear los detalles de la orden para todos los productos
                        cartItems.forEach { item ->
                            val orderProduct = OrderProductDetail(
                                order_id = createdOrderId ?: 0L,
                                product_id = item.product.id,
                                quantity = item.quantity,
                                price = item.product.price
                            )
                            orderProductViewModel.createOrderProduct(orderProduct)
                        }
                        
                        // Limpiar el carrito y mostrar mensaje de éxito
                        cartViewModel.clearCart()
                        showSuccess = true
                        
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Error al crear la orden"
                        showError = true
                    } finally {
                        isLoading = false
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
            enabled = !isLoading
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
            title = { Text("¡Compra Exitosa!") },
            text = { 
                Column {
                    Text("Tu orden ha sido creada correctamente.")
                    Text(
                        text = "Número de orden: ${createdOrderId}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Total: $${String.format("%.2f", finalOrderTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
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