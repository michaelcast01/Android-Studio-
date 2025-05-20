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

    // Configuración de la URL base
    private const val BASE_URL = "http://10.0.2.2:8080/" // Asegúrate de que termine con /
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 2000L // 2 segundos entre intentos
    private const val TIMEOUT_SECONDS = 30L // 30 segundos de timeout

    // Configuración del interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configuración del cliente OkHttp con reintentos y logs
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            var retryCount = 0
            var lastException: Exception? = null
            val request = chain.request()
            while (retryCount < MAX_RETRIES) {
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful) {
                        return@addInterceptor response
                    } else {
                        val errorBody = response.body?.string() ?: "Sin cuerpo de error"
                        throw Exception("Error del servidor: ${response.code} - ${response.message}\n$errorBody")
                    }
                } catch (e: Exception) {
                    lastException = e
                    retryCount++
                    if (retryCount < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS)
                    }
                }
            }
            // En vez de lanzar excepción fatal, devuelve una respuesta falsa con código 599
            val errorMessage = "No se pudo conectar al servidor después de $MAX_RETRIES intentos.\nURL: $BASE_URL\nÚltimo error: ${lastException?.message}\nVerifica que:\n1. El servidor backend esté en ejecución\n2. El puerto 8080 esté abierto y accesible\n3. No haya un firewall bloqueando la conexión\n4. Si usas el emulador de Android, la URL debe ser 10.0.2.2\n5. Si usas un dispositivo físico, usa la IP real de tu computadora"
            return@addInterceptor okhttp3.Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(599)
                .message(errorMessage)
                .body(okhttp3.ResponseBody.create(null, errorMessage))
                .build()
        }
        .build()

    // Configuración de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicios de la API
    val userService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val productService: ProductApiService by lazy { retrofit.create(ProductApiService::class.java) }
    val categoryService: CategoryApiService by lazy { retrofit.create(CategoryApiService::class.java) }
    val orderService: OrderApiService by lazy { retrofit.create(OrderApiService::class.java) }
    val orderDetailService: OrderDetailApiService by lazy { retrofit.create(OrderDetailApiService::class.java) }
    val paymentService: PaymentApiService by lazy { retrofit.create(PaymentApiService::class.java) }
    val roleService: RoleApiService by lazy { retrofit.create(RoleApiService::class.java) }
    val settingService: SettingApiService by lazy { retrofit.create(SettingApiService::class.java) }
    val statusService: StatusApiService by lazy { retrofit.create(StatusApiService::class.java) }
    val categoryProductService: CategoryProductApiService by lazy { retrofit.create(CategoryProductApiService::class.java) }
    val orderProductService: OrderProductApiService by lazy { retrofit.create(OrderProductApiService::class.java) }
}
