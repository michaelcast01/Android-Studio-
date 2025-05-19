package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.SettingDetail
import retrofit2.http.*

interface SettingApiService {
    @GET("/api/settings")
    suspend fun getAll(): List<Setting>

    @GET("/api/settings/{id}")
    suspend fun getById(@Path("id") id: Long): Setting

    @GET("/api/settings-details/{id}")
    suspend fun getDetailsById(@Path("id") id: Long): SettingDetail

    @POST("/api/settings")
    suspend fun create(@Body setting: Setting): Setting

    @PUT("/api/settings/{id}")
    suspend fun update(@Path("id") id: Long, @Body setting: Setting): Setting

    @DELETE("/api/settings/{id}")
    suspend fun delete(@Path("id") id: Long)
}
