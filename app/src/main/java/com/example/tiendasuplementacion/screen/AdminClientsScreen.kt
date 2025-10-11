package com.example.tiendasuplementacion.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserDetailViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import com.example.tiendasuplementacion.model.UserDetail
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.tiendasuplementacion.repository.OrderProductRepository
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.UserOrder
import android.util.Log
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import kotlin.math.abs

// Enumeraciones para opciones de ordenamiento
enum class SortOption(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    RECENT_ACTIVITY("Actividad Reciente", Icons.Default.Schedule),
    ALPHABETICAL("A-Z", Icons.Default.SortByAlpha),
    MOST_ORDERS("Más Pedidos", Icons.Default.ShoppingCart),
    NEWEST_CLIENT("Más Nuevo", Icons.Default.PersonAdd)
}

// Función de utilidad para calcular estadísticas del cliente (optimizada)
@Composable
fun rememberClientStats(userDetail: UserDetail): ClientStats {
    return remember(userDetail) {
        val orders = userDetail.orders
        val totalSpent = orders.sumOf { order ->
            try { 
                val orderTotal = order.total.toDouble()
                // Si el total del pedido es válido, usarlo
                if (orderTotal > 0) orderTotal else 0.0
            } catch (e: Exception) { 0.0 }
        }
        
        ClientStats(
            totalOrders = orders.size,
            totalSpent = totalSpent,
            lastOrderDate = orders.maxByOrNull { it.date_order }?.date_order ?: "",
            statusBreakdown = orders.groupingBy { it.status.name }.eachCount()
        )
    }
}

