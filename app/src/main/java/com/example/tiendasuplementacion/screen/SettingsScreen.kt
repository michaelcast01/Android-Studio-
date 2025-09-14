package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingViewModel: SettingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val settingDetail by settingViewModel.settingDetail.observeAsState()
    val availablePayments by settingViewModel.availablePayments.observeAsState()
    val error by settingViewModel.error.observeAsState()    
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var showPaymentMethods by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var isAddingPayment by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var addedPaymentName by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()

    // Cargar configuraciones al inicializar
    LaunchedEffect(currentUser?.setting_id) {
        currentUser?.setting_id?.let { settingId ->
            settingViewModel.fetchSettingDetails(settingId)
        }
    }

    LaunchedEffect(Unit) {
        settingViewModel.fetchAvailablePaymentMethods()
    }

    // Manejar errores generales
    LaunchedEffect(error) {
        error?.let {
            showNetworkError = true
            networkErrorMessage = it
            isAddingPayment = false
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
                text = "Configuración",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF6E7DF),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.padding(16.dp))

            // Solo mostrar los botones de métodos de pago si NO es un administrador
            if (currentUser?.role_id != 2L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showPaymentMethods = !showPaymentMethods },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("Ver Métodos de Pago")
                    }

                    Button(
                        onClick = { showAddPaymentDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Agregar Método")
                    }
                }

                if (showPaymentMethods) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF26272B)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Métodos de Pago Disponibles",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFF6E7DF)
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            settingDetail?.payments?.forEach { payment ->
                                Text(
                                    "• ${payment.name}",
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                                )
                            } ?: Text(
                                "No hay métodos de pago configurados",
                                color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))

            // Información personal
            settingDetail?.let { detail ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF26272B)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFF6E7DF)
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("Nombre: ${detail.name}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                        Text(
                            "Apodo: ${detail.nickname}",
                            color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                        )
                        Text(
                            "Teléfono: ${detail.phone}",
                            color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                        )
                        Text("Ciudad: ${detail.city}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                        Text(
                            "Dirección: ${detail.address}",
                            color = Color(0xFFF6E7DF).copy(alpha = 0.8f)
                        )
                    }
                }
            } ?: run {
                Text("No se encontraron configuraciones", color = Color(0xFFF6E7DF))
            }

            // Dialog para agregar método de pago
            if (showAddPaymentDialog) {
                AlertDialog(
                    onDismissRequest = { showAddPaymentDialog = false },
                    title = { Text("Agregar Método de Pago") },
                    text = {
                        Column {
                            Text("Selecciona un método de pago para agregar:")
                            Spacer(modifier = Modifier.padding(8.dp))

                            val currentPaymentIds =
                                settingDetail?.payments?.map { it.id } ?: emptyList()
                            val availableToAdd =
                                availablePayments?.filter { it.id !in currentPaymentIds }
                                    ?: emptyList()

                            if (availableToAdd.isEmpty()) {
                                Text("No hay métodos de pago disponibles para agregar")
                            } else {
                                availableToAdd.forEach { payment ->
                                    Button(
                                        onClick = {
                                            isAddingPayment = true
                                            addedPaymentName = payment.name
                                            settingViewModel.addPaymentMethod(payment.id)
                                            showSuccessMessage = true
                                            showAddPaymentDialog = false
                                        },
                                        enabled = !isAddingPayment,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        if (isAddingPayment) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Text(payment.name)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAddPaymentDialog = false }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Dialog de éxito al agregar método de pago
            if (showSuccessMessage) {
                AlertDialog(
                    onDismissRequest = { showSuccessMessage = false },
                    title = { Text("Éxito") },
                    text = { Text("Se ha agregado el método de pago '$addedPaymentName' correctamente.") },
                    confirmButton = {
                        TextButton(onClick = { showSuccessMessage = false }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            // Banner de error de red
            if (showNetworkError) {
                NetworkErrorBanner(
                    message = networkErrorMessage,
                    onRetry = {
                        showNetworkError = false
                        currentUser?.setting_id?.let { settingId ->
                            settingViewModel.fetchSettingDetails(settingId)
                        }
                    },
                    onDismiss = { showNetworkError = false }
                )
            }
        }
    }
}