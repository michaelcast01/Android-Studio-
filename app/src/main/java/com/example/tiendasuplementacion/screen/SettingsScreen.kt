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
import android.util.Log
import com.example.tiendasuplementacion.util.EnvConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning

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
    
    // Estados para validación de email
    var isEmailValidated by remember { mutableStateOf(false) }
    var isEmailVerifying by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var emailVerificationStatus by remember { mutableStateOf<String?>(null) }
    var showVerificationResult by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUser?.setting_id) {
        currentUser?.setting_id?.let { settingId ->
            settingViewModel.fetchSettingDetails(settingId)
        }
    }

    LaunchedEffect(Unit) {
        settingViewModel.fetchAvailablePaymentMethods()
    }

    LaunchedEffect(error) {
        error?.let {
            showNetworkError = true
            networkErrorMessage = it
            isAddingPayment = false
        }
    }

    // Función para verificar el estado periódicamente
    fun pollVerificationStatus(verificationId: String) {
        coroutineScope.launch {
            var attempts = 0
            val maxAttempts = 10
            
            while (attempts < maxAttempts && isEmailVerifying) {
                delay(3000) // Esperar 3 segundos entre verificaciones
                
                try {
                    val status = settingViewModel.checkVerificationStatus(verificationId)
                    
                    when (status.status) {
                        "COMPLETED" -> {
                            isEmailVerifying = false
                            emailVerificationStatus = status.emailStatus
                            
                            when (status.emailStatus) {
                                "VALID" -> {
                                    isEmailValidated = true
                                    verificationMessage = "✅ Email verificado correctamente"
                                }
                                "INVALID" -> {
                                    verificationMessage = "❌ Email no válido"
                                }
                                "UNKNOWN" -> {
                                    verificationMessage = "⚠️ Estado del email incierto"
                                }
                            }
                            showVerificationResult = true
                            break
                        }
                        "FAILED" -> {
                            isEmailVerifying = false
                            verificationMessage = "❌ Error en la verificación"
                            showVerificationResult = true
                            break
                        }
                        "PENDING" -> {
                            // Continuar esperando
                            Log.d("SettingsScreen", "Verificación aún pendiente...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "Error al verificar estado", e)
                }
                
                attempts++
            }
            
            if (attempts >= maxAttempts && isEmailVerifying) {
                isEmailVerifying = false
                verificationMessage = "⏱️ Tiempo de verificación agotado"
                showVerificationResult = true
            }
        }
    }

    // Función para iniciar verificación de email
    fun startEmailVerification(email: String) {
        coroutineScope.launch {
            isEmailVerifying = true
            try {
                // Llamada a la API de verificación
                val response = settingViewModel.startEmailVerification(
                    email = email,
                    callbackUrl = "https://your-app-callback.com/email-verification/callback"
                )
                
                verificationId = response.verificationId
                Log.d("SettingsScreen", "Verificación iniciada con ID: ${response.verificationId}")
                
                // Mostrar mensaje de que se envió la verificación
                verificationMessage = "Verificación enviada. Revisando estado..."
                showVerificationResult = true
                
                // Polling para verificar el estado (simular callback)
                pollVerificationStatus(response.verificationId)
                
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Error al iniciar verificación", e)
                isEmailVerifying = false
                verificationMessage = "Error al enviar verificación: ${e.message}"
                showVerificationResult = true
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
                                Text("• ${payment.name}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                            } ?: Text("No hay métodos de pago configurados", color = Color(0xFFF6E7DF).copy(alpha = 0.7f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.padding(16.dp))
            
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
                        Text("Apodo: ${detail.nickname}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                        Text("Teléfono: ${detail.phone}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                        Text("Ciudad: ${detail.city}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                        Text("Dirección: ${detail.address}", color = Color(0xFFF6E7DF).copy(alpha = 0.8f))
                    }
                }
            } ?: run {
                Text("No se encontraron configuraciones")
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Mostrar email con checkbox de validación y botón
            currentUser?.email?.let { email ->
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color(0xFFF6E7DF).copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = email,
                                color = Color(0xFFF6E7DF),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Mostrar estado de verificación
                            if (isEmailVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF4CAF50),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verificando...",
                                    color = Color(0xFFF6E7DF).copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                when (emailVerificationStatus) {
                                    "VALID" -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verificado",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    "INVALID" -> {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "No válido",
                                            tint = Color(0xFFFF5722),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    "UNKNOWN" -> {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Desconocido",
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "No verificado",
                                            color = Color(0xFFF6E7DF).copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                if (!isEmailVerifying) {
                                    startEmailVerification(email)
                                }
                            },
                            enabled = !isEmailVerifying,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isEmailVerifying) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verificando...")
                                }
                            } else {
                                Text(
                                    when (emailVerificationStatus) {
                                        "VALID" -> "✅ Verificado - Enviar nuevamente"
                                        "INVALID" -> "❌ Inválido - Verificar nuevamente"
                                        "UNKNOWN" -> "⚠️ Incierto - Verificar nuevamente"
                                        else -> "Enviar Verificación a $email"
                                    }
                                )
                            }
                        }
                    }
                } ?: run {
                    Button(
                        onClick = {
                            val emailApiKey = EnvConfig.get("EMAIL_API_KEY", "No encontrada")
                      
                        
                            Log.d("SettingsScreen", "EMAIL_API_KEY: $emailApiKey")
                            
                            // Mostrar todas las variables de entorno disponibles
                            EnvConfig.getAllProperties().forEach { (key, value) ->
                                // Enmascarar valores sensibles para seguridad
                                val maskedValue = if (key.contains("PASSWORD") || 
                                                        key.contains("TOKEN") || 
                                                        key.contains("KEY")) {
                                    "*".repeat(value.length.coerceAtMost(8))
                            } else {
                                value
                            }
                            Log.d("SettingsScreen", "$key: $maskedValue")
                        }
                        
                        Log.d("SettingsScreen", "===============================================")
                        Log.d("SettingsScreen", "¡Botón de validación de correo presionado!")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Validación Correo")
                }
            }
        }

        if (showAddPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showAddPaymentDialog = false },
                title = { Text("Agregar Método de Pago") },
                text = {
                    Column {
                        Text("Selecciona un método de pago para agregar:")
                        Spacer(modifier = Modifier.padding(8.dp))
                        
                        // Filtrar los métodos de pago que no están en la configuración actual
                        val currentPaymentIds = settingDetail?.payments?.map { it.id } ?: emptyList()
                        val availableToAdd = availablePayments?.filter { it.id !in currentPaymentIds } ?: emptyList()
                        
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

        // Dialog para mostrar resultado de verificación
        if (showVerificationResult) {
            AlertDialog(
                onDismissRequest = { showVerificationResult = false },
                title = { 
                    Text(
                        when {
                            verificationMessage.contains("✅") -> "Verificación Exitosa"
                            verificationMessage.contains("❌") -> "Verificación Fallida"
                            verificationMessage.contains("⚠️") -> "Verificación Incierta"
                            verificationMessage.contains("⏱️") -> "Tiempo Agotado"
                            else -> "Estado de Verificación"
                        }
                    ) 
                },
                text = { 
                    Column {
                        Text(verificationMessage)
                        verificationId?.let { id ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "ID de verificación: $id",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVerificationResult = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

}

