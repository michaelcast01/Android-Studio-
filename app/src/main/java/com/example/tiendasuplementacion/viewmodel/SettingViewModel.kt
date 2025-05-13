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
            _settings.value = repository.getAll()
        }
    }

    fun createSetting(setting: Setting) {
        viewModelScope.launch {
            repository.create(setting)
            fetchSettings()
        }
    }
}
