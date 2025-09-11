package com.example.tiendasuplementacion.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.tiendasuplementacion.screen.*
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    paymentViewModel: PaymentViewModel,
    startDestination: String
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    // Avoid delegate type inference issues while auth model is being aligned
    val currentUser = authViewModel.currentUser.collectAsState(null).value
    // Construir items de navegación para usuario normal y para admin (admin no necesita carrito ni métodos de pago)
    val baseItemsUser = listOf(
        NavBarItem("products", "Productos", Icons.Default.Store),
        NavBarItem("cart", "Carrito", Icons.Default.ShoppingCart),
        NavBarItem("payments", "Pagos", Icons.Default.Payment),
        NavBarItem("orders", "Historial", Icons.AutoMirrored.Filled.List),
        NavBarItem("settings", "Configuraciones", Icons.Default.Settings)
    )

    val baseItemsAdmin = listOf(
        NavBarItem("products", "Productos", Icons.Default.Store)
    )

    val items = if (authViewModel.isAdmin()) {
        // Admin: base admin plus enlaces administrativos
        baseItemsAdmin + listOf(
            NavBarItem("admin_clients", "Clientes", Icons.Default.People),
            NavBarItem("admin_payments", "Admin Pagos", Icons.Default.Payment)
        )
    } else baseItemsUser

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (isAuthenticated == true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val username = currentUser?.username
                            val initial = username?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = Color(0xFF18191C),
                                modifier = Modifier.width(32.dp)
                            ) {
                                Text(
                                    text = initial,
                                    color = Color(0xFFF6E7DF),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = username ?: "Usuario", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF6E7DF))
                                Text(text = currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.7f))
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showLogoutDialog = true
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFF6E7DF))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF18191C)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF18191C)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label, tint = Color(0xFFF6E7DF)) },
                            label = { Text(item.label, color = Color(0xFFF6E7DF)) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirmar cierre de sesión") },
                    text = { Text("¿Realmente quieres cerrar la sesión?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            authViewModel.logout()
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
            NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
                composable("products") { ProductScreen(navController, cartViewModel = cartViewModel, authViewModel = authViewModel) }
                composable("cart") { CartScreen(navController, cartViewModel) }
                composable("payments") { PaymentScreen(navController, authViewModel = authViewModel) }
                composable("orders") {
                    val ctx = LocalContext.current
                    OrderScreen(navController = navController, context = ctx)
                }
                composable("admin_clients") {
                    AdminClientsScreen(navController)
                }
                composable("admin_payments") {
                    AdminPaymentsScreen(navController)
                }
                composable("paymentSelection") {
                    // Use defaults inside PaymentSelectionScreen; handle selection within that screen flow
                    PaymentSelectionScreen(
                        navController = navController,
                        paymentViewModel = paymentViewModel,
                        authViewModel = authViewModel,
                        onPaymentSelected = { paymentDetail ->
                            // Cache the selected payment detail to avoid an immediate re-fetch
                            paymentViewModel.setSelectedPaymentDetail(paymentDetail)
                            // Navigate to order confirmation, passing the payment detail id
                            navController.navigate("orderConfirmation/${paymentDetail.id}") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = "orderConfirmation/{paymentId}",
                    arguments = listOf(navArgument("paymentId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val paymentId = backStackEntry.arguments?.getLong("paymentId") ?: 0L
                    OrderConfirmationHost(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        paymentViewModel = paymentViewModel,
                        authViewModel = authViewModel,
                        paymentId = paymentId
                    )
                }
                composable("productForm") { ProductFormScreen(navController) }
                composable("payment_config") { PaymentConfigScreen(navController) }
                composable("settings") { SettingsScreen(navController, authViewModel = authViewModel) }
                composable(
                    route = "editProduct/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                    ProductFormScreen(navController, productId = productId)
                }
            }
        }
    } else {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("login") { LoginScreen(navController, authViewModel) }
        }
    }
}

data class NavBarItem(val route: String, val label: String, val icon: ImageVector) 

@Composable
fun OrderConfirmationHost(
    navController: NavHostController,
    cartViewModel: com.example.tiendasuplementacion.viewmodel.CartViewModel,
    paymentViewModel: com.example.tiendasuplementacion.viewmodel.PaymentViewModel,
    authViewModel: com.example.tiendasuplementacion.viewmodel.AuthViewModel,
    paymentId: Long
) {
    val paymentDetails by paymentViewModel.paymentDetails.observeAsState(emptyList())
    val cachedSelected by paymentViewModel.selectedPaymentDetail.observeAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    // Prefer the in-memory cached selected payment detail (set when the user tapped it);
    // fall back to searching the loaded list and then to fetching from the repository.
    val selected = cachedSelected ?: paymentDetails.find { it.id == paymentId }

    if (selected != null) {
        // Clear cache after consuming to keep memory consistent
        paymentViewModel.clearSelectedPaymentDetail()
        OrderConfirmationScreen(navController = navController, cartViewModel = cartViewModel, selectedPaymentDetail = selected)
    } else {
        // Trigger a load and show spinner while waiting
        LaunchedEffect(paymentId) {
            currentUser?.id?.let { paymentViewModel.fetchPaymentDetails(it) }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFF6E7DF))
        }
    }
}