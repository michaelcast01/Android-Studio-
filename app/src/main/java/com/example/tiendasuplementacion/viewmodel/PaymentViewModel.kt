package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()
    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> = _payments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchPayments()
    }

    fun fetchPayments() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _payments.value = repository.getAll()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los métodos de pago"
                _payments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPayment(payment: Payment) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val createdPayment = repository.create(payment)
                val currentList = _payments.value?.toMutableList() ?: mutableListOf()
                currentList.add(createdPayment)
                _payments.value = currentList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear el método de pago"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getPaymentById(paymentId: Long): Payment? {
        return payments.value?.find { it.id == paymentId }
    }
}