data class ClientStats(
    val totalOrders: Int,
    val totalSpent: Double,
    val lastOrderDate: String,
    val statusBreakdown: Map<String, Int>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun AdminClientsScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel()
) {
    val userDetailsList by userDetailViewModel.userDetailsList.collectAsState(initial = emptyList())
    val isLoading by userDetailViewModel.isLoading.collectAsState(initial = false)
    val error by userDetailViewModel.error.collectAsState(initial = null)
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<UserDetail?>(null) }
    var sortBy by remember { mutableStateOf(SortOption.RECENT_ACTIVITY) }
    var showSortMenu by remember { mutableStateOf(false) }
    val orderProductRepository = remember { OrderProductRepository() }
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Cargar usuarios con role_id = 1 (clientes/usuarios)
        userDetailViewModel.fetchUserDetailsByRole(1L)
    }

    LaunchedEffect(error) {
        if (error != null && (error!!.contains("No se pudo conectar") || error!!.contains("599"))) {
            showNetworkError = true
            networkErrorMessage = error ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF23242A),
                        Color(0xFF23242A)
                    )
                )
            )
    ) {
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header mejorado con estadísticas
                AdminHeader(
                    clientCount = userDetailsList.size,
                    totalOrders = userDetailsList.sumOf { it.orders.size },
                    onRefresh = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        userDetailViewModel.fetchUserDetailsByRole(1L) 
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Controles de búsqueda y ordenamiento
                var query by remember { mutableStateOf("") }

                // Debounce idiomático usando snapshotFlow + produceState
                val debouncedQuery by produceState(initialValue = "", key1 = query) {
                    snapshotFlow { query }
                        .debounce(300L)
                        .collect { v -> value = v }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar cliente...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF26272B),
                            unfocusedContainerColor = Color(0xFF26272B),
                            focusedTextColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedPlaceholderColor = Color(0xFFF6E7DF).copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                        ),
                        singleLine = true
                    )
                    
                    // Botón de ordenamiento
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .background(
                                    Color(0xFF26272B), 
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                sortBy.icon, 
                                contentDescription = "Ordenar",
                                tint = Color(0xFFF6E7DF)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(option.icon, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(option.displayName)
                                        }
                                    },
                                    onClick = {
                                        sortBy = option
                                        showSortMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                // Filtrar y ordenar optimizado con derivedStateOf
                val processedList by remember(userDetailsList, debouncedQuery, sortBy) {
                    derivedStateOf {
                        val filtered = if (debouncedQuery.isBlank()) userDetailsList
                        else userDetailsList.filter { ud ->
                            ud.username.contains(debouncedQuery, ignoreCase = true) || 
                            ud.email.contains(debouncedQuery, ignoreCase = true) ||
                            ud.settings?.name?.contains(debouncedQuery, ignoreCase = true) == true ||
                            ud.settings?.phone?.toString()?.contains(debouncedQuery) == true
                        }
                        
                        // Aplicar ordenamiento según selección
                        when (sortBy) {
                            SortOption.RECENT_ACTIVITY -> filtered.sortedByDescending { userDetail ->
                                userDetail.orders.maxByOrNull { it.date_order }?.date_order ?: "0000-00-00"
                            }
                            SortOption.ALPHABETICAL -> filtered.sortedBy { it.username.lowercase() }
                            SortOption.MOST_ORDERS -> filtered.sortedByDescending { it.orders.size }
                            SortOption.NEWEST_CLIENT -> filtered.sortedByDescending { it.id }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = processedList,
                            key = { it.id }
                        ) { userDetail ->
                            OptimizedClientCard(
                                userDetail = userDetail,
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedUser = userDetail 
                                }
                            )
                        }
                        
                        // Indicador de fin de lista
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${processedList.size} cliente${if (processedList.size != 1) "s" else ""} encontrado${if (processedList.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    // FAB para scroll to top
                    androidx.compose.animation.AnimatedVisibility(
                        visible = listState.firstVisibleItemIndex > 5,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            containerColor = Color(0xFF26272B),
                            contentColor = Color(0xFFF6E7DF),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ir arriba")
                        }
                    }

                    // Loading overlay para evitar re-layouts grandes
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFF6E7DF))
                        }
                    }
                }
            }

        AnimatedVisibility(
            visible = showNetworkError,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    userDetailViewModel.fetchUserDetailsByRole(1L)
                },
                onDismiss = { showNetworkError = false }
            )
        }

        // Diálogo optimizado de detalles del usuario
        if (selectedUser != null) {
            AlertDialog(
                onDismissRequest = { selectedUser = null },
                title = {
                    Text(
                        text = "Detalles del Cliente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    // Usar Column con scroll para mejor manejo del espacio
                    Column(
                        modifier = Modifier
                            .heightIn(max = 600.dp) // Limitar altura máxima del diálogo
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        // Información Personal (más compacta)
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        InfoRow("Usuario", selectedUser?.username ?: "")
                        InfoRow("Email", selectedUser?.email ?: "")
                        InfoRow("Rol", selectedUser?.role?.name ?: "USER")

                        selectedUser?.settings?.let { settings ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Información de Contacto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            InfoRow("Nombre", settings.name)
                            if (settings.nickname.isNotEmpty()) {
                                InfoRow("Apodo", settings.nickname)
                            }
                            InfoRow("Teléfono", settings.phone.toString())
                            InfoRow("Ciudad", settings.city)
                            InfoRow("Dirección", settings.address)

                            if (settings.payments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Métodos de Pago",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                settings.payments.forEach { payment ->
                                    Text(
                                        text = "• ${payment.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Historial de Pedidos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        selectedUser?.orders?.let { orders ->
                            if (orders.isEmpty()) {
                                Text(
                                    text = "No hay pedidos registrados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                // Ordenar pedidos de más reciente a más antigua
                                val sortedOrders = orders.sortedByDescending { order ->
                                    try {
                                        // Intentar parsear la fecha en formato ISO o timestamp
                                        when {
                                            order.date_order.contains("T") -> {
                                                // Formato ISO: "2025-09-20T23:48:02.391555Z"
                                                order.date_order.replace("Z", "").replace("T", " ")
                                            }
                                            order.date_order.contains("-") -> {
                                                // Formato fecha: "2025-09-20"
                                                order.date_order
                                            }
                                            else -> {
                                                // Timestamp u otro formato
                                                order.date_order
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // En caso de error, usar la fecha tal como está
                                        order.date_order
                                    }
                                }
                                
                                // Mostrar pedidos ordenados de forma compacta
                                sortedOrders.forEachIndexed { index, order ->
                                    if (index > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    OptimizedOrderCard(
                                        order = order,
                                        orderProductRepository = orderProductRepository
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedUser = null }) {
                        Text("Cerrar")
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color(0xFF1A1A1A),
                textContentColor = Color(0xFF1A1A1A),
                modifier = Modifier.fillMaxWidth(0.95f) // Usar más ancho de pantalla
            )
        }
    }
}

// Componente de información compacta
@Composable
fun InfoRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
}

// Card optimizada para el diálogo (más compacta)
@Composable
fun OptimizedOrderCard(
    order: UserOrder,
    orderProductRepository: OrderProductRepository
) {
    var orderProducts by remember { mutableStateOf<List<OrderProductDetail>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(order.order_id) {
        try {
            orderProducts = orderProductRepository.getByOrderId(order.order_id)
        } catch (e: Exception) {
            Log.e("AdminClients", "Error loading products for order ${order.order_id}", e)
        } finally {
            isLoadingProducts = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header del pedido (más compacto)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${order.order_id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = order.date_order.take(10), // Solo fecha, sin hora
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Estado con color
                    Surface(
                        color = when (order.status.name.lowercase()) {
                            "pendiente" -> Color(0xFFFF9800)
                            "enviado" -> Color(0xFF2196F3)
                            "entregado" -> Color(0xFF4CAF50)
                            "denegado" -> Color(0xFFFF6B6B)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = order.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Total: $${order.total}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Expandir para mostrar productos
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    if (isLoadingProducts) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cargando...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (orderProducts.isNotEmpty()) {
                        // Header con resumen de productos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Productos comprados:",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Mostrar total calculado de productos si hay precios disponibles
                            val calculatedTotal = remember(orderProducts) {
                                orderProducts.sumOf { productDetail ->
                                    val price = when {
                                        productDetail.price > 0 -> productDetail.price
                                        productDetail.product.price > 0 -> {
                                            try { productDetail.product.price.toDouble() } catch (e: Exception) { 0.0 }
                                        }
                                        else -> 0.0
                                    }
                                    price * productDetail.quantity
                                }
                            }
                            
                            // (Removed redundant calculated total badge — total already shown above)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lista de productos
                        orderProducts.forEach { productDetail ->
                            ProductRowCompact(productDetail)
                        }
                        
                        // Nota comparativa si hay discrepancia
                        val orderTotal = try { order.total.toDouble() } catch (e: Exception) { 0.0 }
                        val calculatedTotal = orderProducts.sumOf { productDetail ->
                            val price = when {
                                productDetail.price > 0 -> productDetail.price
                                productDetail.product.price > 0 -> {
                                    try { productDetail.product.price.toDouble() } catch (e: Exception) { 0.0 }
                                }
                                else -> 0.0
                            }
                            price * productDetail.quantity
                        }
                        
                        if (orderTotal > 0 && calculatedTotal > 0 && 
                            abs(orderTotal - calculatedTotal) > 0.01) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ Nota: Total del pedido ($${String.format("%.2f", orderTotal)}) difiere del calculado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        
                    } else {
                        Text(
                            text = "No se pudieron cargar los productos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
            
            // Indicador de expansión
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isExpanded) "▲ Ocultar productos" else "▼ Ver productos (${order.total_products})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ProductRowCompact(productDetail: OrderProductDetail) {
    // Calcular precio inteligente con fallbacks
    val effectivePrice = remember(productDetail) {
        when {
            // Si el precio del detalle del pedido es válido, usarlo
            productDetail.price > 0 -> productDetail.price
            // Si no, usar el precio actual del producto
            productDetail.product.price > 0 -> productDetail.product.price.toDouble()
            // Fallback: calcular desde el precio de stock si existe
            else -> {
                try {
                    productDetail.product.price.toDouble()
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    }
    
    val subtotal = effectivePrice * productDetail.quantity
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F0F0)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = productDetail.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (effectivePrice > 0) {
                    Text(
                        text = "Precio unitario: $${String.format("%.2f", effectivePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Cantidad y subtotal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge de cantidad mejorado
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${productDetail.quantity}x",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Subtotal o mensaje de precio no disponible
                if (effectivePrice > 0) {
                    Text(
                        text = "$${String.format("%.2f", subtotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        text = "Precio N/D",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Componente AdminHeader optimizado
@Composable
fun AdminHeader(
    clientCount: Int,
    totalOrders: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26272B)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gestión de Clientes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.People,
                        label = "Clientes",
                        value = clientCount.toString()
                    )
                    StatChip(
                        icon = Icons.Default.ShoppingCart,
                        label = "Pedidos",
                        value = totalOrders.toString()
                    )
                }
            }
            
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .background(
                        Color(0xFFF6E7DF).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = Color(0xFFF6E7DF)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        color = Color(0xFFF6E7DF).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFF6E7DF),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Card de cliente optimizada con mejor performance y más información
@Composable
fun OptimizedClientCard(
    userDetail: UserDetail,
    onClick: () -> Unit
) {
    val stats = rememberClientStats(userDetail)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26272B)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con info principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userDetail.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF6E7DF),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = userDetail.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    userDetail.settings?.let { settings ->
                        if (settings.name.isNotEmpty()) {
                            Text(
                                text = settings.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF6E7DF).copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Status indicator
                val statusColor = when {
                    stats.totalOrders == 0 -> Color(0xFF757575)
                    stats.lastOrderDate.contains("2025-09") -> Color(0xFF4CAF50)
                    stats.totalOrders > 5 -> Color(0xFF2196F3)
                    else -> Color(0xFFFF9800)
                }
                
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(12.dp)
                ) {}
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClientStatItem(
                    icon = Icons.Default.ShoppingCart,
                    value = "${stats.totalOrders}",
                    label = "Pedidos"
                )
                ClientStatItem(
                    icon = Icons.Default.AttachMoney,
                    value = "$${String.format("%.0f", stats.totalSpent)}",
                    label = "Total"
                )
                if (stats.lastOrderDate.isNotEmpty()) {
                    ClientStatItem(
                        icon = Icons.Default.Schedule,
                        value = stats.lastOrderDate.take(10),
                        label = "Último"
                    )
                }
            }
            
            // Status breakdown si tiene pedidos
            if (stats.statusBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stats.statusBreakdown.entries.take(3).forEach { (status, count) ->
                        OrderStatusChip(status = status, count = count)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
        )
    }
}

@Composable
fun OrderStatusChip(status: String, count: Int) {
    val color = when (status.lowercase()) {
        "pendiente" -> Color(0xFFFF9800)
        "enviado" -> Color(0xFF2196F3)
        "entregado" -> Color(0xFF4CAF50)
        "denegado" -> Color(0xFFFF6B6B)
        else -> Color(0xFF757575)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "$count ${status.take(3)}",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Medium
        )
    }
}