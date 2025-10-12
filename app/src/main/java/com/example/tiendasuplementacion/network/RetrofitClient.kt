package com.example.tiendasuplementacion.network

import android.content.Context
import com.example.tiendasuplementacion.BuildConfig
import com.example.tiendasuplementacion.interfaces.*
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    // Configuración de la URL base (dev por defecto)
    private const val BASE_URL = "http://10.0.2.2:8080/" // Asegúrate de que termine con /
    private const val TIMEOUT_SECONDS = 15L // Reducido de 30 segundos a 15 segundos para redes rápidas
    private const val HTTP_CACHE_SIZE: Long = 20L * 1024L * 1024L // Aumentado a 20 MB
    private const val CACHE_MAX_AGE = 5 * 60 // 5 minutos para datos frescos
    private const val CACHE_MAX_STALE = 60 * 60 * 24 * 7 // 7 días para datos obsoletos

    // Interceptor de logging, controlado por BuildConfig.LOGGING_ENABLED
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.LOGGING_ENABLED) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    // Cache de conexiones reutilizable
    private val cacheInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(CACHE_MAX_AGE, TimeUnit.SECONDS)
            .build()
        response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }

    // Interceptor para network offline
    private val offlineCacheInterceptor = Interceptor { chain ->
        var request = chain.request()
        if (!isNetworkAvailable) {
            val cacheControl = CacheControl.Builder()
                .maxStale(CACHE_MAX_STALE, TimeUnit.SECONDS)
                .onlyIfCached()
                .build()
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        chain.proceed(request)
    }

    // Variable para controlar disponibilidad de red (puede ser actualizada desde NetworkUtils)
    private var isNetworkAvailable = true

    // Opcional: cache directory (debe inicializarse desde Application.onCreate)
    private var cacheDir: File? = null

    fun init(context: Context) {
        cacheDir = File(context.cacheDir, "http_cache")
    }

    fun updateNetworkAvailability(available: Boolean) {
        isNetworkAvailable = available
    }

    // Construye el cliente OkHttp usando cache si está inicializado
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(5, 10, TimeUnit.MINUTES)) // Pool optimizado

        // Añadir interceptors de cache
        builder.addNetworkInterceptor(cacheInterceptor)
        builder.addInterceptor(offlineCacheInterceptor)

        // Añadir logging solo en DEBUG (definido arriba)
        if (BuildConfig.LOGGING_ENABLED) {
            builder.addInterceptor(loggingInterceptor)
        }

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

    // Configuración de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicios de la API (lazy initialization para mejor performance)
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