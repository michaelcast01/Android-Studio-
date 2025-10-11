package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.OrderDetailViewModel
import com.example.tiendasuplementacion.util.QRCodeGenerator
import com.google.gson.Gson
import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import com.example.tiendasuplementacion.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(navController: NavController, viewModel: OrderDetailViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val details by viewModel.details.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)
    var isGeneratingQR by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            viewModel.fetchOrderDetails()
        } catch (e: Exception) {
            Log.e("OrderDetailScreen", "Error fetching order details", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error al cargar los detalles del pedido",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
    
    LaunchedEffect(error) {
        error?.let { errorMsg ->
            Log.e("OrderDetailScreen", "Error received: $errorMsg")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error: $errorMsg",
                    duration = SnackbarDuration.Short
                )
            }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Detalle de la Orden",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF6E7DF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFF6E7DF)
                    )
                }
            } else if (details.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay detalles disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                    )
                }
            } else {
                details.forEach { detail ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF26272B)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Producto: ${detail.productName}", 
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF6E7DF)
                            )
                            Text(
                                "Cantidad: ${detail.quantity}",
                                color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                            )
                            Text(
                                "Precio: ${CurrencyFormatter.format(detail.price)}",
                                color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Generate QR code with order details safely
                            val orderJson = remember(detail) {
                                try {
                                    Gson().toJson(detail)
                                } catch (e: Exception) {
                                    Log.e("OrderDetailScreen", "Error serializing order detail", e)
                                    "Error al generar código QR"
                                }
                            }
                            
                            val qrBitmap = remember(orderJson) {
                                try {
                                    isGeneratingQR = true
                                    val bitmap = QRCodeGenerator.generateQRCode(orderJson)
                                    isGeneratingQR = false
                                    bitmap
                                } catch (e: Exception) {
                                    Log.e("OrderDetailScreen", "Error generating QR code", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Error al generar el código QR",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    isGeneratingQR = false
                                    QRCodeGenerator.generateQRCode("Error")
                                }
                            }
                            
                            if (isGeneratingQR) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .align(Alignment.CenterHorizontally),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFF6E7DF)
                                    )
                                }
                            } else {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR Code de la orden",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // SnackbarHost para feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

