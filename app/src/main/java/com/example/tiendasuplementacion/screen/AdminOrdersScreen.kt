package com.example.tiendasuplementacion.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.tiendasuplementacion.component.ShimmerPlaceholder
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.repository.OrderProductRepository
import com.example.tiendasuplementacion.util.CurrencyFormatter
import com.example.tiendasuplementacion.util.ExportUtils
import com.example.tiendasuplementacion.viewmodel.OrderViewModel
import kotlinx.coroutines.launch

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
    var showQuickStatusDialog by remember { mutableStateOf<Order?>(null) }
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val orderProductRepository = remember { OrderProductRepository() }

    LaunchedEffect(filterStatus, search) { 
        orderViewModel.setFilter(statusId = filterStatus, search = if (search.isBlank()) null else search) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF23242A))
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.SpaceBetween, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Gestión de Pedidos",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = {
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
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF3F51B5)
                )
            ) { 
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exportar CSV") 
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtros mejorados
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2E2F36)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Buscar por ID, Usuario, Nombre o Email", color = Color(0xFFB0B0B0)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB0B0B0))
                    },
                    placeholder = { Text("Ej: Juan Pérez, admin@ejemplo.com", color = Color(0xFF808080)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color(0xFFB0B0B0)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filtros de estado mejorados
                Text(
                    text = "Filtrar por Estado:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusFilterChip(
                        text = "Todos",
                        count = pagedItems.itemSnapshotList.items.size,
                        isSelected = filterStatus == null,
                        onClick = { filterStatus = null },
                        color = Color(0xFF757575),
                        modifier = Modifier.weight(1f)
                    )
                    StatusFilterChip(
                        text = "Pendiente",
                        count = pagedItems.itemSnapshotList.items.count { it?.status_id == 1L },
                        isSelected = filterStatus == 1L,
                        onClick = { filterStatus = 1L },
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    StatusFilterChip(
                        text = "Enviado",
                        count = pagedItems.itemSnapshotList.items.count { it?.status_id == 2L },
                        isSelected = filterStatus == 2L,
                        onClick = { filterStatus = 2L },
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    StatusFilterChip(
                        text = "Entregado",
                        count = pagedItems.itemSnapshotList.items.count { it?.status_id == 3L },
                        isSelected = filterStatus == 3L,
                        onClick = { filterStatus = 3L },
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de pedidos optimizada
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Loading inicial
            if (pagedItems.loadState.refresh is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3F51B5))
                    }
                }
            }
            
            items(
                count = pagedItems.itemCount,
                key = pagedItems.itemKey { it.order_id },
                contentType = pagedItems.itemContentType { "order" }
            ) { index ->
                val order = pagedItems[index]
                if (order == null) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    OptimizedOrderCard(
                        order = order,
                        onClick = { selectedOrder = order },
                        onExpandToggle = { 
                            expandedOrderId = if (expandedOrderId == order.order_id) null else order.order_id 
                        },
                        onQuickStatusChange = { showQuickStatusDialog = order },
                        isExpanded = expandedOrderId == order.order_id,
                        orderProductRepository = orderProductRepository
                    )
                }
            }
            
            // Loading al final
            if (pagedItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3F51B5))
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
        }
    }

    // Dialog mejorado
    if (selectedOrder != null) {
        OptimizedOrderDialog(
            order = selectedOrder!!,
            onDismiss = { selectedOrder = null },
            orderViewModel = orderViewModel,
            orderProductRepository = orderProductRepository
        )
    }
    
    // Dialog para cambio rápido de estado
    if (showQuickStatusDialog != null) {
        QuickStatusChangeDialog(
            order = showQuickStatusDialog!!,
            onDismiss = { showQuickStatusDialog = null },
            onStatusChange = { newStatus ->
                orderViewModel.updateOrderStatusOptimistic(showQuickStatusDialog!!.order_id, newStatus)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Estado actualizado a ${getStatusText(newStatus)}")
                }
                showQuickStatusDialog = null
            }
        )
    }
    
    // SnackbarHost
    SnackbarHost(hostState = snackbarHostState)
}

