# ✅ Implementación Completa: Habilitar/Deshabilitar Usuarios

## 📋 Resumen
Se ha implementado exitosamente la funcionalidad para que el administrador pueda habilitar/deshabilitar usuarios desde la aplicación Android.

---

## 🔧 Cambios Realizados en Android (Kotlin + Jetpack Compose)

### 1. ✅ Modelo `User.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/model/User.kt`

**Cambio realizado:**
- Agregado campo `enabled` (Boolean, default: true)

```kotlin
data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val role_id: Long,
    val setting_id: Long? = null,
    val enabled: Boolean = true  // ← NUEVO CAMPO
)
```

---

### 2. ✅ Interfaz API Service `UserApiService.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/interfaces/UserApiService.kt`

**Cambio realizado:**
- Agregado endpoint para toggle enabled

```kotlin
@PATCH("/api/users/{id}/toggle-enabled")
suspend fun toggleUserEnabled(@Path("id") id: Long): User
```

---

### 3. ✅ Repositorio `UserRepository.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/repository/UserRepository.kt`

**Cambio realizado:**
- Agregado método `toggleEnabled()`

```kotlin
suspend fun toggleEnabled(id: Long): User = service.toggleUserEnabled(id)
```

---

### 4. ✅ ViewModel `AuthViewModel.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/viewmodel/AuthViewModel.kt`

**Cambios realizados:**

#### a) Validación en el login
```kotlin
fun login(email: String, password: String) {
    viewModelScope.launch {
        try {
            _error.value = null
            if (email.isBlank() || password.isBlank()) {
                throw Exception("Por favor complete todos los campos")
            }
            val user = repository.login(email, password)
            
            // ✅ Validar si el usuario está habilitado
            if (!user.enabled) {
                _isAuthenticated.value = false
                _currentUser.value = null
                _error.value = "Usuario deshabilitado. Contacte al administrador."
                return@launch
            }
            
            _currentUser.value = user
            _isAuthenticated.value = true
            saveSession(user)
        } catch (e: Exception) {
            _isAuthenticated.value = false
            _currentUser.value = null
            _error.value = if (e.message?.contains("404") == true) {
                "Usuario o contraseña incorrectos"
            } else {
                e.message ?: "Error al iniciar sesión"
            }
        }
    }
}
```

#### b) Guardar estado enabled en sesión
```kotlin
private fun saveSession(user: User) {
    Log.d("AuthViewModel", "Saving session for user: $user")
    val sharedPreferences = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putLong("user_id", user.id)
        putString("username", user.username)
        putString("email", user.email)
        putLong("role_id", user.role_id)
        putLong("setting_id", user.setting_id ?: 0L)
        putBoolean("enabled", user.enabled)  // ← NUEVO
        apply()
    }
}
```

#### c) Restaurar estado enabled
```kotlin
fun restoreSession() {
    viewModelScope.launch {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", 0L)
        if (userId != 0L) {
            val username = sharedPreferences.getString("username", "") ?: ""
            val email = sharedPreferences.getString("email", "") ?: ""
            val roleId = sharedPreferences.getLong("role_id", 0L)
            val settingId = sharedPreferences.getLong("setting_id", 0L)
            val enabled = sharedPreferences.getBoolean("enabled", true)  // ← NUEVO
            
            val user = User(
                id = userId,
                username = username,
                email = email,
                password = "",
                role_id = roleId,
                setting_id = settingId,
                enabled = enabled  // ← NUEVO
            )
            Log.d("AuthViewModel", "Restoring session for user: $user")
            _currentUser.value = user
            _isAuthenticated.value = true
        } else {
            Log.d("AuthViewModel", "No session found")
            _isAuthenticated.value = false
            _currentUser.value = null
        }
    }
}
```

---

### 5. ✅ ViewModel `UserViewModel.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/viewmodel/UserViewModel.kt`

