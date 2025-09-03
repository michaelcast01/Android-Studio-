package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.Role
import retrofit2.http.*

interface RoleApiService {
    @GET("/api/roles")
    suspend fun getAll(): List<Role>

    @GET("/api/roles/{id}")
    suspend fun getById(@Path("id") id: Long): Role

    @POST("/api/roles")
    suspend fun create(@Body role: Role): Role

    @PUT("/api/roles/{id}")
    suspend fun update(@Path("id") id: Long, @Body role: Role): Role

    @DELETE("/api/roles/{id}")
    suspend fun delete(@Path("id") id: Long)
}
