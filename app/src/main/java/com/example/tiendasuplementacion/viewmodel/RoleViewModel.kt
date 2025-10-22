package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Role
import com.example.tiendasuplementacion.repository.RoleRepository
import kotlinx.coroutines.launch

class RoleViewModel : ViewModel() {
    private val repository = RoleRepository()
    private val _roles = MutableLiveData<List<Role>>()
    val roles: LiveData<List<Role>> = _roles

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
