package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.UserDetail
import com.example.tiendasuplementacion.repository.UserDetailRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class UserDetailViewModel : ViewModel() {
    private val repository = UserDetailRepository()
    
    private val _userDetail = MutableLiveData<UserDetail>()
    val userDetail: LiveData<UserDetail> = _userDetail

    private val _userDetailsList = MutableLiveData<List<UserDetail>>()
    val userDetailsList: LiveData<List<UserDetail>> = _userDetailsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchUserDetails(id: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val fetched = repository.getUserDetails(id)
                // Ordenar pedidos por fecha descendente (más reciente primero). Si no se puede parsear, fallback por order_id.
                val sortedOrders = fetched.orders.sortedWith(compareByDescending { o ->
                    parseOrderDateOrFallback(o.date_order, o.order_id)
                })
                _userDetail.value = fetched.copy(orders = sortedOrders)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los detalles del usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseOrderDateOrFallback(dateStr: String?, orderId: Long): Long {
        if (dateStr.isNullOrBlank()) return orderId
        
        // Formatos de fecha a intentar
        val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US), // ISO con microsegundos
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),         // ISO básico
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),           // ISO con zona
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),              // Formato simple
            SimpleDateFormat("yyyy-MM-dd", Locale.US)                        // Solo fecha
        )
        
        for (format in dateFormats) {
            try {
                val date = format.parse(dateStr)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
                // Intentar siguiente formato
            }
        }
        
        // Último recurso: intentar parsear como epoch millis
        return try {
            dateStr.toLong()
        } catch (_: Exception) {
            // Fallback: usar orderId para mantener orden estable
            orderId
        }
    }

    fun fetchUserDetailsByRole(roleId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _userDetailsList.value = repository.getUserDetailsByRole(roleId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los detalles de los usuarios"
                _userDetailsList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 