package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.network.RetrofitClient
import com.example.tiendasuplementacion.viewmodel.EmailVerificationRequest

class EmailVerificationRepository {
    private val service = RetrofitClient.emailVerificationService

    suspend fun getVerification(verificationId: String) = service.getVerificationStatus(verificationId)
    suspend fun create(request: EmailVerificationRequest) = service.startEmailVerification(request)

}
