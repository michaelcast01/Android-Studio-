package com.example.tiendasuplementacion.network

import android.util.Log
import com.example.tiendasuplementacion.interfaces.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    private const val BASE_URL = "http://10.0.2.2:8080" // Cambia si es API externa o en físico
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L
    private const val TIMEOUT_SECONDS = 15L // Reducido de 30 a 15 segundos

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            var retryCount = 0
            var lastException: Exception? = null

            Log.d(TAG, "Intentando conectar a $BASE_URL")

            while (retryCount < MAX_RETRIES) {
                try {
                    Log.d(TAG, "Intento ${retryCount + 1} de $MAX_RETRIES")
                    return@addInterceptor chain.proceed(chain.request())
                } catch (e: Exception) {
                    lastException = e
                    retryCount++
                    Log.e(TAG, "Error en intento $retryCount: ${e.message}")
                    if (retryCount < MAX_RETRIES) {
                        Log.d(TAG, "Esperando $RETRY_DELAY_MS ms antes del siguiente intento")
                        Thread.sleep(RETRY_DELAY_MS)
                    }
                }
            }

            val errorMessage = "No se pudo conectar al servidor después de $MAX_RETRIES intentos. " +
                "Asegúrate de que el servidor esté corriendo en $BASE_URL. " +
                "Error: ${lastException?.message}"
            Log.e(TAG, errorMessage)
            throw Exception(errorMessage)
        }
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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
