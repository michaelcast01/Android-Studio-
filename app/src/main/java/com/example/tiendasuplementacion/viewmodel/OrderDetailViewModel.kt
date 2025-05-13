package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.OrderDetail
import com.example.tiendasuplementacion.repository.OrderDetailRepository
import kotlinx.coroutines.launch

class OrderDetailViewModel : ViewModel() {
    private val repository = OrderDetailRepository()
    private val _details = MutableLiveData<List<OrderDetail>>()
    val details: LiveData<List<OrderDetail>> = _details

    fun fetchOrderDetails() {
        viewModelScope.launch {
            _details.value = repository.getAll()
        }
    }

    fun createDetail(detail: OrderDetail) {
        viewModelScope.launch {
            repository.create(detail)
            fetchOrderDetails()
        }
    }
}
