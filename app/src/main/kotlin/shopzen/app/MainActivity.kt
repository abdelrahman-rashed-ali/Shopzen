package shopzen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val credentialManager = remember { CredentialManager.create(this@MainActivity) }
            val coroutineScope = rememberCoroutineScope()
            AppThemeController {
                LocaleControllerEffect()
                AppNavHost(
                    navController = navController,
                    launchGoogleSignIn = { onToken, onError ->
                        coroutineScope.launch {
                            launchGoogleSignIn(
                                credentialManager = credentialManager,
                                onToken = onToken,
                                onError = onError
                            )
                        }
                    },
                    launchAppleSignIn = { onSuccess, onError ->
                        launchAppleSignIn(
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    }
                )
            }
        }
    }

    private suspend fun launchGoogleSignIn(
        credentialManager: CredentialManager,
        onToken: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val serverClientId = getGoogleWebClientId()
        if (serverClientId.isBlank()) {
            onError(getString(R.string.error_google_client_id_missing))
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )
            val googleCredential = GoogleIdTokenCredential
                .createFrom(result.credential.data)
            onToken(googleCredential.idToken)
        } catch (_: GetCredentialCancellationException) {
            onError(getString(R.string.error_google_cancelled))
        } catch (_: GetCredentialException) {
            onError(getString(R.string.error_google_failed))
        } catch (_: IllegalArgumentException) {
            onError(getString(R.string.error_google_failed))
        }
    }

    private fun getGoogleWebClientId(): String {
        val generatedClientId = runCatching {
            getString(R.string.default_web_client_id)
        }.getOrDefault("")

        return generatedClientId.ifBlank {
            BuildConfig.GOOGLE_WEB_CLIENT_ID
        }
    }

    private fun launchAppleSignIn(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()

        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(this, provider)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { throwable ->
                val message = if (throwable is FirebaseAuthUserCollisionException) {
                    throwable.localizedMessage ?: getString(R.string.error_apple_failed)
                } else {
                    throwable.localizedMessage ?: getString(R.string.error_apple_failed)
                }
                onError(message)
            }
    }
}
