package com.example.tiendasuplementacion.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.viewmodel.CartViewModel

@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onCheckout: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carrito de Compras", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (cartItems.isEmpty()) {
            Text("Tu carrito está vacío.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                                Text("Cantidad: ${item.quantity}")
                                Text("Precio: \$${item.product.price * item.quantity}")
                            }
                            Button(onClick = {
                                viewModel.removeFromCart(item.product.id)
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total: \$${viewModel.getTotalPrice()}", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    onCheckout()
                    viewModel.clearCart()
                },
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text("Finalizar compra")
            }
        }
    }
}
