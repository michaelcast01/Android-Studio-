@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")

package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.R
import com.example.tiendasuplementacion.model.Role
import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.viewmodel.RoleViewModel
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.ADMIN_ROLE
import com.example.tiendasuplementacion.util.EnvConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    roleViewModel: RoleViewModel = viewModel(),
    settingViewModel: SettingViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Campos adicionales para registro
    var username by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Estados para verificación de email
    var isEmailVerifying by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf("") }
    var currentVerificationId by remember { mutableStateOf<String?>(null) }
    var emailVerificationStatus by remember { mutableStateOf<String?>(null) }
    var emailVerified by remember { mutableStateOf(false) }
    
    val roles by roleViewModel.roles.observeAsState(emptyList())
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val error by authViewModel.error.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Validación de email
    fun isValidEmail(email: String): Boolean = email.contains("@") && email.length > 3
    var emailTouched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        roleViewModel.fetchRoles()
    }

    LaunchedEffect(error) {
        if (error != null) {
            showError = true
            errorMessage = error ?: ""
        }
    }

    // Función para hacer polling del estado de verificación
    fun pollVerificationStatus(verificationId: String) {
        scope.launch {
            var attempts = 0
            val maxAttempts = 10
            
            while (attempts < maxAttempts && isEmailVerifying) {
                delay(3000) // Esperar 3 segundos entre verificaciones
                
                try {
                    Log.d("LoginScreen", "Verificando estado para verificationId: $verificationId")
                    
                    val status = settingViewModel.checkVerificationStatus(verificationId)
                    
                    Log.d("LoginScreen", "Estado recibido: ${status.status}")
                    
                    when (status.status) {
                        "COMPLETED" -> {
                            isEmailVerifying = false
                            emailVerificationStatus = status.email_status
                            
                            when (status.email_status) {
                                "VALID" -> {
                                    verificationMessage = "✅ Email verificado correctamente. Ahora puedes completar tu registro."
                                    emailVerified = true
                                }
                                "INVALID" -> {
                                    verificationMessage = "❌ Email no válido. Por favor, verifica tu dirección de correo."
                                    emailVerified = false
                                }
                                "UNKNOWN" -> {
                                    verificationMessage = "⚠️ Estado del email incierto. Puedes continuar pero se recomienda verificar nuevamente."
                                    emailVerified = false
                                }
                            }
                            showVerificationDialog = true
                            break
                        }
                        "FAILED" -> {
                            isEmailVerifying = false
                            verificationMessage = "❌ Error en la verificación del email"
                            emailVerified = false
                            showVerificationDialog = true
                            break
                        }
                        "PENDING" -> {
                            Log.d("LoginScreen", "Verificación aún pendiente... intento ${attempts + 1}/$maxAttempts")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Error al verificar estado en intento ${attempts + 1}", e)
                    
                    if (attempts >= maxAttempts - 1) {
                        isEmailVerifying = false
                        verificationMessage = "❌ Error al verificar estado: ${e.message}"
                        emailVerified = false
                        showVerificationDialog = true
                        break
                    }
                }
                
                attempts++
            }
            
            if (attempts >= maxAttempts && isEmailVerifying) {
                isEmailVerifying = false
                verificationMessage = "⏱️ Tiempo de verificación agotado. Puedes continuar pero se recomienda verificar el email."
                emailVerified = false
                showVerificationDialog = true
            }
        }
    }

    // Función para iniciar verificación de email
    fun startEmailVerification(email: String) {
        isEmailVerifying = true
        verificationMessage = "📧 Enviando verificación..."
        showVerificationDialog = true
        
        scope.launch {
            try {
                Log.d("LoginScreen", "Iniciando verificación de email para: $email")
                
                val response = settingViewModel.startEmailVerification(
                    email = email,
                    callbackUrl = "http://localhost:8080/api/email-verification/callback"
                )
                
                Log.d("LoginScreen", "Respuesta de startEmailVerification: $response")

                if (response.verification_id.isNullOrEmpty()) {
                    isEmailVerifying = false
                    verificationMessage = "❌ Error: No se pudo iniciar la verificación."
                    emailVerified = false
                    showVerificationDialog = true
                    return@launch
                }

                currentVerificationId = response.verification_id
                verificationMessage = "📧 Verificación enviada. Verificando estado..."

                // Iniciar polling para verificar el estado
                pollVerificationStatus(response.verification_id)

            } catch (e: Exception) {
                isEmailVerifying = false
                verificationMessage = "❌ Error al enviar verificación: ${e.message}"
                emailVerified = false
                showVerificationDialog = true
                Log.e("LoginScreen", "Error starting email verification", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF26272B)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .verticalScroll(scrollState),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF26272B)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRegistering) "Registro" else "DiamondSuplements",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF6E7DF)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isRegistering) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Nombre de Usuario", color = Color(0xFFF6E7DF)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFF6E7DF))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF26272B),
                            unfocusedContainerColor = Color(0xFF26272B),
                            focusedTextColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailTouched = true
                            // Reset verification status when email changes
                            if (isRegistering) {
                                emailVerified = false
                                emailVerificationStatus = null
                            }
                        },
                        label = { Text("Email", color = Color(0xFFF6E7DF)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFF6E7DF))
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        isError = emailTouched && !isValidEmail(email),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF26272B),
                            unfocusedContainerColor = Color(0xFF26272B),
                            focusedTextColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    if (isRegistering && isValidEmail(email)) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Email verification status indicator
                        when {
                            isEmailVerifying -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF4CAF50),
                                    strokeWidth = 2.dp
                                )
                            }
                            emailVerified -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Email verificado",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            emailVerificationStatus == "INVALID" -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Email no válido",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {
                                IconButton(
                                    onClick = { 
                                        if (!isEmailVerifying && isValidEmail(email)) {
                                            startEmailVerification(email)
                                        }
                                    },
                                    enabled = !isEmailVerifying && isValidEmail(email)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verificar email",
                                        tint = Color(0xFFF6E7DF)
                                    )
                                }
                            }
                        }
                    }
                }

                if (emailTouched && !isValidEmail(email)) {
                    Text(
                        text = "El correo debe contener @",
                        color = Color(0xFFF6E7DF),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                if (isRegistering && !emailVerified && isValidEmail(email)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Debes verificar tu email antes de registrarte. Haz clic en el ícono de verificación junto al campo de email.",
                            color = Color(0xFFFF9800),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color(0xFFF6E7DF)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF6E7DF))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF26272B),
                        unfocusedContainerColor = Color(0xFF26272B),
                        focusedTextColor = Color(0xFFF6E7DF),
                        unfocusedTextColor = Color(0xFFF6E7DF),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (isRegistering) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono", color = Color(0xFFF6E7DF)) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFF6E7DF))
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF26272B),
                                unfocusedContainerColor = Color(0xFF26272B),
                                focusedTextColor = Color(0xFFF6E7DF),
                                unfocusedTextColor = Color(0xFFF6E7DF),
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Ciudad", color = Color(0xFFF6E7DF)) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color(0xFFF6E7DF))
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF26272B),
                                unfocusedContainerColor = Color(0xFF26272B),
                                focusedTextColor = Color(0xFFF6E7DF),
                                unfocusedTextColor = Color(0xFFF6E7DF),
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección", color = Color(0xFFF6E7DF)) },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFFF6E7DF))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF26272B),
                            unfocusedContainerColor = Color(0xFF26272B),
                            focusedTextColor = Color(0xFFF6E7DF),
                            unfocusedTextColor = Color(0xFFF6E7DF),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showError) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFF6E7DF),
                            modifier = Modifier.padding(8.dp),
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            // Si estamos en modo registro y el email no está verificado, iniciar verificación
                            if (isRegistering && !emailVerified && isValidEmail(email)) {
                                startEmailVerification(email)
                                return@launch
                            }
                            
                            isLoading = true
                            showError = false
                            errorMessage = ""

                            if (isRegistering) {
                                // Verificar que el email esté validado antes de continuar
                                if (!emailVerified) {
                                    showError = true
                                    errorMessage = "Debes verificar tu email antes de registrarte"
                                    isLoading = false
                                    return@launch
                                }

                                val setting = Setting(
                                    id = 0,
                                    payment_id = 1,
                                    name = username,
                                    nickname = "",
                                    phone = phone.toLongOrNull() ?: 0L,
                                    city = city,
                                    address = address
                                )
                                val createdSetting = settingViewModel.createSetting(setting)
                                val user = User(
                                    id = 0,
                                    username = username,
                                    email = email,
                                    password = password,
                                    role_id = 1L,
                                    setting_id = createdSetting.id
                                )
                                authViewModel.register(user)
                            } else {
                                authViewModel.login(email, password)
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegistering && !emailVerified && isValidEmail(email)) {
                            Color(0xFFFF9800) // Color naranja para indicar que verificará el email
                        } else if (isRegistering && !emailVerified) {
                            Color(0xFF4B5FD5).copy(alpha = 0.5f)
                        } else {
                            Color(0xFF4B5FD5)
                        },
                        contentColor = Color.White
                    ),
                    enabled = if (isRegistering) {
                        // Permitir hacer clic si todos los campos están llenos y el email es válido
                        email.isNotBlank() && password.isNotBlank() && username.isNotBlank() &&
                        phone.isNotBlank() && city.isNotBlank() && address.isNotBlank() && 
                        isValidEmail(email) && !isLoading && !isEmailVerifying
                    } else {
                        email.isNotBlank() && password.isNotBlank() && isValidEmail(email) && !isLoading
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isRegistering) {
                                if (!emailVerified && isValidEmail(email)) {
                                    "Verificar Email"
                                } else {
                                    "Registrar"
                                }
                            } else {
                                "Ingresar"
                            },
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { 
                        isRegistering = !isRegistering
                        // Reset verification states when switching modes
                        emailVerified = false
                        emailVerificationStatus = null
                        isEmailVerifying = false
                    }
                ) {
                    Text(
                        if (isRegistering) "¿Ya tienes una cuenta? Inicia sesión" 
                        else "¿No tienes una cuenta? Regístrate",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (currentUser?.role_id == ADMIN_ROLE) {
                    // Mostrar botón de agregar producto
                }
            }
        }

        // Dialog para mostrar resultado de verificación de email
        if (showVerificationDialog) {
            AlertDialog(
                onDismissRequest = { showVerificationDialog = false },
                title = {
                    Text(
                        when {
                            verificationMessage.contains("✅") -> "Email Verificado"
                            verificationMessage.contains("❌") -> "Error de Verificación"
                            verificationMessage.contains("⚠️") -> "Verificación Incierta"
                            verificationMessage.contains("⏱️") -> "Tiempo Agotado"
                            verificationMessage.contains("📧") -> "Verificando Email"
                            else -> "Estado de Verificación"
                        }
                    )
                },
                text = {
                    Column {
                        if (isEmailVerifying) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(verificationMessage)
                            }
                        } else {
                            Text(verificationMessage)
                        }
                        
                        currentVerificationId?.let { id ->
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
                    if (!isEmailVerifying) {
                        TextButton(onClick = { showVerificationDialog = false }) {
                            Text("Continuar")
                        }
                    }
                },
                dismissButton = {
                    if (!isEmailVerifying && !emailVerified && isValidEmail(email)) {
                        TextButton(
                            onClick = {
                                showVerificationDialog = false
                                startEmailVerification(email)
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            )
        }
    }
}
