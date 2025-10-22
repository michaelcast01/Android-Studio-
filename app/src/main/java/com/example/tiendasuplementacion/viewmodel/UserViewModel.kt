package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _users.value = repository.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
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
            }
        }
    }
}
