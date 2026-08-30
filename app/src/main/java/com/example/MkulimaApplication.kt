package com.example

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class MkulimaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        initializeFirestoreOfflinePersistence()
    }

    private fun initializeFirestoreOfflinePersistence() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            val db = FirebaseFirestore.getInstance()
            val cacheSettings = PersistentCacheSettings.newBuilder()
                .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            val settings = FirebaseFirestoreSettings.Builder(db.firestoreSettings)
                .setLocalCacheSettings(cacheSettings)
                .build()
            db.firestoreSettings = settings
            Log.d("MkulimaApplication", "Firestore offline persistence configured successfully with unlimited cache.")
        } catch (e: Throwable) {
            Log.w("MkulimaApplication", "Could not configure Firestore offline persistence: ${e.message}")
        }
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
                    .maxSizePercent(0.05)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .crossfade(150)
            .respectCacheHeaders(false)
            .build()
    }
}
