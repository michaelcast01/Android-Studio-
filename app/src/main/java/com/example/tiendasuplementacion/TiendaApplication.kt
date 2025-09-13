package com.example.tiendasuplementacion

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.tiendasuplementacion.network.RetrofitClient
import com.example.tiendasuplementacion.util.EnvConfig

class TiendaApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        EnvConfig.initialize(this)
        EnvConfig.logAllVariables()
        
        RetrofitClient.init(this)
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }
}