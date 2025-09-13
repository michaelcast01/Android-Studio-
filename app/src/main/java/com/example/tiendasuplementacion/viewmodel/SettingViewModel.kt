package com.example.tiendasuplementacion.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.SettingDetail
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.repository.SettingRepository
import com.example.tiendasuplementacion.util.EnvConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.*

data class EmailVerificationRequest(
    val email: String,
    val callback: CallbackConfig
)

data class CallbackConfig(
    val url: String,
    val method: String = "POST",
    val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json",
        "Authorization" to "Bearer your_app_token"
    )
)

data class EmailVerificationResponse(
    val verificationId: String,
    val status: String,
    val message: String,
    val email: String
)

data class VerificationStatusResponse(
    val verificationId: String,
    val emailAddress: String,
    val status: String,
    val emailStatus: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val verifiedAt: String?
)

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

    // Función para iniciar verificación de email
    suspend fun startEmailVerification(email: String, callbackUrl: String): EmailVerificationResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = EmailVerificationRequest(
                    email = email,
                    callback = CallbackConfig(
                        url = callbackUrl,
                        method = "POST",
                        headers = mapOf(
                            "Content-Type" to "application/json",
                            "Authorization" to "Bearer ${EnvConfig.get("EMAIL_API_KEY", "default_token")}",
                            "X-App-Source" to "TiendaSuplementacion"
                        )
                    )
                )

                // Aquí harías la llamada real a tu API
                // val response = apiService.startEmailVerification(request)

                // Simulación de respuesta por ahora
                EmailVerificationResponse(
                    verificationId = "c1003800-3e08-418b-b43b-b96cf82b3b80",
                    status = "PENDING",
                    message = "Email verification initiated. You will receive a callback when completed.",
                    email = email
                )

            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error starting email verification", e)
                throw e
            }
        }
    }

    // Función para verificar el estado de verificación
    suspend fun checkVerificationStatus(verificationId: String): VerificationStatusResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Aquí harías la llamada real a tu API
                // val response = apiService.getVerificationStatus(verificationId)

                // Simulación de respuesta que cambia con el tiempo
                val emailStatuses = listOf("VALID", "INVALID", "UNKNOWN")
                val randomStatus = emailStatuses.random()

                VerificationStatusResponse(
                    verificationId = verificationId,
                    emailAddress = "user@example.com",
                    status = "COMPLETED",
                    emailStatus = randomStatus,
                    createdAt = "2024-01-15T10:30:00",
                    updatedAt = "2024-01-15T10:32:00",
                    verifiedAt = "2024-01-15T10:32:00"
                )

            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error checking verification status", e)
                throw e
            }
        }
    }
}
