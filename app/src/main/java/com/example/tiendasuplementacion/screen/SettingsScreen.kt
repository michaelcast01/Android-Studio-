package com.example.tiendasuplementacion.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel
import com.example.tiendasuplementacion.component.NetworkErrorBanner
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch

// Enumeración para secciones del perfil
enum class ProfileSection(
    val title: String, 
    val icon: ImageVector,
    val description: String
) {
    PERSONAL_INFO("Información Personal", Icons.Default.Person, "Datos básicos y contacto"),
    PAYMENT_METHODS("Métodos de Pago", Icons.Default.CreditCard, "Gestiona tus formas de pago"),
    SECURITY("Seguridad", Icons.Default.Security, "Configuración de seguridad"),
    NOTIFICATIONS("Notificaciones", Icons.Default.Notifications, "Preferencias de notificaciones"),
    HELP_SUPPORT("Ayuda y Soporte", Icons.Default.Help, "Centro de ayuda"),
    ABOUT("Acerca de", Icons.Default.Info, "Información de la aplicación")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    
    // Estados de UI optimizados
    var showNetworkError by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var expandedSections by remember { mutableStateOf(setOf(ProfileSection.PERSONAL_INFO)) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var isAddingPayment by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var addedPaymentName by remember { mutableStateOf("") }
    var selectedPaymentToAdd by remember { mutableStateOf<com.example.tiendasuplementacion.model.Payment?>(null) }
    
    // Utilitarios para UX avanzada
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val listState = rememberLazyListState()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF23242A),
                        Color(0xFF1A1B20)
                    )
                )
            )
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header optimizado con avatar y info básica
            item {
                ProfileHeader(
                    currentUser = currentUser,
                    settingDetail = settingDetail,
                    onEditProfile = { /* TODO: Implementar edición */ }
                )
            }

            // Secciones dinámicas y expansibles
            ProfileSection.values().forEach { section ->
                item(key = section.name) {
                    when (section) {
                        ProfileSection.PERSONAL_INFO -> {
                            PersonalInfoSection(
                                settingDetail = settingDetail,
                                isExpanded = section in expandedSections,
                                onToggleExpansion = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expandedSections = if (section in expandedSections) {
                                        expandedSections - section
                                    } else {
                                        expandedSections + section
                                    }
                                }
                            )
                        }
                        ProfileSection.PAYMENT_METHODS -> {
                            PaymentMethodsSection(
                                settingDetail = settingDetail,
                                availablePayments = availablePayments,
                                isExpanded = section in expandedSections,
                                isClient = currentUser?.role_id == 1L,
                                onToggleExpansion = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expandedSections = if (section in expandedSections) {
                                        expandedSections - section
                                    } else {
                                        expandedSections + section
                                    }
                                },
                                onAddPayment = { showAddPaymentDialog = true }
                            )
                        }
                        else -> {
                            GenericProfileSection(
                                section = section,
                                isExpanded = section in expandedSections,
                                onToggleExpansion = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expandedSections = if (section in expandedSections) {
                                        expandedSections - section
                                    } else {
                                        expandedSections + section
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Espaciado final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB para acciones rápidas
        AnimatedVisibility(
            visible = listState.firstVisibleItemIndex > 2,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = Color(0xFF26272B),
                contentColor = Color(0xFFF6E7DF),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ir arriba")
            }
        }

        // Diálogos optimizados
        if (showAddPaymentDialog) {
            OptimizedAddPaymentDialog(
                availablePayments = availablePayments,
                currentPayments = settingDetail?.payments,
                isLoading = isAddingPayment,
                onDismiss = { showAddPaymentDialog = false },
                onPaymentSelected = { payment ->
                    selectedPaymentToAdd = payment
                    isAddingPayment = true
                    addedPaymentName = payment.name
                    settingViewModel.addPaymentMethod(payment.id)
                    showSuccessMessage = true
                    showAddPaymentDialog = false
                }
            )
        }

        if (showSuccessMessage) {
            OptimizedSuccessDialog(
                title = "¡Método Agregado!",
                message = "Se ha agregado '$addedPaymentName' correctamente a tus métodos de pago.",
                onDismiss = { 
                    showSuccessMessage = false
                    isAddingPayment = false
                    selectedPaymentToAdd = null
                }
            )
        }

        // Banner de error optimizado
        AnimatedVisibility(
            visible = showNetworkError,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            NetworkErrorBanner(
                message = networkErrorMessage,
                onRetry = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

// Componentes optimizados para la vista de configuración

@Composable
fun ProfileHeader(
    currentUser: com.example.tiendasuplementacion.model.User?,
    settingDetail: com.example.tiendasuplementacion.model.SettingDetail?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26272B)
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con iniciales
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                color = Color(0xFF4CAF50)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.username?.take(2)?.uppercase() ?: "US",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentUser?.username ?: "Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFF6E7DF),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser?.email ?: "email@ejemplo.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF6E7DF).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                settingDetail?.name?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(
                onClick = onEditProfile,
                modifier = Modifier
                    .background(
                        Color(0xFFF6E7DF).copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar perfil",
                    tint = Color(0xFFF6E7DF)
                )
            }
        }
    }
}

@Composable
fun PersonalInfoSection(
    settingDetail: com.example.tiendasuplementacion.model.SettingDetail?,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit
) {
    ExpandableProfileCard(
        section = ProfileSection.PERSONAL_INFO,
        isExpanded = isExpanded,
        onToggle = onToggleExpansion
    ) {
        settingDetail?.let { detail ->
            PersonalInfoItem(
                icon = Icons.Default.Person,
                label = "Nombre completo",
                value = detail.name
            )
            PersonalInfoItem(
                icon = Icons.Default.Badge,
                label = "Apodo",
                value = detail.nickname
            )
            PersonalInfoItem(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = detail.phone.toString()
            )
            PersonalInfoItem(
                icon = Icons.Default.LocationCity,
                label = "Ciudad",
                value = detail.city
            )
            PersonalInfoItem(
                icon = Icons.Default.Home,
                label = "Dirección",
                value = detail.address
            )
        } ?: EmptyStateMessage("No se encontró información personal")
    }
}

@Composable
fun PaymentMethodsSection(
    settingDetail: com.example.tiendasuplementacion.model.SettingDetail?,
    availablePayments: List<com.example.tiendasuplementacion.model.Payment>?,
    isExpanded: Boolean,
    isClient: Boolean,
    onToggleExpansion: () -> Unit,
    onAddPayment: () -> Unit
) {
    ExpandableProfileCard(
        section = ProfileSection.PAYMENT_METHODS,
        isExpanded = isExpanded,
        onToggle = onToggleExpansion,
        actionButton = if (isClient) {
            {
                IconButton(
                    onClick = onAddPayment,
                    modifier = Modifier
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar método",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        } else null
    ) {
        settingDetail?.payments?.let { payments ->
            if (payments.isEmpty()) {
                EmptyStateMessage("No hay métodos de pago configurados")
            } else {
                payments.forEach { payment ->
                    PaymentMethodItem(payment = payment)
                }
            }
        } ?: EmptyStateMessage("Cargando métodos de pago...")
    }
}

@Composable
fun GenericProfileSection(
    section: ProfileSection,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit
) {
    ExpandableProfileCard(
        section = section,
        isExpanded = isExpanded,
        onToggle = onToggleExpansion
    ) {
        EmptyStateMessage("Próximamente disponible")
    }
}

@Composable
fun ExpandableProfileCard(
    section: ProfileSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    actionButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF26272B)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header de la sección
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF6E7DF).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                section.icon,
                                contentDescription = null,
                                tint = Color(0xFFF6E7DF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFF6E7DF),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = section.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actionButton?.invoke()
                    
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Contraer" else "Expandir",
                            tint = Color(0xFFF6E7DF)
                        )
                    }
                }
            }
            
            // Contenido expansible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(300)
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun PersonalInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFFF6E7DF).copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFF6E7DF).copy(alpha = 0.6f)
            )
            Text(
                text = value.ifEmpty { "No especificado" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF6E7DF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PaymentMethodItem(
    payment: com.example.tiendasuplementacion.model.Payment
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = payment.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFF6E7DF),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFF6E7DF).copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun OptimizedAddPaymentDialog(
    availablePayments: List<com.example.tiendasuplementacion.model.Payment>?,
    currentPayments: List<com.example.tiendasuplementacion.model.Payment>?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPaymentSelected: (com.example.tiendasuplementacion.model.Payment) -> Unit
) {
    val currentPaymentIds = currentPayments?.map { it.id } ?: emptyList()
    val availableToAdd = availablePayments?.filter { it.id !in currentPaymentIds } ?: emptyList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Método de Pago")
            }
        },
        text = {
            Column {
                if (availableToAdd.isEmpty()) {
                    EmptyStateMessage("No hay métodos de pago disponibles para agregar")
                } else {
                    Text(
                        "Selecciona un método de pago:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    availableToAdd.forEach { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { if (!isLoading) onPaymentSelected(payment) },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF6E7DF).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = payment.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun OptimizedSuccessDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("¡Perfecto!")
            }
        }
    )
}