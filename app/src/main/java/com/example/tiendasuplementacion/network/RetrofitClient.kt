package com.example.tiendasuplementacion.network

import com.example.tiendasuplementacion.interfaces.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080" // Cambia si es API externa o en físico

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val productService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    val categoryService: CategoryApiService by lazy {
        retrofit.create(CategoryApiService::class.java)
    }

    val orderService: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    val orderDetailService: OrderDetailApiService by lazy {
        retrofit.create(OrderDetailApiService::class.java)
    }

    val paymentService: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }

    val roleService: RoleApiService by lazy {
        retrofit.create(RoleApiService::class.java)
    }

    val settingService: SettingApiService by lazy {
        retrofit.create(SettingApiService::class.java)
    }

    val statusService: StatusApiService by lazy {
        retrofit.create(StatusApiService::class.java)
    }

    val categoryProductService: CategoryProductApiService by lazy {
        retrofit.create(CategoryProductApiService::class.java)
    }
}
