package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserDetailViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import com.example.tiendasuplementacion.model.UserDetail
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel()
) {
    val userDetailsList by userDetailViewModel.userDetailsList.observeAsState(emptyList())
    val isLoading by userDetailViewModel.isLoading.observeAsState(false)
    val error by userDetailViewModel.error.observeAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<UserDetail?>(null) }

    LaunchedEffect(Unit) {
        // Cargar usuarios con role_id = 2 (clientes/usuarios)
        userDetailViewModel.fetchUserDetailsByRole(2L)
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
                Text(
                    text = "Gestión de Clientes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF6E7DF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Búsqueda rápida por usuario o email
                var query by remember { mutableStateOf("") }

                // Debounce idiomático usando snapshotFlow + produceState
                val debouncedQuery by produceState(initialValue = "", key1 = query) {
                    snapshotFlow { query }
                        .debounce(300L)
                        .collect { v -> value = v }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por usuario o email...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF26272B),
                        unfocusedContainerColor = Color(0xFF26272B),
                        focusedTextColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedPlaceholderColor = Color(0xFFF6E7DF).copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                    )
                )
                // Filtrar la lista localmente para una respuesta instantánea usando derivedStateOf
                val filtered by remember(userDetailsList, debouncedQuery) {
                    derivedStateOf {
                        if (debouncedQuery.isBlank()) userDetailsList
                        else userDetailsList.filter { ud ->
                            ud.username.contains(debouncedQuery, ignoreCase = true) || ud.email.contains(debouncedQuery, ignoreCase = true)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { userDetail ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF26272B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { selectedUser = userDetail }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = userDetail.username,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFFF6E7DF),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = userDetail.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                        )
                                        userDetail.settings?.let { settings ->
                                            Text(
                                                text = "Tel: ${settings.phone}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // Contador rápido de pedidos
                                    val ordersCount = userDetail.orders.size
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(text = "Pedidos: $ordersCount", color = Color(0xFFF6E7DF))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Botones rápidos: ver historial (dialog) y editar/ir a producto
                                        Row {
                                            TextButton(onClick = { selectedUser = userDetail }) {
                                                Text("Ver historial")
                                            }
                                        }
                                    }
                                }
                            }
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

        if (showNetworkError) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    showNetworkError = false
                    userDetailViewModel.fetchUserDetailsByRole(2L)
                },
                onDismiss = { showNetworkError = false }
            )
        }

        // Diálogo de detalles del usuario
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
                        // Usar LazyColumn dentro del diálogo para historiales grandes
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Usuario: ${selectedUser?.username}")
                        Text("Email: ${selectedUser?.email}")
                        Text("Rol: ${selectedUser?.role?.name}")

                        selectedUser?.settings?.let { settings ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Información de Contacto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nombre: ${settings.name}")
                            Text("Apodo: ${settings.nickname}")
                            Text("Teléfono: ${settings.phone}")
                            Text("Ciudad: ${settings.city}")
                            Text("Dirección: ${settings.address}")

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Métodos de Pago",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            settings.payments.forEach { payment ->
                                Text("• ${payment.name}")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Historial de Pedidos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedUser?.orders?.let { orders ->
                            if (orders.isEmpty()) {
                                Text("No hay pedidos registrados")
                            } else {
                                // Lazy list para historial
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 320.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(orders, key = { it.order_id }) { order ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Pedido #${order.order_id}",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text("Fecha: ${order.date_order}")
                                                Text("Estado: ${order.status.name}")
                                                Text("Total: $${order.total}")
                                                Text("Productos: ${order.total_products}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedUser = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
} 