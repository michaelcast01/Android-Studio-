package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit, adminViewModel: AdminViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val orders by adminViewModel.orders.collectAsState()
    val selected = adminViewModel.selectedOrders
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Admin") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { adminViewModel.refresh() }) { Text("Refresh") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showConfirm = true }, enabled = selected.isNotEmpty()) { Text("Marcar Enviado") }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = orders, key = { it.order_id }) { order: Order ->
                    val isChecked = selected.contains(order.order_id)
                    ListItem(
                            headlineContent = { Text("Pedido #${order.order_id}") },
                            supportingContent = { Text("Total: ${order.total} - Estado: ${order.status_id}") },
                            trailingContent = {
                                Checkbox(checked = isChecked, onCheckedChange = { checked ->
                                    if (checked) adminViewModel.select(order.order_id) else adminViewModel.deselect(order.order_id)
                                })
                            }
                    )
                    Divider()
                }
            }
        }

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Confirmar") },
                text = { Text("Marcar ${selected.size} pedidos como enviados?") },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirm = false
                        adminViewModel.markSelectedAsShipped()
                        // show snackbar with undo
                        scope.launch {
                            val res = snackbarHostState.showSnackbar("Pedidos marcados", "DESHACER")
                            if (res == SnackbarResult.ActionPerformed) adminViewModel.undoLastBulkAction()
                        }
                    }) { Text("Confirmar") }
                },
                dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancelar") } }
            )
        }
    }
}
