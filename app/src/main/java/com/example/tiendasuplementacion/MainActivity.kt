package com.example.tiendasuplementacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.tiendasuplementacion.navigation.AppNavGraph
import com.example.tiendasuplementacion.ui.theme.TiendaSuplementacionTheme
import com.example.tiendasuplementacion.viewmodel.CartViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cartViewModel: CartViewModel by viewModels()
        val authViewModel: AuthViewModel by viewModels()
        val paymentViewModel: PaymentViewModel by viewModels()

        authViewModel.restoreSession()

        setContent {
            TiendaSuplementacionTheme {
                val navController = rememberNavController()
                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                when (isAuthenticated) {
                    null -> {
                        // Pantalla de carga mientras se restaura la sesión
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 4.dp
                            )
                        }
                    }
                    else -> {
                        AppNavGraph(
                            navController = navController,
                            cartViewModel = cartViewModel,
                            authViewModel = authViewModel,
                            paymentViewModel = paymentViewModel,
                            startDestination = if (isAuthenticated == true) "products" else "login"
                        )
                    }
                }
            }
        }
    }
}
