package com.example.tiendasuplementacion.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tiendasuplementacion.screen.*
import com.example.tiendasuplementacion.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    padding: PaddingValues = PaddingValues(),
    cartViewModel: CartViewModel
) {
    val items = listOf(
        Screen.Products,
        Screen.Categories,
        Screen.Cart,
        Screen.Orders,
        Screen.Payments
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        BottomNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            cartViewModel = cartViewModel
        )
    }
}

@Composable
fun BottomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("userForm") {
            UserFormScreen(navController)
        }
        composable("users") {
            UserScreen(navController)
        }
        composable(Screen.Products.route) {
            ProductScreen(navController, cartViewModel = cartViewModel)
        }
        composable("productForm") {
            ProductFormScreen(navController)
        }
        composable(Screen.Categories.route) {
            CategoryScreen(navController)
        }
        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                onCheckout = {
                    navController.navigate(Screen.Orders.route)
                }
            )
        }
        composable(Screen.Orders.route) {
            OrderScreen(navController)
        }
        composable("orderForm") {
            OrderFormScreen(navController, cartViewModel = cartViewModel)
        }
        composable(Screen.Payments.route) {
            PaymentScreen(navController)
        }
        composable("orderDetails") {
            OrderDetailScreen(navController)
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Products : Screen("products", "Productos", Icons.Default.ShoppingCart)
    object Categories : Screen("categories", "Categorías", Icons.Default.Category)
    object Cart : Screen("cart", "Carrito", Icons.Default.ShoppingBasket)
    object Orders : Screen("orders", "Órdenes", Icons.AutoMirrored.Filled.List)
    object Payments : Screen("payments", "Pagos", Icons.Default.Payment)
} 