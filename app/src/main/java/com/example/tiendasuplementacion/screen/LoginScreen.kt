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
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch

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

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailTouched = true
                    },
                    label = { Text("Email", color = Color(0xFFF6E7DF)) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFF6E7DF))
                    },
                    modifier = Modifier.fillMaxWidth(),
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
                if (emailTouched && !isValidEmail(email)) {
                    Text(
                        text = "El correo debe contener @",
                        color = Color(0xFFF6E7DF),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
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
                            isLoading = true
                            showError = false
                            errorMessage = ""

                            if (isRegistering) {
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
                        containerColor = Color(0xFF4B5FD5),
                        contentColor = Color.White
                    ),
                    enabled = if (isRegistering) {
                        email.isNotBlank() && password.isNotBlank() && username.isNotBlank() &&
                        phone.isNotBlank() && city.isNotBlank() && address.isNotBlank() && isValidEmail(email)
                    } else {
                        email.isNotBlank() && password.isNotBlank() && isValidEmail(email)
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isRegistering) "Registrar" else "Ingresar",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isRegistering = !isRegistering }
                ) {
                    Text(
                        if (isRegistering) "¿Ya tienes una cuenta? Inicia sesión" 
                        else "¿No tienes una cuenta? Regístrate",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (currentUser?.role_id == 2L) {
                    // Mostrar botón de agregar producto
                }
            }
        }
    }
}