**Cambios realizados:**
- Agregado método `toggleUserEnabled()`
- Agregados estados para error y éxito

```kotlin
private val _error = MutableLiveData<String?>()
val error: LiveData<String?> = _error

private val _toggleSuccess = MutableLiveData<Boolean>()
val toggleSuccess: LiveData<Boolean> = _toggleSuccess

fun toggleUserEnabled(userId: Long) {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            _error.value = null
            repository.toggleEnabled(userId)
            _toggleSuccess.value = true
            // Recargar la lista de usuarios
            fetchUsers()
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Error al cambiar estado del usuario: ${e.message}"
            _toggleSuccess.value = false
        } finally {
            _isLoading.value = false
        }
    }
}
```

---

### 6. ✅ Pantalla `AdminClientsScreen.kt`
**Ubicación:** `app/src/main/java/com/example/tiendasuplementacion/screen/AdminClientsScreen.kt`

**Cambios realizados:**

#### a) Agregar UserViewModel al componente
```kotlin
@Composable
fun AdminClientsScreen(
    navController: NavController,
    userDetailViewModel: UserDetailViewModel = viewModel(),
    userViewModel: com.example.tiendasuplementacion.viewmodel.UserViewModel = viewModel()  // ← NUEVO
) {
    // Estados observables
    val users by userViewModel.users.observeAsState(emptyList())
    val toggleSuccess by userViewModel.toggleSuccess.observeAsState(false)
    val userError by userViewModel.error.observeAsState()
    
    var showToggleConfirmDialog by remember { mutableStateOf(false) }
    
    // ... resto del código
}
```

#### b) Cargar usuarios al iniciar
```kotlin
LaunchedEffect(Unit) {
    userDetailViewModel.fetchUserDetailsByRole(1L)
    userViewModel.fetchUsers()  // ← NUEVO
}

// Recargar después del toggle
LaunchedEffect(toggleSuccess) {
    if (toggleSuccess) {
        userDetailViewModel.fetchUserDetailsByRole(1L)
        selectedUser = null
    }
}
```

#### c) Actualizar OptimizedClientCard con indicador visual
```kotlin
@Composable
fun OptimizedClientCard(
    userDetail: UserDetail,
    onClick: () -> Unit,
    userViewModel: com.example.tiendasuplementacion.viewmodel.UserViewModel = viewModel()
) {
    val stats = rememberClientStats(userDetail)
    val users by userViewModel.users.observeAsState(emptyList())
    val userEnabled = users.find { it.id == userDetail.id }?.enabled ?: true
    
    // Indicador visual si está deshabilitado
    if (!userEnabled) {
        Surface(
            color = Color(0xFFf44336),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Deshabilitado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Cambiar color del indicador de estado
    val statusColor = when {
        !userEnabled -> Color(0xFFf44336) // Rojo si está deshabilitado
        stats.totalOrders == 0 -> Color(0xFF757575)
        stats.lastOrderDate.contains("2025-09") -> Color(0xFF4CAF50)
        stats.totalOrders > 5 -> Color(0xFF2196F3)
        else -> Color(0xFFFF9800)
    }
}
```

#### d) Agregar botón en el diálogo de detalles
```kotlin
confirmButton = {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón para habilitar/deshabilitar usuario
        selectedUser?.let { user ->
            val userEnabled = users.find { it.id == user.id }?.enabled ?: true
            Button(
                onClick = { showToggleConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userEnabled) Color(0xFFf44336) else Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = if (userEnabled) Icons.Default.Block else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (userEnabled) "Deshabilitar" else "Habilitar")
            }
        }
        
        TextButton(onClick = { selectedUser = null }) {
            Text("Cerrar")
        }
    }
}
```

