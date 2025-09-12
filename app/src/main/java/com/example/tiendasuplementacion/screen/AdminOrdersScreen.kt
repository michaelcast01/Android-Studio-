package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.viewmodel.OrderViewModel
import androidx.paging.LoadState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.tiendasuplementacion.util.ExportUtils
import kotlinx.coroutines.launch
import android.util.Log
import com.example.tiendasuplementacion.component.ShimmerPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    orderViewModel: OrderViewModel = viewModel()
) {
    val orders by orderViewModel.orders.observeAsState(emptyList())
    val pagedItems = orderViewModel.pagedOrdersFlow.collectAsLazyPagingItems()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var filterStatus by remember { mutableStateOf<Long?>(null) }
    var search by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(filterStatus, search) { orderViewModel.setFilter(statusId = filterStatus, search = if (search.isBlank()) null else search) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Gestión de Pedidos", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = {
                coroutineScope.launch {
                    try {
                        val orders = pagedItems.itemSnapshotList.items.filterNotNull()
                        val file = ExportUtils.exportOrdersToFile(context, orders)
                        ExportUtils.shareFile(context, file)
                        snackbarHostState.showSnackbar("CSV exportado exitosamente")
                    } catch (e: Exception) {
                        Log.e("Export", "Error exporting CSV", e)
                        snackbarHostState.showSnackbar("Error al exportar CSV")
                    }
                }
            }) { Text("Exportar CSV") }
        }

        // filters row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Buscar id/usuario") }, modifier = Modifier.weight(1f))
            Button(onClick = { filterStatus = null }) { Text("Todos") }
            Button(onClick = { filterStatus = 1L }) { Text("Pendiente") }
            Button(onClick = { filterStatus = 2L }) { Text("Enviado") }
            Button(onClick = { filterStatus = 3L }) { Text("Entregado") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            // Header loader
            if (pagedItems.loadState.refresh is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            items(pagedItems) { order ->
                if (order == null) {
                    // Placeholder while loading
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(8.dp)
                    )
                } else {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pedido #${order.order_id}")
                                Text("Total: $${order.total}")
                                Text("Usuario: ${order.user_id}")
                            }
                            TextButton(onClick = { selectedOrder = order }) { Text("Ver / Editar") }
                        }
                    }
                }
            }
            
            // Footer loader
            if (pagedItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error states
            if (pagedItems.loadState.refresh is LoadState.Error) {
                val error = pagedItems.loadState.refresh as LoadState.Error
                item {
                    ErrorItem(
                        message = "Error al cargar pedidos",
                        onRetry = { pagedItems.retry() }
                    )
                }
            }

            // handle loading state footers via pagedItems.loadState if needed (omitted for brevity)
        }
    }

    if (selectedOrder != null) {
        OrderAdminDialog(order = selectedOrder!!, onDismiss = { selectedOrder = null }, orderViewModel = orderViewModel)
    }
    
    // SnackbarHost para feedback
    SnackbarHost(hostState = snackbarHostState)
}

@Composable
fun ErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun OrderAdminDialog(order: Order, onDismiss: () -> Unit, orderViewModel: OrderViewModel) {
    var newStatus by remember { mutableStateOf(order.status_id) }
    var tracking by remember { mutableStateOf("") }
    var refundAmount by remember { mutableStateOf(0.0) }
    var refundReason by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Pedido #${order.order_id}") }, text = {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Total: $${order.total}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Estado actual: ${order.status_id}")

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = newStatus.toString(), onValueChange = { newStatus = it.toLongOrNull() ?: newStatus }, label = { Text("Nuevo status id") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = tracking, onValueChange = { tracking = it }, label = { Text("Tracking") })

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = refundAmount.toString(), onValueChange = { refundAmount = it.toDoubleOrNull() ?: refundAmount }, label = { Text("Refund amount") })
            OutlinedTextField(value = refundReason, onValueChange = { refundReason = it }, label = { Text("Refund reason") })
        }
    }, confirmButton = {
        Column {
            TextButton(onClick = {
                orderViewModel.updateOrderStatusOptimistic(order.order_id, newStatus)
            }) { Text("Cambiar estado") }
            TextButton(onClick = {
                orderViewModel.assignTracking(order.order_id, tracking) { success, msg -> /* TODO: feedback */ }
            }) { Text("Asignar tracking") }
            TextButton(onClick = {
                orderViewModel.refundOrder(order.order_id, refundAmount, refundReason) { success, msg -> /* TODO: feedback */ }
            }) { Text("Emitir reembolso") }
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cerrar") }
    })
}
