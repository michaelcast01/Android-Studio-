package com.example.tiendasuplementacion.api

import com.example.tiendasuplementacion.model.UserDetail
import retrofit2.http.GET
import retrofit2.http.Path

interface UserDetailApi {
    @GET("user-detail/{userId}")
    suspend fun getUserDetail(@Path("userId") userId: Long): UserDetail
} 