@Composable
fun OptimizedOrderCard(
    order: Order,
    onClick: () -> Unit,
    onExpandToggle: () -> Unit,
    onQuickStatusChange: () -> Unit,
    isExpanded: Boolean,
    orderProductRepository: OrderProductRepository
) {
    var orderProducts by remember { mutableStateOf<List<OrderProductDetail>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(true) }

    LaunchedEffect(order.order_id) {
        try {
            orderProducts = orderProductRepository.getByOrderId(order.order_id)
        } catch (e: Exception) {
            Log.e("AdminOrders", "Error loading products for order ${order.order_id}", e)
        } finally {
            isLoadingProducts = false
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E2F36)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${order.order_id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Usuario: ${order.user_id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0B0B0)
                    )
                    Text(
                        text = order.date_order,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0B0B0)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = CurrencyFormatter.format(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusChip(
                            statusId = order.status_id,
                            isClickable = true,
                            onClick = onQuickStatusChange
                        )
                        IconButton(
                            onClick = onExpandToggle,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                tint = Color(0xFF3F51B5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Productos del pedido - Vista previa o expandida
            if (isLoadingProducts) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cargando productos...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0B0B0)
                    )
                }
            } else if (orderProducts.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Productos (${orderProducts.size}):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        
                        Text(
                            text = if (isExpanded) "Ver menos" else "Ver todos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Mostrar productos según si está expandido o no
                    val productsToShow = if (isExpanded) orderProducts else orderProducts.take(2)
                    
                    productsToShow.forEach { productDetail ->
                        if (isExpanded) {
                            ExpandedProductRow(productDetail = productDetail)
                        } else {
                            ProductRow(productDetail = productDetail)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (!isExpanded && orderProducts.size > 2) {
                        Text(
                            text = "... y ${orderProducts.size - 2} productos más",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Sin productos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF6B6B)
                )
            }

            // Footer con acciones
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de cambio rápido de estado
                OutlinedButton(
                    onClick = onQuickStatusChange,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800)
                    )
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Estado")
                }
                
                // Botón de administración completa
                OutlinedButton(
                    onClick = onClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF3F51B5)
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Administrar")
                }
            }
        }
    }
}

