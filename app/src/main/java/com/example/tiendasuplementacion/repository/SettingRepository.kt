package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.model.SettingDetail
import com.example.tiendasuplementacion.network.RetrofitClient

class SettingRepository {
    private val service = RetrofitClient.settingService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun getDetailsById(id: Long) = service.getDetailsById(id)
    suspend fun create(setting: Setting) = service.create(setting)
    suspend fun update(id: Long, setting: Setting) = service.update(id, setting)
    suspend fun delete(id: Long) = service.delete(id)
}
