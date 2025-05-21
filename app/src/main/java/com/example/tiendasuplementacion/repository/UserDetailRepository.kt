package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.UserDetail
import com.example.tiendasuplementacion.network.RetrofitClient

class UserDetailRepository {
    private val service = RetrofitClient.userService

    suspend fun getUserDetails(id: Long): UserDetail {
        return service.getUserDetails(id)
    }

    suspend fun getUserDetailsByRole(roleId: Long): List<UserDetail> {
        return service.getUserDetailsByRole(roleId)
    }
} 