package com.example.tiendasuplementacion.navigation

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.tiendasuplementacion.screen.LoginScreen
import com.example.tiendasuplementacion.screen.ProductScreen
import com.example.tiendasuplementacion.screen.CartScreen
import com.example.tiendasuplementacion.screen.PaymentScreen
import com.example.tiendasuplementacion.screen.ProductFormScreen
import com.example.tiendasuplementacion.screen.SettingsScreen
import com.example.tiendasuplementacion.screen.OrderScreen
import com.example.tiendasuplementacion.screen.PaymentSelectionScreen
import com.example.tiendasuplementacion.screen.OrderConfirmationScreen
import com.example.tiendasuplementacion.screen.PSEPaymentScreen
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import androidx.compose.ui.graphics.Color

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
    val currentUser by authViewModel.currentUser.collectAsState()
    val items = if (currentUser?.role_id == 2L) {
        listOf(
            NavBarItem("products", "Productos", Icons.Default.Store),
            NavBarItem("payments", "Pagos", Icons.Default.Payment),
            NavBarItem("orders", "Pedidos", Icons.Default.List),
            NavBarItem("settings", "Configuraciones", Icons.Default.Settings)
        )
    } else {
        listOf(
            NavBarItem("products", "Productos", Icons.Default.Store),
            NavBarItem("cart", "Carrito", Icons.Default.ShoppingCart),
            NavBarItem("payments", "Pagos", Icons.Default.Payment),
            NavBarItem("orders", "Pedidos", Icons.Default.List),
            NavBarItem("settings", "Configuraciones", Icons.Default.Settings)
        )
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (isAuthenticated == true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val initial = currentUser?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
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
                                Text(text = currentUser?.username ?: "Usuario", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF6E7DF))
                                Text(text = currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF6E7DF).copy(alpha = 0.7f))
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showLogoutDialog = true
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFF6E7DF))
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
                composable("payments") { PaymentScreen(navController) }
                composable("orders") { 
                    OrderScreen(
                        navController = navController,
                        userDetailViewModel = viewModel(),
                        authViewModel = authViewModel
                    ) 
                }
                composable("paymentSelection") {
                    PaymentSelectionScreen(
                        navController = navController,
                        paymentViewModel = paymentViewModel,
                        onPaymentSelected = { payment ->
                            if (payment.method == "PSE") {
                                navController.navigate("psePayment") {
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate("orderConfirmation/${payment.id}") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                composable("psePayment") {
                    PSEPaymentScreen(
                        navController = navController,
                        cartViewModel = cartViewModel
                    )
                }
                composable(
                    route = "orderConfirmation/{paymentId}",
                    arguments = listOf(navArgument("paymentId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val paymentId = backStackEntry.arguments?.getLong("paymentId") ?: 0L
                    val payment = paymentViewModel.getPaymentById(paymentId)
                    if (payment != null) {
                        OrderConfirmationScreen(
                            navController = navController,
                            cartViewModel = cartViewModel,
                            selectedPayment = payment,
                            authViewModel = authViewModel
                        )
                    }
                }
                composable("productForm") { ProductFormScreen(navController) }
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

data class NavBarItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) 