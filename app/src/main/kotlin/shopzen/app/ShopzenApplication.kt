package shopzen.app

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.google.firebase.FirebaseApp
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class ShopzenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN

        // Configure Coil with a robust ImageLoader — crossfade, disk & memory cache
        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .crossfade(300)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20) // 20% of app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }
}
