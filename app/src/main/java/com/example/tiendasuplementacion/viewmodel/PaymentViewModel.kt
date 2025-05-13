package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()
    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> = _payments

    fun fetchPayments() {
        viewModelScope.launch {
            _payments.value = repository.getAll()
        }
    }

    fun createPayment(payment: Payment) {
        viewModelScope.launch {
            repository.create(payment)
            fetchPayments()
        }
    }
}
