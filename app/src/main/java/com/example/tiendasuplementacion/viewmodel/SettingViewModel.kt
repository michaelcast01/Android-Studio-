package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.SettingDetail
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.repository.SettingRepository
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {
    private val repository = SettingRepository()
    private val _settings = MutableLiveData<List<Setting>>()
    val settings: LiveData<List<Setting>> = _settings

    private val _settingDetail = MutableLiveData<SettingDetail>()
    val settingDetail: LiveData<SettingDetail> = _settingDetail

    private val _availablePayments = MutableLiveData<List<Payment>>()
    val availablePayments: LiveData<List<Payment>> = _availablePayments

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchSettings() {
        viewModelScope.launch {
            try {
                _settings.value = repository.getAll()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchSettingDetails(id: Long) {
        viewModelScope.launch {
            try {
                _settingDetail.value = repository.getDetailsById(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun fetchAvailablePaymentMethods() {
        viewModelScope.launch {
            try {
                _availablePayments.value = repository.getAvailablePaymentMethods()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addPaymentMethod(paymentId: Long) {
        viewModelScope.launch {
            try {
                val settingId = _settingDetail.value?.id ?: return@launch
                repository.addPaymentMethod(settingId, paymentId)
                // Actualizar los detalles después de agregar el método de pago
                fetchSettingDetails(settingId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    suspend fun createSetting(setting: Setting): Setting {
        return try {
            repository.create(setting)
        } catch (e: Exception) {
            throw Exception("Error al crear la configuración: ${e.message}")
        }
    }
}
