package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.viewmodel.EmailVerificationRequest
import com.example.tiendasuplementacion.viewmodel.EmailVerificationResponse
import com.example.tiendasuplementacion.viewmodel.VerificationStatusResponse
import retrofit2.http.*
import com.example.tiendasuplementacion.model.EmailVerification

interface EmailVerificationService {
    
    @POST("api/email-verification/verify")
    suspend fun startEmailVerification(
        @Body request: EmailVerificationRequest
    ): EmailVerificationResponse
    
    @GET("api/email-verification/status/{verificationId}")
    suspend fun getVerificationStatus(
        @Path("verificationId") verificationId: String
    ): VerificationStatusResponse
    
    @GET("api/email-verification/info")
    suspend fun getApiInfo(): EmailVerification
}