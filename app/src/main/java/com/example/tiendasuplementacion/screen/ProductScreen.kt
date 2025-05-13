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
import com.example.tiendasuplementacion.component.GenericListScreen
import com.example.tiendasuplementacion.model.Product

@Composable
fun ProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val products by productViewModel.products.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    GenericListScreen(
        title = "Productos",
        items = products,
        onItemClick = {},
        onCreateClick = { navController.navigate("productForm") }
    ) { product ->
        ProductItem(product = product, onAddToCart = { cartViewModel.addToCart(it) })
    }
}

@Composable
fun ProductItem(product: Product, onAddToCart: (Product) -> Unit) {
    Column(modifier = Modifier.padding(8.dp)) {
        AsyncImage(
            model = product.url_image,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Precio: \$${product.price}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
        Button(
            onClick = { onAddToCart(product) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Añadir al carrito")
        }
    }
}
