package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tiendasuplementacion.viewmodel.ProductViewModel
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.component.GridListScreen
import com.example.tiendasuplementacion.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel
) {
    val products by productViewModel.products.observeAsState(emptyList())
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
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

    GridListScreen(
        title = "Productos",
        items = products,
        onItemClick = { /* No hacemos nada al hacer clic en el producto */ },
        onCreateClick = { navController.navigate("productForm") },
        itemContent = { product ->
            ProductItem(
                product = product,
                onAddToCart = {
                    try {
                        cartViewModel.addToCart(it)
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Error al agregar al carrito"
                        showError = true
                    }
                }
            )
        },
        cartItemCount = cartItemCount,
        onCartClick = { navController.navigate("cart") }
    )
}

@Composable
fun ProductItem(product: Product, onAddToCart: (Product) -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = product.url_image,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Precio: \$${product.price}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Stock: ${product.stock}",
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = { onAddToCart(product) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = product.stock > 0
        ) {
            Text(if (product.stock > 0) "Añadir al carrito" else "Sin stock")
        }
    }
}
