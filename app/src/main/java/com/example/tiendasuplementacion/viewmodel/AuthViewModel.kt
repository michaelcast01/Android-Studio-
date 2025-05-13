package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                if (username.isBlank() || password.isBlank()) {
                    throw Exception("Por favor complete todos los campos")
                }
                val user = repository.login(username, password)
                _currentUser.value = user
                _isAuthenticated.value = true
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

    fun register(user: User) {
        viewModelScope.launch {
            try {
                _error.value = null
                if (user.username.isBlank() || user.password.isBlank() || user.email.isBlank()) {
                    throw Exception("Por favor complete todos los campos")
                }
                repository.create(user)
                _currentUser.value = user
                _isAuthenticated.value = true
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _currentUser.value = null
                _error.value = e.message ?: "Error al registrar usuario"
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentUser.value = null
        _error.value = null
    }
}