package com.example.tiendasuplementacion.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.SettingDetail
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.repository.SettingRepository
import com.example.tiendasuplementacion.util.EnvConfig
import com.example.tiendasuplementacion.network.RetrofitClient
import com.example.tiendasuplementacion.interfaces.EmailVerificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val verification_id: String,
    val status: String,
    val message: String,
    val email: String
)

data class VerificationStatusResponse(
    val verification_id: String,
    val email_address: String,
    val status: String,
    val email_status: String?,
    val created_at: String?,
    val updated_at: String?,
    val verified_at: String?
)

class SettingViewModel : ViewModel() {
    private val repository = SettingRepository()
    private val emailVerificationService = RetrofitClient.emailVerificationService

    private val _settings = MutableLiveData<List<Setting>>()
    val settings: LiveData<List<Setting>> = _settings

    private val _settingDetail = MutableLiveData<SettingDetail>()
    val settingDetail: LiveData<SettingDetail> = _settingDetail

    private val _availablePayments = MutableLiveData<List<Payment>>()
    val availablePayments: LiveData<List<Payment>> = _availablePayments

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _apiInfo = MutableLiveData<EmailVerificationService>()
    val apiInfo: LiveData<EmailVerificationService> = _apiInfo

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
                fetchSettingDetails(settingId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateSetting(
        settingId: Long,
        name: String,
        nickname: String,
        phone: Long,
        city: String,
        address: String,
        paymentId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedSetting = Setting(
                    id = settingId,
                    payment_id = paymentId,
                    name = name,
                    nickname = nickname,
                    phone = phone,
                    city = city,
                    address = address
                )
                repository.update(settingId, updatedSetting)
                fetchSettingDetails(settingId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                onError(e.message ?: "Error desconocido al actualizar el perfil")
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

    suspend fun startEmailVerification(email: String, callbackUrl: String): EmailVerificationResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = EmailVerificationRequest(
                    email = email,
                    callback = CallbackConfig(
                        url = "http://localhost:8080/api/email-verification/callback",
                        method = "POST",
                        headers = mapOf(
                            "authorization" to "3a71KlUgY833X5vBkHOsM6YjgPfbqZ6d4HNiBn7q",
                            "content-type" to "application/json"
                        )
                    )
                )

                val response = emailVerificationService.startEmailVerification(request)
                Log.d("SettingViewModel", "Email verification started: ${response.verification_id}")
                response

            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error starting email verification", e)
                throw Exception("Error al iniciar verificación de email: ${e.message}")
            }
        }
    }

    suspend fun checkVerificationStatus(verificationId: String): VerificationStatusResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = emailVerificationService.getVerificationStatus(verificationId)
                Log.d("SettingViewModel", "Verification status: ${response.status} - ${response.email_status}")
                response

            } catch (e: Exception) {
                Log.e("SettingViewModel", "Error checking verification status", e)
                throw Exception("Error al verificar estado: ${e.message}")
            }
        }
    }

}