@Composable
fun ProductRow(productDetail: OrderProductDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = productDetail.product.url_image,
            contentDescription = productDetail.product.name,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = productDetail.product.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Cantidad destacada
        Surface(
            color = Color(0xFF3F51B5),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "${productDetail.quantity}x",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = CurrencyFormatter.format(productDetail.price * productDetail.quantity),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusChip(
    statusId: Long,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val (text, color) = when (statusId) {
        1L -> "Pendiente" to Color(0xFFFF9800)
        2L -> "Enviado" to Color(0xFF2196F3)
        3L -> "Entregado" to Color(0xFF4CAF50)
        else -> "Desconocido" to Color(0xFF757575)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = if (isClickable && onClick != null) {
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
        } else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
            if (isClickable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar estado",
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun OptimizedOrderDialog(
    order: Order,
    onDismiss: () -> Unit,
    orderViewModel: OrderViewModel,
    orderProductRepository: OrderProductRepository
) {
    var orderProducts by remember { mutableStateOf<List<OrderProductDetail>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(true) }
    var newStatus by remember { mutableStateOf(order.status_id) }
    var tracking by remember { mutableStateOf("") }

    LaunchedEffect(order.order_id) {
        try {
            orderProducts = orderProductRepository.getByOrderId(order.order_id)
        } catch (e: Exception) {
            Log.e("AdminOrders", "Error loading products for order ${order.order_id}", e)
        } finally {
            isLoadingProducts = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pedido #${order.order_id}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                item {
                    // Información del pedido
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", fontWeight = FontWeight.Medium)
                                Text(
                                    CurrencyFormatter.format(order.total),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Usuario:", fontWeight = FontWeight.Medium)
                                Text("${order.user_id}")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fecha:", fontWeight = FontWeight.Medium)
                                Text(order.date_order)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estado:", fontWeight = FontWeight.Medium)
                                StatusChip(statusId = order.status_id)
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Productos del Pedido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (isLoadingProducts) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(orderProducts) { productDetail ->
                        DetailedProductRow(productDetail = productDetail)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Administración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newStatus.toString(),
                        onValueChange = { newStatus = it.toLongOrNull() ?: newStatus },
                        label = { Text("Nuevo estado ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tracking,
                        onValueChange = { tracking = it },
                        label = { Text("Número de seguimiento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        orderViewModel.updateOrderStatusOptimistic(order.order_id, newStatus)
                        onDismiss()
                    }
                ) {
                    Text("Actualizar Estado")
                }
                
                if (tracking.isNotBlank()) {
                    Button(
                        onClick = {
                            orderViewModel.assignTracking(order.order_id, tracking) { success, msg -> }
                            onDismiss()
                        }
                    ) {
                        Text("Asignar Tracking")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun DetailedProductRow(productDetail: OrderProductDetail) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = productDetail.product.url_image,
                contentDescription = productDetail.product.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productDetail.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = productDetail.product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
                Text(
                    text = "Precio unitario: ${CurrencyFormatter.format(productDetail.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Cantidad destacada
                Surface(
                    color = Color(0xFF3F51B5),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cantidad: ${productDetail.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Subtotal: ${CurrencyFormatter.format(productDetail.price * productDetail.quantity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
            color = Color(0xFFFF6B6B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3F51B5)
            )
        ) {
            Text("Reintentar")
        }
    }
}

// Nueva función para vista expandida de productos
@Composable
fun ExpandedProductRow(productDetail: OrderProductDetail) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF393A42)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = productDetail.product.url_image,
                contentDescription = productDetail.product.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productDetail.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = productDetail.product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B0B0),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Precio: ${CurrencyFormatter.format(productDetail.price)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF90A4AE)
                    )
                    Text(
                        text = "Stock: ${productDetail.product.stock}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (productDetail.product.stock > 0) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Cantidad destacada
                Surface(
                    color = Color(0xFF3F51B5),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${productDetail.quantity}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = CurrencyFormatter.format(productDetail.price * productDetail.quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Filtro de estado mejorado con contador
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterChip(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else color
                )
            }
        },
        selected = isSelected,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.3f),
            selectedLabelColor = color,
            labelColor = Color(0xFFB0B0B0)
        )
    )
}

// Dialog para cambio rápido de estado
@Composable
fun QuickStatusChangeDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusChange: (Long) -> Unit
) {
    val statusOptions = listOf(
        Triple(1L, "Pendiente", Color(0xFFFF9800)),
        Triple(2L, "Enviado", Color(0xFF2196F3)),
        Triple(3L, "Entregado", Color(0xFF4CAF50))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cambiar Estado - Pedido #${order.order_id}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Estado actual: ${getStatusText(order.status_id)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Seleccionar nuevo estado:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                statusOptions.forEach { triple ->
                    val (statusId, statusText, statusColor) = triple
                    Card(
                        onClick = { onStatusChange(statusId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (order.status_id == statusId) 
                                statusColor.copy(alpha = 0.2f) 
                            else 
                                Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = statusColor,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (order.status_id == statusId) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            if (order.status_id == statusId) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Estado actual",
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Función auxiliar para obtener texto del estado
fun getStatusText(statusId: Long): String {
    return when (statusId) {
        1L -> "Pendiente"
        2L -> "Enviado"
        3L -> "Entregado"
        else -> "Desconocido"
    }
}
