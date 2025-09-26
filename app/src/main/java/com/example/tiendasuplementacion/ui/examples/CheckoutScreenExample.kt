package com.example.tiendasuplementacion.ui.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.model.CartItem
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.utils.OrderProductError
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.viewmodel.CheckoutViewModel
import com.example.tiendasuplementacion.viewmodel.OrderProductViewModel

/**
 * Ejemplo de uso del nuevo API de OrderProducts en Compose.
 * Esta pantalla muestra cómo integrar el carrito con el checkout usando el nuevo sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    userId: Long,
    cartViewModel: CartViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel(),
    orderProductViewModel: OrderProductViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartError by cartViewModel.error.collectAsState()
    val cartLoading by cartViewModel.loading.collectAsState()
    
    val checkoutLoading by checkoutViewModel.loading.collectAsState()
    val checkoutError by checkoutViewModel.error.collectAsState()
    val checkoutResult by checkoutViewModel.checkoutResult.collectAsState()
    
    val orderProductError by orderProductViewModel.error.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Mi Carrito",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mostrar errores
        cartError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        checkoutError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        orderProductError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (error) {
                        is OrderProductError.InsufficientStock -> MaterialTheme.colorScheme.errorContainer
                        is OrderProductError.NetworkError -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = error.message,
                        color = when (error) {
                            is OrderProductError.InsufficientStock -> MaterialTheme.colorScheme.onErrorContainer
                            is OrderProductError.NetworkError -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    
                    if (error.isRetryable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { cartViewModel.refreshAllProductsStock() },
                            modifier = Modifier.size(width = 120.dp, height = 36.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        // Lista del carrito
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("El carrito está vacío")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        onQuantityChange = { newQuantity ->
                            cartViewModel.updateQuantity(cartItem.product.id, newQuantity)
                        },
                        onRemove = {
                            cartViewModel.removeFromCart(cartItem.product.id)
                        },
                        onRefreshStock = {
                            cartViewModel.refreshProductStock(cartItem.product.id)
                        }
                    )
                }
            }
        }

        // Footer con total y botón de checkout
        if (cartItems.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${String.format("%.2f", cartViewModel.getTotalPrice())}",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Button(
                    onClick = { showCheckoutDialog = true },
                    enabled = !checkoutLoading && !cartLoading,
                    modifier = Modifier.height(48.dp)
                ) {
                    if (checkoutLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Finalizar Compra")
                    }
                }
            }
        }

        // Botón para refrescar stock de todos los productos
        if (cartItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { cartViewModel.refreshAllProductsStock() },
                    enabled = !cartLoading
                ) {
                    if (cartLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Actualizar Stock")
                }
            }
        }
    }

    // Dialog de confirmación de checkout
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Confirmar Compra") },
            text = { 
                Text("¿Está seguro de que desea procesar la compra por $${String.format("%.2f", cartViewModel.getTotalPrice())}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckoutDialog = false
                        checkoutViewModel.processCheckout(cartItems, userId)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar resultado del checkout
    checkoutResult?.let { result ->
        LaunchedEffect(result) {
            if (result.success) {
                // Checkout exitoso - limpiar carrito
                cartViewModel.clearCart()
                // Aquí podrías navegar a una pantalla de confirmación
            }
        }

        if (result.failedItems.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { checkoutViewModel.clearResult() },
                title = { Text("Algunos productos no pudieron procesarse") },
                text = {
                    Column {
                        Text("Los siguientes productos tuvieron problemas:")
                        Spacer(modifier = Modifier.height(8.dp))
                        result.failedItems.forEach { (item, error) ->
                            Text("• ${item.product.name}: $error")
                        }
                        
                        if (result.createdOrderProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${result.createdOrderProducts.size} productos se procesaron correctamente.")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        checkoutViewModel.clearResult()
                        // Refrescar stock después de problemas
                        cartViewModel.refreshAllProductsStock()
                    }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onRefreshStock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = cartItem.product.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Precio: $${String.format("%.2f", cartItem.product.price)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Stock disponible: ${cartItem.product.stock}",
                style = MaterialTheme.typography.bodySmall,
                color = if (cartItem.product.stock < cartItem.quantity) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Controles de cantidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            if (cartItem.quantity > 1) {
                                onQuantityChange(cartItem.quantity - 1)
                            }
                        },
                        enabled = cartItem.quantity > 1
                    ) {
                        Text("-")
                    }
                    
                    Text(
                        text = cartItem.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity + 1) },
                        enabled = cartItem.quantity < cartItem.product.stock
                    ) {
                        Text("+")
                    }
                }
                
                // Botones de acción
                Row {
                    TextButton(onClick = onRefreshStock) {
                        Text("Actualizar")
                    }
                    
                    TextButton(
                        onClick = onRemove,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
            
            // Advertencia si la cantidad excede el stock
            if (cartItem.quantity > cartItem.product.stock) {
                Text(
                    text = "⚠️ Cantidad excede el stock disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}