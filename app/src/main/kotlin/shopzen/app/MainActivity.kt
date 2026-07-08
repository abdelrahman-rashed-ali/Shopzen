package shopzen.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import shopzen.app.navigation.AppNavHost
import shopzen.app.theme.AppThemeController
import shopzen.app.theme.LocaleControllerEffect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val credentialManager =
                remember { CredentialManager.create(this@MainActivity.applicationContext) }
            val coroutineScope = rememberCoroutineScope()

            AppThemeController {
                LocaleControllerEffect()
                AppNavHost(
                    navController = navController,
                    launchGoogleSignIn = { onToken, onError -> },
                    launchAppleSignIn = { onSuccess, onError -> })
            }
        }
    }
}
