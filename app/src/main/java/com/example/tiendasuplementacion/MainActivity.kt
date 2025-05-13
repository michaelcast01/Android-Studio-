package com.example.tiendasuplementacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.tiendasuplementacion.navigation.AppNavGraph
import com.example.tiendasuplementacion.ui.theme.TiendaSuplementacionTheme
import com.example.tiendasuplementacion.viewmodel.CartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cartViewModel: CartViewModel by viewModels()

        setContent {
            TiendaSuplementacionTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        padding = innerPadding,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}