#### e) Diálogo de confirmación
```kotlin
if (showToggleConfirmDialog && selectedUser != null) {
    val userEnabled = users.find { it.id == selectedUser?.id }?.enabled ?: true
    AlertDialog(
        onDismissRequest = { showToggleConfirmDialog = false },
        title = {
            Text(
                text = if (userEnabled) "¿Deshabilitar Usuario?" else "¿Habilitar Usuario?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (userEnabled) 
                    "El usuario ${selectedUser?.username} no podrá iniciar sesión si lo deshabilitas." 
                else 
                    "El usuario ${selectedUser?.username} podrá iniciar sesión nuevamente."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedUser?.let { user ->
                        userViewModel.toggleUserEnabled(user.id)
                    }
                    showToggleConfirmDialog = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userEnabled) Color(0xFFf44336) else Color(0xFF4CAF50)
                )
            ) {
                Text(if (userEnabled) "Deshabilitar" else "Habilitar")
            }
        },
        dismissButton = {
            TextButton(onClick = { showToggleConfirmDialog = false }) {
                Text("Cancelar")
            }
        }
    )
}
```

#### f) Mostrar estado en información del usuario
```kotlin
// En el diálogo de detalles, agregar después de Rol
selectedUser?.let { user ->
    val userEnabled = users.find { it.id == user.id }?.enabled ?: true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Estado:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Surface(
            color = if (userEnabled) Color(0xFF4CAF50) else Color(0xFFf44336),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (userEnabled) Icons.Default.CheckCircle else Icons.Default.Block,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (userEnabled) "Activo" else "Deshabilitado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

---

## 📦 Archivos Modificados

1. ✅ `app/src/main/java/com/example/tiendasuplementacion/model/User.kt`
2. ✅ `app/src/main/java/com/example/tiendasuplementacion/interfaces/UserApiService.kt`
3. ✅ `app/src/main/java/com/example/tiendasuplementacion/repository/UserRepository.kt`
4. ✅ `app/src/main/java/com/example/tiendasuplementacion/viewmodel/AuthViewModel.kt`
5. ✅ `app/src/main/java/com/example/tiendasuplementacion/viewmodel/UserViewModel.kt`
6. ✅ `app/src/main/java/com/example/tiendasuplementacion/screen/AdminClientsScreen.kt`

---

## 🎯 Funcionalidades Implementadas

### ✅ Frontend Android
- [x] Modelo User actualizado con campo `enabled`
- [x] API Service con endpoint de toggle enabled
- [x] Repository con método toggleEnabled
- [x] AuthViewModel valida usuarios habilitados en login
- [x] UserViewModel con método para cambiar estado
- [x] AdminClientsScreen con botón de habilitar/deshabilitar
- [x] Indicador visual en tarjetas de clientes
- [x] Badge de estado en detalles del usuario
- [x] Diálogo de confirmación antes de cambiar estado
- [x] Validación de usuarios deshabilitados al intentar login

### 🔄 Backend (Ya implementado según tu documento)
- [x] Campo `enabled` en modelo Users
- [x] Método `toggleEnabled()` en UsersService
- [x] Endpoint `PATCH /api/users/{id}/toggle-enabled`
- [x] Validación en login para usuarios habilitados

---

## 🧪 Cómo Probar

### 1. Compilar la aplicación
```bash
./gradlew build
```

### 2. Ejecutar en emulador/dispositivo
```bash
./gradlew installDebug
```

### 3. Pasos para probar:

#### a) Como Administrador:
1. Iniciar sesión con cuenta de administrador
2. Navegar a la sección de "Clientes"
3. Seleccionar un cliente (hacer tap en su tarjeta)
4. En el diálogo de detalles, verás:
   - Estado actual del usuario (Activo/Deshabilitado)
   - Botón "Deshabilitar" (rojo) o "Habilitar" (verde)
5. Presionar el botón
6. Confirmar en el diálogo de confirmación
7. El estado se actualizará inmediatamente

#### b) Como Usuario Deshabilitado:
1. Cerrar sesión
2. Intentar iniciar sesión con un usuario deshabilitado
3. Debería mostrar el error: "Usuario deshabilitado. Contacte al administrador."

---

## 🎨 UI/UX Implementado

### Indicadores Visuales:
1. **Badge "Deshabilitado"** en las tarjetas de clientes
   - Color rojo (#f44336)
   - Ícono de bloqueo
   - Solo se muestra cuando el usuario está deshabilitado

2. **Punto de estado** en la esquina superior derecha de cada tarjeta
   - Rojo: Usuario deshabilitado
   - Gris: Sin pedidos
   - Verde: Cliente activo reciente
   - Azul: Cliente con muchos pedidos
   - Naranja: Cliente estándar

3. **Badge de estado** en detalles del usuario
   - Verde con ícono ✓: Activo
   - Rojo con ícono 🚫: Deshabilitado

4. **Botones contextuales**
   - Botón rojo "Deshabilitar" cuando el usuario está activo
   - Botón verde "Habilitar" cuando el usuario está deshabilitado

5. **Diálogo de confirmación**
   - Mensaje claro sobre las consecuencias
   - Botón de cancelar para prevenir errores

---

## 🔒 Seguridad

### Validaciones Implementadas:

1. **En el Login:**
   - Se verifica que `user.enabled == true`
   - Mensaje de error específico si el usuario está deshabilitado
   - No se guarda la sesión si el usuario está deshabilitado

2. **En el Frontend:**
   - Solo administradores pueden acceder a la pantalla de clientes
   - Confirmación antes de cambiar el estado
   - Recarga automática de datos después del cambio

3. **Pendiente en Backend (Recomendado):**
   - Agregar validación de rol de administrador en el endpoint
   - Usar Spring Security con `@PreAuthorize("hasRole('ADMIN')")`
   - Incluir token JWT en las peticiones

---

## 📝 Notas Importantes

1. **Usuarios Deshabilitados:**
   - No pueden iniciar sesión
   - Aparecen marcados visualmente en la lista de clientes
   - Sus datos se conservan (no se eliminan)

2. **Sincronización:**
   - Los cambios se reflejan inmediatamente después del toggle
   - Se recarga la lista de clientes automáticamente

3. **Estado por Defecto:**
   - Los usuarios nuevos se crean habilitados por defecto (`enabled = true`)

4. **Persistencia:**
   - El estado `enabled` se guarda en SharedPreferences junto con la sesión
   - Se restaura al reabrir la aplicación

---

## 🚀 Próximos Pasos Sugeridos

### Backend:
- [ ] Ejecutar SQL en Supabase para agregar columna `enabled`
- [ ] Desplegar cambios del backend a Render
- [ ] Agregar autenticación JWT si no existe
- [ ] Implementar validación de roles en el endpoint

### Testing:
- [ ] Probar con diferentes usuarios
- [ ] Verificar que usuarios deshabilitados no puedan login
- [ ] Probar la sincronización entre dispositivos
- [ ] Verificar permisos de administrador

### Mejoras Futuras:
- [ ] Log de auditoría (quién deshabilitó a quién y cuándo)
- [ ] Notificación al usuario cuando es deshabilitado
- [ ] Filtro para mostrar solo usuarios activos/deshabilitados
- [ ] Estadísticas de usuarios activos vs deshabilitados

---

## ✨ Resultado Final

La funcionalidad está **100% implementada** en el frontend Android. Los administradores ahora pueden:

✅ Ver el estado de cada usuario (activo/deshabilitado)  
✅ Habilitar o deshabilitar usuarios con un botón  
✅ Ver confirmación antes de realizar el cambio  
✅ Los cambios se reflejan inmediatamente en la UI  
✅ Los usuarios deshabilitados no pueden iniciar sesión  

---

## 📞 Contacto

Si necesitas ajustar algo o tienes dudas sobre la implementación, aquí está toda la información documentada.

**¡Implementación completada exitosamente! 🎉**
