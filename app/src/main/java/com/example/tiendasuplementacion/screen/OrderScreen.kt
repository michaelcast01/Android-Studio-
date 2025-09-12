package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserDetailViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.tiendasuplementacion.R
import com.example.tiendasuplementacion.component.ShimmerPlaceholder
import com.example.tiendasuplementacion.model.UserOrder
import androidx.compose.ui.draw.clip
import android.content.Context
import androidx.core.content.FileProvider
import com.example.tiendasuplementacion.util.PdfGenerator
import java.io.File
import android.content.Intent
import android.widget.Toast
import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import com.example.tiendasuplementacion.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    context: Context
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val userDetail by userDetailViewModel.userDetail.observeAsState()
    val isLoading by userDetailViewModel.isLoading.observeAsState(false)
    val error by userDetailViewModel.error.observeAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var selectedOrder by remember { mutableStateOf<UserOrder?>(null) }
    var showOrderDetails by remember { mutableStateOf(false) }
    var isProcessingPdf by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            try {
                userDetailViewModel.fetchUserDetails(userId)
            } catch (e: Exception) {
                Log.e("OrderScreen", "Error fetching user details", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error al cargar el historial de compras",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
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
                        Color(0xFF23242A), // Fondo oscuro
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
                text = "Historial de Compras",
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    userDetail?.orders?.let { orders ->
                        if (orders.isEmpty()) {
                            item {
                                Text(
                                    text = "No tienes pedidos aún",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            // Mostrar el último pedido destacado primero (asumimos orders ya vienen ordenados por ViewModel)
                            val latest = orders.first()
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF3A3B40)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Último Pedido — #${latest.order_id}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF6E7DF)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Fecha: ${latest.date_order}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Estado: ${latest.status.name}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Total: ${CurrencyFormatter.format(latest.total)}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        Text("Productos: ${latest.total_products}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Productos:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF6E7DF)
                                        )
                                        latest.products.take(3).forEach { product ->
                                            Text("• ${product.name} - ${CurrencyFormatter.format(product.price)}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                        }
                                        if (latest.products.size > 3) {
                                            Text("... y ${latest.products.size - 3} más", color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                try {
                                                    selectedOrder = latest
                                                    showOrderDetails = true
                                                } catch (e: Exception) {
                                                    Log.e("OrderScreen", "Error showing order details", e)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Error al mostrar detalles del pedido",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Ver Detalles Completos")
                                        }
                                    }
                                }
                            }

                            // Luego mostrar el resto de pedidos (excluyendo el primero)
                            if (orders.size > 1) {
                                items(items = orders.drop(1), key = { it.order_id }) { order ->
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
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Pedido #${order.order_id}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF6E7DF)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Fecha: ${order.date_order}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                            Text("Estado: ${order.status.name}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                            Text("Total: ${CurrencyFormatter.format(order.total)}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                            Text("Productos: ${order.total_products}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Productos:",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF6E7DF)
                                            )
                                            order.products.take(3).forEach { product -> // Limitar a 3 productos para evitar overflow
                                                Text("• ${product.name} - ${CurrencyFormatter.format(product.price)}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                                            }
                                            if (order.products.size > 3) {
                                                Text("... y ${order.products.size - 3} más", color = Color(0xFFF6E7DF).copy(alpha = 0.6f))
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = {
                                                    try {
                                                        selectedOrder = order
                                                        showOrderDetails = true
                                                    } catch (e: Exception) {
                                                        Log.e("OrderScreen", "Error showing order details", e)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "Error al mostrar detalles del pedido",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Ver Detalles Completos")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showNetworkError) {
                NetworkErrorBanner(
                    message = networkErrorMessage,
                    onRetry = {
                        showNetworkError = false
                        currentUser?.id?.let { userId ->
                            try {
                                userDetailViewModel.fetchUserDetails(userId)
                            } catch (e: Exception) {
                                Log.e("OrderScreen", "Error retrying user details", e)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Error al reintentar",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    },
                    onDismiss = { showNetworkError = false }
                )
            }
        }
        
        // SnackbarHost para feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showOrderDetails && selectedOrder != null) {
        // Generate PDF safely when showing order details
        val pdfPath = remember(selectedOrder) {
            selectedOrder?.let { order ->
                try {
                    PdfGenerator.generateInvoicePdf(context, order)
                } catch (e: Exception) {
                    Log.e("OrderScreen", "Error generating PDF", e)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Error al generar la factura PDF",
                            duration = SnackbarDuration.Short
                        )
                    }
                    ""
                }
            } ?: ""
        }

        val pdfUri = remember(pdfPath) {
                    if (pdfPath.isNotEmpty()) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(pdfPath)
                    )
                } catch (e: Exception) {
                    Log.e("OrderScreen", "Error creating file URI", e)
                    null
                }
            } else null
        }

        AlertDialog(
            onDismissRequest = { 
                showOrderDetails = false
                selectedOrder = null
            },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Detalles del Pedido #${selectedOrder?.order_id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // Información General
                    Text(
                        text = "Información General",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha: ${selectedOrder?.date_order}", color = Color.Black)
                    Text("Estado: ${selectedOrder?.status?.name}", color = Color.Black)
                    Text("Total: ${selectedOrder?.total?.let { CurrencyFormatter.format(it) } ?: "N/A"}", color = Color.Black)
                    Text("Cantidad de productos: ${selectedOrder?.total_products}", color = Color.Black)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información de Pago
                    Text(
                        text = "Información de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedOrder?.additionalInfoPayment?.let { paymentInfo ->
                        Text("Dirección de facturación:", color = Color.Black)
                        Text(paymentInfo.addressLine1 ?: "", color = Color.Black)
                        Text("${paymentInfo.city ?: ""}, ${paymentInfo.stateOrProvince ?: ""}", color = Color.Black)
                        Text("${paymentInfo.country ?: ""} ${paymentInfo.postalCode ?: ""}", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Productos
                    Text(
                        text = "Productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedOrder?.products?.forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(product.url_image)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                val state = painter.state
                                when (state) {
                                    is AsyncImagePainter.State.Loading -> ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
                                    is AsyncImagePainter.State.Error -> {
                                        Image(
                                            painter = painterResource(R.drawable.placeholder),
                                            contentDescription = product.name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    else -> SubcomposeAsyncImageContent()
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                                Text(
                                    text = CurrencyFormatter.format(product.price),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección de Factura - Siempre visible
                    Text(
                        text = "Factura Digital",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (pdfUri != null) {
                        // PDF generado correctamente
                        Button(
                            onClick = {
                                isProcessingPdf = true
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(pdfUri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("OrderScreen", "Error opening PDF", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No se encontró una aplicación para abrir PDFs",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } finally {
                                    isProcessingPdf = false
                                }
                            },
                            enabled = !isProcessingPdf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3F51B5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abriendo...")
                            } else {
                                Text("Ver Factura PDF")
                            }
                        }
                    } else {
                        // Error en la generación del PDF o aún generando
                        OutlinedButton(
                            onClick = {
                                isProcessingPdf = true
                                coroutineScope.launch {
                                    try {
                                        selectedOrder?.let { order ->
                                            val newPdfPath = PdfGenerator.generateInvoicePdf(context, order)
                                                if (newPdfPath.isNotEmpty()) {
                                                val newPdfUri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    File(newPdfPath)
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(newPdfUri, "application/pdf")
                                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    message = "Error al generar la factura PDF",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("OrderScreen", "Error generating/opening PDF", e)
                                        snackbarHostState.showSnackbar(
                                            message = "Error al generar o abrir la factura PDF",
                                            duration = SnackbarDuration.Short
                                        )
                                    } finally {
                                        isProcessingPdf = false
                                    }
                                }
                            },
                            enabled = !isProcessingPdf,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generando PDF...")
                            } else {
                                Text("Generar y Ver Factura PDF")
                            }
                        }
                        
                        // Mensaje explicativo
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "La factura PDF se generará al hacer clic en el botón",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOrderDetails = false
                        selectedOrder = null
                    }
                ) {
                    Text("Cerrar", color = Color(0xFF3F51B5))
                }
            }
        )
    }
}
