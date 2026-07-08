package shopzen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import shopzen.app.BuildConfig
import shopzen.app.navigation.AppNavHost
import shopzen.app.theme.AppThemeController
import shopzen.app.theme.LocaleControllerEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                    launchGoogleSignIn = { onToken, onError ->
                        coroutineScope.launch {
                            runCatching {
                                val option = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                    .build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(option)
                                    .build()
                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = this@MainActivity,
                                )
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                onToken(googleIdTokenCredential.idToken)
                            }.onFailure { e ->
                                onError(e.message ?: "Google sign-in failed")
                            }
                        }
                    },
                    launchAppleSignIn = { onSuccess, onError -> })
            }
        }
    }
}
