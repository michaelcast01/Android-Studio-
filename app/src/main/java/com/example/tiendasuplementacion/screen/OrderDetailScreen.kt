package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.component.GenericListScreen
import com.example.tiendasuplementacion.viewmodel.OrderDetailViewModel
import com.example.tiendasuplementacion.model.OrderDetail

@Composable
fun OrderDetailScreen(navController: NavController, viewModel: OrderDetailViewModel = viewModel()) {
    val details by viewModel.details.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchOrderDetails()
    }

    GenericListScreen(
        title = "Detalle Orden",
        items = details,
        onItemClick = {},
        onCreateClick = { navController.navigate("orderDetailForm") }
    ) { detail ->
        Column(Modifier.padding(8.dp)) {
            Text("Producto: ${detail.productName}", fontWeight = FontWeight.Bold)
            Text("Cantidad: ${detail.quantity}")
            Text("Precio: ${detail.price}")
        }
    }
}

