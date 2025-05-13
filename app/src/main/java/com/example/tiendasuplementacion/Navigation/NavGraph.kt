package com.example.tiendasuplementacion.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tiendasuplementacion.screen.*

@Composable
fun AppNavGraph(navController: NavHostController, padding: PaddingValues = PaddingValues()) {
    NavHost(
        navController = navController,
        startDestination = "login", // Primera pantalla
        modifier = Modifier.padding(padding)
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
        composable("products") {
            ProductScreen(navController)
        }
        composable("categories") {
            CategoryScreen(navController)
        }
        composable("orders") {
            OrderScreen(navController)
        }
        composable("orderDetails") {
            OrderDetailScreen(navController)
        }
        composable("payments") {
            PaymentScreen(navController)
        }
        composable("statuses") {
            StatusScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("cart") {
            CartScreen(onCheckout = {
                navController.navigate("orders") // o donde quieras redirigir
            })
        }

    }

}
