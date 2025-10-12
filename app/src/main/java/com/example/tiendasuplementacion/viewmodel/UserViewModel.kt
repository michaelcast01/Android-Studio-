package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _users.value = repository.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch { _events.emit(UiEvent.ShowError(e.message ?: "Error al obtener usuarios")) }
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
                viewModelScope.launch { _events.emit(UiEvent.ShowError(e.message ?: "Error al crear usuario")) }
            }
        }
    }
}
