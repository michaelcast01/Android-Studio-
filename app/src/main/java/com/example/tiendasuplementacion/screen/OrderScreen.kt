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
import com.example.tiendasuplementacion.viewmodel.OrderViewModel

@Composable
fun OrderScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel()
) {
    val orders by viewModel.orders.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    GenericListScreen(
        title = "Órdenes",
        items = orders,
        onItemClick = {},
        onCreateClick = { navController.navigate("orderForm") }
    ) { order ->
        Column(Modifier.padding(8.dp)) {
            Text("ID: ${order.order_id}", fontWeight = FontWeight.Bold)
            Text("Total: ${order.total}")
            Text("Fecha: ${order.date_order}")
            Text("Usuario ID: ${order.user_id}")
        }
    }
}
