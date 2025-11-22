package com.example.tiendasuplementacion.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val ADMIN_ROLE = 2L  // Role ID 2 = Admin, Role ID 1 = User

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository()
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Restaurar la sesión al iniciar
        restoreSession()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                if (email.isBlank() || password.isBlank()) {
                    throw Exception("Por favor complete todos los campos")
                }
                val user = repository.login(email, password)
                
                // Validar si el usuario está habilitado
                if (!user.enabled) {
                    _isAuthenticated.value = false
                    _currentUser.value = null
                    _error.value = "Usuario deshabilitado. Contacte al administrador."
                    return@launch
                }
                
                _currentUser.value = user
                _isAuthenticated.value = true
                // Guardar la sesión
                saveSession(user)
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _currentUser.value = null
                
                // Extraer el mensaje personalizado según el código HTTP
                val message = e.message ?: "Error al iniciar sesión"
                _error.value = when {
                    message.startsWith("404:") -> message.substringAfter("404:")
                    message.startsWith("423:") -> message.substringAfter("423:")
                    message.startsWith("401:") -> message.substringAfter("401:")
                    else -> message
                }
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            try {
                _error.value = null
                if (user.email.isBlank() || user.password.isBlank()) {
                    throw Exception("Por favor complete todos los campos")
                }
                repository.create(user)
                _currentUser.value = user
                _isAuthenticated.value = true
                // Guardar la sesión
                saveSession(user)
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _currentUser.value = null
                _error.value = e.message ?: "Error al registrar usuario"
            }
        }
    }

    private fun saveSession(user: User) {
        Log.d("AuthViewModel", "Saving session for user: $user")
        val sharedPreferences = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("user_id", user.id)
            putString("username", user.username)
            putString("email", user.email)
            putLong("role_id", user.role_id)
            putLong("setting_id", user.setting_id ?: 0L)
            putBoolean("enabled", user.enabled)
            apply()
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            val sharedPreferences = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getLong("user_id", 0L)
            if (userId != 0L) {
                val username = sharedPreferences.getString("username", "") ?: ""
                val email = sharedPreferences.getString("email", "") ?: ""
                val roleId = sharedPreferences.getLong("role_id", 0L)
                val settingId = sharedPreferences.getLong("setting_id", 0L)
                val enabled = sharedPreferences.getBoolean("enabled", true)
                
                val user = User(
                    id = userId,
                    username = username,
                    email = email,
                    password = "", // No almacenamos la contraseña
                    role_id = roleId,
                    setting_id = settingId,
                    enabled = enabled
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

    fun logout() {
        viewModelScope.launch {
            // Limpiar SharedPreferences
            val sharedPreferences = getApplication<Application>().getSharedPreferences("auth", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            
            _isAuthenticated.value = false
            _currentUser.value = null
            _error.value = null
        }
    }

    // Helper para chequear si el usuario actual es administrador
    fun isAdmin(): Boolean = _currentUser.value?.role_id == ADMIN_ROLE
}