package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.OrderViewModel
import com.example.tiendasuplementacion.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    val cartItems by cartViewModel.cartItems.collectAsState()
    val total = cartViewModel.getTotalPrice()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crear Orden",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total: $${total}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF6E7DF)
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (showError) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        isLoading = true
                        try {
                            // Aquí iría la lógica para crear la orden
                            navController.navigateUp()
                        } catch (e: Exception) {
                                android.util.Log.e("OrderFormScreen", "Error creando orden", e)
                                errorMessage = "No se pudo crear la orden. Intenta de nuevo."
                            showError = true
                        } finally {
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading && cartItems.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirmar Orden", color = Color.White)
                    }
                }
            }
        }
    }
} 