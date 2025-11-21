package com.example.tiendasuplementacion.network

import android.content.Context
import com.example.tiendasuplementacion.BuildConfig
import com.example.tiendasuplementacion.interfaces.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    // Configuración de la URL base - PC en red local
    private const val BASE_URL = "https://suplements-v1.fly.dev/"
    private const val TIMEOUT_SECONDS = 15L // Reducido de 30 segundos a 15 segundos para redes rápidas
    private const val HTTP_CACHE_SIZE: Long = 10L * 1024L * 1024L // 10 MB

    // Interceptor de logging, controlado por BuildConfig.LOGGING_ENABLED
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.LOGGING_ENABLED) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    // Opcional: cache directory (debe inicializarse desde Application.onCreate)
    private var cacheDir: File? = null

    fun init(context: Context) {
        cacheDir = File(context.cacheDir, "http_cache")
    }

    // Construye el cliente OkHttp usando cache si está inicializado
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Añadir headers comunes para todas las requests
        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "TiendaSuplementacion-Android/1.0")
                .build()
            chain.proceed(newRequest)
        }

        // Añadir logging solo en DEBUG (definido arriba)
        builder.addInterceptor(loggingInterceptor)

        // Si se inicializó cacheDir en Application, úsala
        cacheDir?.let { dir ->
            try {
                val cache = Cache(dir, HTTP_CACHE_SIZE)
                builder.cache(cache)
            } catch (ignored: Exception) {
                // Si falla la creación del cache, seguimos sin cache pero no rompemos la app
            }
        }

        builder.build()
    }

    // Configuraci9n de Retrofit
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
    val emailVerificationService: EmailVerificationService by lazy { retrofit.create(EmailVerificationService::class.java) }
}
