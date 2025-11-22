package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toggleSuccess = MutableLiveData<Boolean>()
    val toggleSuccess: LiveData<Boolean> = _toggleSuccess

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _users.value = repository.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al cargar usuarios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(user: User) {
        viewModelScope.launch {
            try {
                repository.create(user)
                fetchUsers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Error al crear usuario: ${e.message}"
            }
        }
    }

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

    fun clearError() {
        _error.value = null
    }
}
