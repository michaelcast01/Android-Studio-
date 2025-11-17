package com.example.tiendasuplementacion.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.tiendasuplementacion.ui.theme.AppBeige
import com.example.tiendasuplementacion.ui.theme.AppGray
import com.example.tiendasuplementacion.ui.theme.AppGreen
import com.example.tiendasuplementacion.ui.theme.AppAccentBlue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    settingViewModel: SettingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val settingDetail by settingViewModel.settingDetail.observeAsState()
    val error by settingViewModel.error.observeAsState()
    val appTextColor = AppBeige
    
    // Estados del formulario
    var fullName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Estados de UI
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Cargar datos del usuario
    LaunchedEffect(currentUser?.setting_id) {
        currentUser?.setting_id?.let { settingId ->
            settingViewModel.fetchSettingDetails(settingId)
        }
    }
    
    // Actualizar campos cuando se cargan los datos
    LaunchedEffect(settingDetail) {
        settingDetail?.let { setting ->
            fullName = setting.name
            nickname = setting.nickname
            phone = setting.phone.toString()
            city = setting.city
            address = setting.address
        }
    }
    
    // Manejar errores
    LaunchedEffect(error) {
        error?.let {
            errorMessage = it
            showErrorDialog = true
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Editar Perfil",
                        fontWeight = FontWeight.Bold,
                        color = appTextColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = appTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppGray,
                    titleContentColor = appTextColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppGray,
                            Color(0xFF1A1B20)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar del usuario
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(AppGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.take(2).uppercase(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appTextColor.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Sección de Información Personal
                SectionHeader(
                    icon = Icons.Default.Person,
                    title = "Información Personal",
                    subtitle = "Datos básicos y contacto"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Nombre completo
                ProfileTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Nombre completo",
                    icon = Icons.Default.Person,
                    placeholder = "Ingresa tu nombre completo"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Apodo
                ProfileTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Apodo",
                    icon = Icons.Default.AccountCircle,
                    placeholder = "No especificado"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Teléfono
                ProfileTextField(
                    value = phone,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            phone = it
                        }
                    },
                    label = "Teléfono",
                    icon = Icons.Default.Phone,
                    placeholder = "1234567",
                    keyboardType = KeyboardType.Phone
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Ciudad
                ProfileTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "Ciudad",
                    icon = Icons.Default.LocationCity,
                    placeholder = "32345"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Dirección
                ProfileTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Dirección",
                    icon = Icons.Default.Home,
                    placeholder = "calle 6",
                    singleLine = false,
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón de guardar
                Button(
                    onClick = {
                        if (validateFields(fullName, phone, city, address)) {
                            isLoading = true
                            settingDetail?.let { setting ->
                                settingViewModel.updateSetting(
                                    settingId = setting.id,
                                    name = fullName,
                                    nickname = nickname,
                                    phone = phone.toLongOrNull() ?: 0L,
                                    city = city,
                                    address = address,
                                    paymentId = setting.payments.firstOrNull()?.id ?: 0L,
                                    onSuccess = {
                                        isLoading = false
                                        showSuccessDialog = true
                                    },
                                    onError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                        showErrorDialog = true
                                    }
                                )
                            }
                        } else {
                            errorMessage = "Por favor completa todos los campos requeridos"
                            showErrorDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppAccentBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Cambios",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "¡Perfil Actualizado!",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    "Tus datos han sido actualizados correctamente.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
    
    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Error",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF6E7DF)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f)
                )
            }
        }
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFF6E7DF)) },
        placeholder = { Text(placeholder, color = Color(0xFFF6E7DF).copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        textStyle = TextStyle(color = Color(0xFFF6E7DF)),
        modifier = Modifier
            .fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFFF6E7DF),
            unfocusedLabelColor = Color(0xFFF6E7DF).copy(alpha = 0.7f),
            cursorColor = Color(0xFFF6E7DF)
        )
    )
}

fun validateFields(
    fullName: String,
    phone: String,
    city: String,
    address: String
): Boolean {
    return fullName.isNotBlank() &&
            phone.isNotBlank() &&
            city.isNotBlank() &&
            address.isNotBlank()
}
