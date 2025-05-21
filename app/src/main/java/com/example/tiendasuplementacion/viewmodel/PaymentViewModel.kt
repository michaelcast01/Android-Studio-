package com.example.tiendasuplementacion.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()
    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> = _payments

    private val _paymentDetails = MutableLiveData<List<PaymentDetail>>()
    val paymentDetails: LiveData<List<PaymentDetail>> = _paymentDetails

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

    fun fetchPaymentDetails(userId: Long) {
        Log.d("PaymentViewModel", "Fetching payment details for user: $userId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val details = repository.getPaymentDetails(userId)
                Log.d("PaymentViewModel", "Received payment details: $details")
                _paymentDetails.value = details
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error fetching payment details", e)
                _error.value = e.message ?: "Error al cargar los detalles de pago"
                _paymentDetails.value = emptyList()
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

    suspend fun savePaymentDetail(paymentDetail: PaymentDetail): Boolean {
        Log.d("PaymentViewModel", "Attempting to save payment detail: $paymentDetail")
        Log.d("PaymentViewModel", "Payment ID: ${paymentDetail.payment_id}")
        Log.d("PaymentViewModel", "User ID: ${paymentDetail.user_id}")
        Log.d("PaymentViewModel", "Card details: ${paymentDetail.cardNumber}, ${paymentDetail.expirationDate}, ${paymentDetail.cvc}")
        Log.d("PaymentViewModel", "Address details: ${paymentDetail.country}, ${paymentDetail.addressLine1}, ${paymentDetail.city}")
        
        return try {
            _isLoading.value = true
            _error.value = null
            val savedPaymentDetail = repository.savePaymentDetail(paymentDetail)
            Log.d("PaymentViewModel", "Successfully saved payment detail. Response: $savedPaymentDetail")
            val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
            currentList.add(savedPaymentDetail)
            _paymentDetails.value = currentList
            true
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "Error saving payment detail", e)
            _error.value = e.message ?: "Error al guardar los detalles del método de pago"
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun savePaymentDetailAndNavigate(paymentDetail: PaymentDetail, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val savedPaymentDetail = repository.savePaymentDetail(paymentDetail)
                Log.d("PaymentViewModel", "Successfully saved payment detail. Response: $savedPaymentDetail")
                val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
                currentList.add(savedPaymentDetail)
                _paymentDetails.value = currentList
                onSuccess()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error saving payment detail", e)
                _error.value = e.message ?: "Error al guardar los detalles del método de pago"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePaymentDetail(paymentDetailId: Long) {
        // Optimistically remove the payment detail from the UI
        val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
        val paymentDetailToRemove = currentList.find { it.id == paymentDetailId }
        
        if (paymentDetailToRemove != null) {
            // Store the current state in case we need to rollback
            val previousList = currentList.toList()
            
            // Update UI immediately
            currentList.remove(paymentDetailToRemove)
            _paymentDetails.value = currentList

            // Make the API call
            viewModelScope.launch {
                try {
                    _error.value = null
                    repository.deletePaymentDetail(paymentDetailId)
                    Log.d("PaymentViewModel", "Successfully deleted payment detail: $paymentDetailId")
                    // No necesitamos actualizar la UI aquí porque ya lo hicimos optimistamente
                } catch (e: Exception) {
                    // Si la eliminación falla, revertimos al estado anterior
                    Log.e("PaymentViewModel", "Error deleting payment detail", e)
                    _error.value = "Error al eliminar el método de pago. Por favor, intente nuevamente."
                    _paymentDetails.value = previousList
                }
            }
        }
    }
}
