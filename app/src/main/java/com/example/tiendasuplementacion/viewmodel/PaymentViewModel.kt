package com.example.tiendasuplementacion.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _paymentDetails = MutableStateFlow<List<PaymentDetail>>(emptyList())
    val paymentDetails: StateFlow<List<PaymentDetail>> = _paymentDetails.asStateFlow()

    private val _paymentDetailsUiState = MutableStateFlow<UiState<List<PaymentDetail>>>(UiState.Loading)
    val paymentDetailsUiState: StateFlow<UiState<List<PaymentDetail>>> = _paymentDetailsUiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // One-shot UI events (snackbars, navigation, etc.)
    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

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
                val currentList = _payments.value.toMutableList()
                currentList.add(createdPayment)
                _payments.value = currentList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear el método de pago"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun update(id: Long, payment: Payment) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val updatedPayment = repository.update(id, payment)
                val currentList = _payments.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == id }
                if (index != -1) {
                    currentList[index] = updatedPayment
                    _payments.value = currentList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar el método de pago"
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
                _paymentDetailsUiState.value = UiState.Loading
                val details = repository.getPaymentDetails(userId)
                Log.d("PaymentViewModel", "Received payment details count=${details.size}")
                _paymentDetails.value = details
                _paymentDetailsUiState.value = UiState.Success(details)
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error fetching payment details", e)
                val msg = e.message ?: "Error al cargar los detalles de pago"
                _error.value = msg
                _events.emit(UiEvent.ShowError(msg))
                _paymentDetails.value = emptyList()
                _paymentDetailsUiState.value = UiState.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getPaymentById(paymentId: Long): Payment? {
        return payments.value.find { it.id == paymentId }
    }

    // A small in-memory cache to hold the last selected PaymentDetail so we can avoid
    // re-fetching immediately after the user selects a payment method and we navigate.
    private val _selectedPaymentDetail = MutableStateFlow<PaymentDetail?>(null)
    val selectedPaymentDetail: StateFlow<PaymentDetail?> = _selectedPaymentDetail.asStateFlow()

    fun setSelectedPaymentDetail(paymentDetail: PaymentDetail?) {
        _selectedPaymentDetail.value = paymentDetail
    }

    fun clearSelectedPaymentDetail() {
        _selectedPaymentDetail.value = null
    }

    suspend fun savePaymentDetail(paymentDetail: PaymentDetail): Boolean {
    Log.d("PaymentViewModel", "Attempting to save payment detail for user=${paymentDetail.user_id} payment=${paymentDetail.payment_id}")
        
        return try {
            _isLoading.value = true
            _error.value = null
            val savedPaymentDetail = repository.savePaymentDetail(paymentDetail)
            Log.d("PaymentViewModel", "Successfully saved payment detail. Response: $savedPaymentDetail")
            val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
            currentList.add(savedPaymentDetail)
            _paymentDetails.value = currentList
            _paymentDetailsUiState.value = UiState.Success(currentList)
            _events.emit(UiEvent.ShowSnackbar("Método de pago guardado"))
            true
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "Error saving payment detail", e)
            _error.value = e.message ?: "Error al guardar los detalles del método de pago"
            _paymentDetailsUiState.value = UiState.Error(_error.value ?: "Error al guardar los detalles del método de pago")
            _events.emit(UiEvent.ShowError(_error.value ?: "Error al guardar los detalles del método de pago"))
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
                    _paymentDetailsUiState.value = UiState.Success(currentList)
                // Emit navigation event and snackbar
                    _events.emit(UiEvent.ShowSnackbar("Método de pago guardado (navigate back)"))
                    _events.emit(UiEvent.NavigateBack)
                onSuccess()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error saving payment detail", e)
                _error.value = e.message ?: "Error al guardar los detalles del método de pago"
                    _paymentDetailsUiState.value = UiState.Error(_error.value ?: "Error al guardar los detalles del método de pago")
                    _events.emit(UiEvent.ShowError(_error.value ?: "Error al guardar los detalles del método de pago"))
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
                } catch (e: Exception) {
                    // Si la eliminación falla, revertimos al estado anterior
                    Log.e("PaymentViewModel", "Error deleting payment detail", e)
                    _error.value = "Error al eliminar el método de pago. Por favor, intente nuevamente."
                    _paymentDetails.value = previousList
                }
            }
        }
    }

    fun updatePaymentDetail(paymentDetail: PaymentDetail) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Guardamos el estado actual para posible rollback
                val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
                val previousList = currentList.toList()

                // Actualizamos optimistamente la UI
                val index = currentList.indexOfFirst { it.id == paymentDetail.id }
                if (index != -1) {
                    currentList[index] = paymentDetail
                    _paymentDetails.value = currentList

                    // Hacemos la llamada al API
                    val updatedPayment = repository.updatePaymentDetail(paymentDetail.id, paymentDetail)
                    Log.d("PaymentViewModel", "Successfully updated payment detail: ${updatedPayment.id}")
                } else {
                    throw Exception("No se encontró el método de pago a actualizar")
                }
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error updating payment detail", e)
                _error.value = "Error al actualizar el método de pago. Por favor, intente nuevamente."
                
                // Revertimos los cambios en caso de error
                _paymentDetails.value?.let { currentList ->
                    val index = currentList.indexOfFirst { it.id == paymentDetail.id }
                    if (index != -1) {
                        val revertedList = currentList.toMutableList()
                        revertedList[index] = _paymentDetails.value!![index]
                        _paymentDetails.value = revertedList
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
