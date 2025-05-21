package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserDetailViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage
import com.example.tiendasuplementacion.model.UserOrder
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val userDetails by userDetailViewModel.userDetails.observeAsState()
    val isLoading by userDetailViewModel.isLoading.observeAsState(false)
    val error by userDetailViewModel.error.observeAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var selectedOrder by remember { mutableStateOf<UserOrder?>(null) }
    var showOrderDetails by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            userDetailViewModel.fetchUserDetails(userId)
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
                        Color(0xFF23242A), // Fondo oscuro
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
                text = "Historial de Compras",
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    userDetails?.orders?.let { orders ->
                        if (orders.isEmpty()) {
                            item {
                                Text(
                                    text = "No tienes pedidos aún",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(orders) { order ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(10.dp),
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
                                        Text(
                                            text = "Pedido #${order.order_id}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF6E7DF)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Fecha: ${order.date_order}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Estado: ${order.status.name}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Total: $${order.total}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Productos: ${order.total_products}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Productos:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF6E7DF)
                                        )
                                        order.products.forEach { product ->
                                            Text("• ${product.name} - $${product.price}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                selectedOrder = order
                                                showOrderDetails = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Ver Detalles Completos")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showNetworkError) {
                NetworkErrorBanner(
                    message = networkErrorMessage,
                    onRetry = {
                        showNetworkError = false
                        currentUser?.id?.let { userId ->
                            userDetailViewModel.fetchUserDetails(userId)
                        }
                    },
                    onDismiss = { showNetworkError = false }
                )
            }
        }
    }

    if (showOrderDetails && selectedOrder != null) {
        AlertDialog(
            onDismissRequest = { 
                showOrderDetails = false
                selectedOrder = null
            },
            title = {
                Text(
                    text = "Detalles del Pedido #${selectedOrder?.order_id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // Información general del pedido
                    Text(
                        text = "Información General",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha: ${selectedOrder?.date_order}")
                    Text("Estado: ${selectedOrder?.status?.name}")
                    Text("Total: $${selectedOrder?.total}")
                    Text("Cantidad de productos: ${selectedOrder?.total_products}")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Información del método de pago
                    Text(
                        text = "Información de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedOrder?.additionalInfoPayment?.let { paymentInfo ->
                        if (paymentInfo.cardNumber != null) {
                            Text("Número de tarjeta: •••• ${paymentInfo.cardNumber.takeLast(4)}")
                            Text("Titular: ${paymentInfo.cardholderName}")
                            Text("Vence: ${paymentInfo.expirationDate}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dirección de facturación:")
                        Text(buildString {
                            append(paymentInfo.addressLine1)
                            if (!paymentInfo.addressLine2.isNullOrBlank()) {
                                append(", ${paymentInfo.addressLine2}")
                            }
                        })
                        Text("${paymentInfo.city}, ${paymentInfo.stateOrProvince}")
                        Text("${paymentInfo.country} ${paymentInfo.postalCode}")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de productos
                    Text(
                        text = "Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedOrder?.products?.forEach { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = product.url_image,
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$${product.price}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOrderDetails = false
                        selectedOrder = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
