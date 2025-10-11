package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.Status
import com.example.tiendasuplementacion.repository.StatusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatusViewModel : ViewModel() {
    private val repository = StatusRepository()
    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    val statuses: StateFlow<List<Status>> = _statuses

    fun fetchStatuses() {
        viewModelScope.launch {
            _statuses.value = repository.getAll()
        }
    }

    fun createStatus(status: Status) {
        viewModelScope.launch {
            repository.create(status)
            fetchStatuses()
        }
    }
}
