package shopzen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.OAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import shopzen.app.navigation.AppNavHost
import shopzen.app.theme.AppThemeController
import shopzen.app.theme.LocaleControllerEffect
import shopzen.presentation.theme.MyApplicationTheme
import shopzen.presentation.theme.ShopzenTheme

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

            MyApplicationTheme {
                AppNavHost(
                    navController = navController,
                    launchGoogleSignIn = { onToken, onError -> },
                    launchAppleSignIn = { onSuccess, onError -> })
            }
        }
    }
}