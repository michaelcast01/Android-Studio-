package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.api.UserDetailApi
import com.example.tiendasuplementacion.model.UserDetail
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserDetailViewModel : ViewModel() {
    private val _userDetail = MutableLiveData<UserDetail>()
    val userDetail: LiveData<UserDetail> = _userDetail

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val api: UserDetailApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("YOUR_BASE_URL") // Replace with your actual base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(UserDetailApi::class.java)
    }

    fun fetchUserDetail(userId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = api.getUserDetail(userId)
                _userDetail.value = response
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al obtener los detalles del usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 