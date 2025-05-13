package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Crear Orden",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total: \$${total}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                // Aquí iría la lógica para crear la orden
                // Por ahora solo navegamos de vuelta
                navController.navigateUp()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && cartItems.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Confirmar Orden")
            }
        }
    }
} 