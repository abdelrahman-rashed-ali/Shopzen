package shopzen.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.HiltAndroidApp
import shopzen.app.BuildConfig

@HiltAndroidApp
class ShopzenApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    }
}
