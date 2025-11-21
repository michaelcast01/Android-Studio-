package com.example.tiendasuplementacion.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.repository.PaymentRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * ViewModel optimizado para gestión de pagos con caché y estados mejorados
 */
class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepository()
    
    // LiveData para métodos de pago
    private val _payments = MutableLiveData<List<Payment>>()
    val payments: LiveData<List<Payment>> = _payments

    // LiveData para detalles de pago
    private val _paymentDetails = MutableLiveData<List<PaymentDetail>>()
    val paymentDetails: LiveData<List<PaymentDetail>> = _paymentDetails

    // StateFlow para estado UI de detalles de pago
    private val _paymentDetailsUiState = MutableStateFlow<UiState<List<PaymentDetail>>>(UiState.Loading)
    val paymentDetailsUiState: StateFlow<UiState<List<PaymentDetail>>> = _paymentDetailsUiState.asStateFlow()

    // Estados de carga y error
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Caché de pagos para evitar llamadas innecesarias
    private var paymentsCache: List<Payment>? = null
    private var paymentsCacheTimestamp: Long = 0
    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutos

    // Estado de operaciones exitosas
    private val _operationSuccess = MutableStateFlow<OperationResult?>(null)
    val operationSuccess: StateFlow<OperationResult?> = _operationSuccess.asStateFlow()

    init {
        fetchPayments()
    }

    /**
     * Resultado de operaciones
     */
    data class OperationResult(
        val type: OperationType,
        val success: Boolean,
        val message: String? = null
    )

    enum class OperationType {
        CREATE, UPDATE, DELETE, SAVE_DETAIL
    }

    /**
     * Verifica si el caché es válido
     */
    private fun isCacheValid(): Boolean {
        return paymentsCache != null && 
               (System.currentTimeMillis() - paymentsCacheTimestamp) < CACHE_VALIDITY_MS
    }

    /**
     * Invalida el caché
     */
    fun invalidateCache() {
        paymentsCache = null
        paymentsCacheTimestamp = 0
    }

    /**
     * Obtiene todos los métodos de pago disponibles con caché
     */
    fun fetchPayments(forceRefresh: Boolean = false) {
        // Si tenemos caché válido y no se fuerza el refresh, usarlo
        if (!forceRefresh && isCacheValid()) {
            _payments.value = paymentsCache
            Log.d("PaymentViewModel", "Usando caché de pagos")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val fetchedPayments = repository.getAll()
                
                // Actualizar caché
                paymentsCache = fetchedPayments
                paymentsCacheTimestamp = System.currentTimeMillis()
                
                _payments.value = fetchedPayments
                Log.d("PaymentViewModel", "Pagos cargados: ${fetchedPayments.size}")
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error al cargar pagos", e)
                _error.value = e.message ?: "Error al cargar los métodos de pago"
                _payments.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea un nuevo método de pago con validación
     */
    fun createPayment(payment: Payment, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Validación básica
                if (payment.name.isBlank()) {
                    throw IllegalArgumentException("El nombre del método de pago no puede estar vacío")
                }
                
                val createdPayment = repository.create(payment)
                
                // Actualización optimista
                val currentList = _payments.value?.toMutableList() ?: mutableListOf()
                currentList.add(createdPayment)
                _payments.value = currentList
                
                // Invalidar caché para forzar recarga
                invalidateCache()
                
                _operationSuccess.value = OperationResult(
                    type = OperationType.CREATE,
                    success = true,
                    message = "Método de pago creado exitosamente"
                )
                
                Log.d("PaymentViewModel", "Método de pago creado: ${createdPayment.name}")
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error al crear método de pago", e)
                _error.value = e.message ?: "Error al crear el método de pago"
                _operationSuccess.value = OperationResult(
                    type = OperationType.CREATE,
                    success = false,
                    message = e.message
                )
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
                val currentList = _payments.value?.toMutableList() ?: mutableListOf()
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
                Log.d("PaymentViewModel", "Received payment details: $details")
                _paymentDetails.value = details
                _paymentDetailsUiState.value = UiState.Success(details)
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error fetching payment details", e)
                val msg = e.message ?: "Error al cargar los detalles de pago"
                _error.value = msg
                _paymentDetails.value = emptyList()
                _paymentDetailsUiState.value = UiState.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpia el resultado de la operación
     */
    fun clearOperationResult() {
        _operationSuccess.value = null
    }

    /**
     * Obtiene un método de pago por su ID
     */
    fun getPaymentById(paymentId: Long): Payment? {
        return payments.value?.find { it.id == paymentId }
    }

    /**
     * Obtiene métodos de pago activos
     */
    fun getActivePayments(): List<Payment> {
        return payments.value?.filter { it.isActive } ?: emptyList()
    }

    /**
     * Caché del detalle de pago seleccionado para evitar re-fetcheos innecesarios
     */
    private val _selectedPaymentDetail = MutableLiveData<PaymentDetail?>()
    val selectedPaymentDetail: LiveData<PaymentDetail?> = _selectedPaymentDetail

    /**
     * Establece el detalle de pago seleccionado
     */
    fun setSelectedPaymentDetail(paymentDetail: PaymentDetail?) {
        _selectedPaymentDetail.value = paymentDetail
        Log.d("PaymentViewModel", "Selected payment detail set: ${paymentDetail?.id}")
    }

    /**
     * Limpia el detalle de pago seleccionado
     */
    fun clearSelectedPaymentDetail() {
        _selectedPaymentDetail.value = null
    }

    /**
     * Guarda los detalles de pago con validación mejorada
     */
    suspend fun savePaymentDetail(paymentDetail: PaymentDetail): Boolean {
        Log.d("PaymentViewModel", "Attempting to save payment detail: $paymentDetail")
        Log.d("PaymentViewModel", "Payment ID: ${paymentDetail.payment_id}")
        Log.d("PaymentViewModel", "User ID: ${paymentDetail.user_id}")
        
        return try {
            _isLoading.value = true
            _error.value = null
            
            // Validación de campos requeridos
            validatePaymentDetail(paymentDetail)
            
            // Log de detalles sensibles solo en debug
            if (paymentDetail.cardNumber != null) {
                Log.d("PaymentViewModel", "Card ending in: ${paymentDetail.cardNumber?.takeLast(4)}")
            }
            Log.d("PaymentViewModel", "Address: ${paymentDetail.country}, ${paymentDetail.city}")
            
            val savedPaymentDetail = repository.savePaymentDetail(paymentDetail)
            Log.d("PaymentViewModel", "Successfully saved payment detail. ID: ${savedPaymentDetail.id}")
            
            // Actualización optimista
            val currentList = _paymentDetails.value?.toMutableList() ?: mutableListOf()
            currentList.add(savedPaymentDetail)
            _paymentDetails.value = currentList
            _paymentDetailsUiState.value = UiState.Success(currentList)
            
            _operationSuccess.value = OperationResult(
                type = OperationType.SAVE_DETAIL,
                success = true,
                message = "Método de pago configurado exitosamente"
            )
            
            true
        } catch (e: IllegalArgumentException) {
            Log.e("PaymentViewModel", "Validation error", e)
            _error.value = e.message
            _paymentDetailsUiState.value = UiState.Error(e.message ?: "Error de validación")
            false
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "Error saving payment detail", e)
            _error.value = e.message ?: "Error al guardar los detalles del método de pago"
            _paymentDetailsUiState.value = UiState.Error(_error.value ?: "Error al guardar")
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Valida los detalles de pago antes de guardar
     */
    private fun validatePaymentDetail(paymentDetail: PaymentDetail) {
        require(paymentDetail.payment_id > 0) { "ID de pago inválido" }
        require(paymentDetail.user_id > 0) { "ID de usuario inválido" }
        require(!paymentDetail.country.isNullOrBlank()) { "El país es requerido" }
        require(!paymentDetail.addressLine1.isNullOrBlank()) { "La dirección es requerida" }
        require(!paymentDetail.city.isNullOrBlank()) { "La ciudad es requerida" }
        require(!paymentDetail.stateOrProvince.isNullOrBlank()) { "El estado/provincia es requerido" }
        require(!paymentDetail.postalCode.isNullOrBlank()) { "El código postal es requerido" }
        
        // Si el pago requiere tarjeta, validar campos de tarjeta
        if (paymentDetail.payment.requiresCardInfo()) {
            require(!paymentDetail.cardNumber.isNullOrBlank()) { "El número de tarjeta es requerido" }
            require(!paymentDetail.expirationDate.isNullOrBlank()) { "La fecha de expiración es requerida" }
            require(!paymentDetail.cvc.isNullOrBlank()) { "El CVC es requerido" }
            require(!paymentDetail.cardholderName.isNullOrBlank()) { "El nombre del titular es requerido" }
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
                onSuccess()
            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error saving payment detail", e)
                _error.value = e.message ?: "Error al guardar los detalles del método de pago"
                    _paymentDetailsUiState.value = UiState.Error(_error.value ?: "Error al guardar los detalles del método de pago")
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
