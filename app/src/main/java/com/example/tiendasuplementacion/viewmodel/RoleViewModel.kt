package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.Role
import com.example.tiendasuplementacion.repository.RoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoleViewModel : ViewModel() {
    private val repository = RoleRepository()
    private val _roles = MutableStateFlow<List<Role>>(emptyList())
    val roles: StateFlow<List<Role>> = _roles

    fun fetchRoles() {
        viewModelScope.launch {
            _roles.value = repository.getAll()
        }
    }

    fun createRole(role: Role) {
        viewModelScope.launch {
            repository.create(role)
            fetchRoles()
        }
    }
}
