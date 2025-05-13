package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Status
import com.example.tiendasuplementacion.repository.StatusRepository
import kotlinx.coroutines.launch

class StatusViewModel : ViewModel() {
    private val repository = StatusRepository()
    private val _statuses = MutableLiveData<List<Status>>()
    val statuses: LiveData<List<Status>> = _statuses

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
