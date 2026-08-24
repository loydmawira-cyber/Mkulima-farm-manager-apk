package com.example

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class MkulimaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB persistent disk cache
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .crossfade(200) // Smooth crossfade placeholder transition for pleasant perceived performance
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .allowHardware(true)
            .respectCacheHeaders(false)
            .build()
    }
}
