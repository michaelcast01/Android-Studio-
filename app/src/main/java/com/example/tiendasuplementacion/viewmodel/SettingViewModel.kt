package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Setting
import com.example.tiendasuplementacion.repository.SettingRepository
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {
    private val repository = SettingRepository()
    private val _settings = MutableLiveData<List<Setting>>()
    val settings: LiveData<List<Setting>> = _settings

    fun fetchSettings() {
        viewModelScope.launch {
            try {
                _settings.value = repository.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
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
