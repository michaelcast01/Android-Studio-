package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.UserDetail
import com.example.tiendasuplementacion.repository.UserDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.ZoneOffset

class UserDetailViewModel : ViewModel() {
    private val repository = UserDetailRepository()

    private val _userDetail = MutableStateFlow<UserDetail?>(null)
    val userDetail: StateFlow<UserDetail?> = _userDetail

    private val _userDetailsList = MutableStateFlow<List<UserDetail>>(emptyList())
    val userDetailsList: StateFlow<List<UserDetail>> = _userDetailsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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

    private fun parseOrderDateOrFallback(dateStr: String?, orderId: Long): Instant {
        if (dateStr.isNullOrBlank()) return Instant.ofEpochMilli(orderId)
        // Intentar parse ISO primero
        val candidates = listOf(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        for (fmt in candidates) {
            try {
                val temporal = fmt.parse(dateStr)
                return Instant.from(temporal)
            } catch (_: DateTimeParseException) {
                // intentar siguiente
            }
        }
        // Ultimo recurso: intentar parsear como epoch millis
        return try {
            Instant.ofEpochMilli(dateStr.toLong())
        } catch (_: Exception) {
            // Fallback: usar orderId como semilla para mantener orden estable
            Instant.ofEpochMilli(orderId)
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