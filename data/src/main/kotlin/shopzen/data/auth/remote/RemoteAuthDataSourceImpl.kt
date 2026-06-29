package shopzen.data.auth.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : RemoteAuthDataSource {

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): FirebaseUser {

        val result = firebaseAuth
            .createUserWithEmailAndPassword(
                email,
                password
            )
            .await()

        val user = requireNotNull(result.user) {
            "Firebase returned a null user after registration"
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates)
            .await()
        user.sendEmailVerification()
            .await()
        return user

    }

    override suspend fun signIn(
        email: String,
        password: String
    ): FirebaseUser {
        val result = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()

        return requireNotNull(result.user) {
            "Firebase returned a null user after sign in"
        }
    }

    override suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return requireNotNull(result.user) {
            "Firebase returned a null user after Google sign in"
        }
    }

    override suspend fun signInWithApple(
        idToken: String,
        rawNonce: String
    ): FirebaseUser {
        val credential = OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        val result = firebaseAuth.signInWithCredential(credential).await()
        return requireNotNull(result.user) {
            "Firebase returned a null user after Apple sign in"
        }
    }

    override suspend fun signInAnonymously(): FirebaseUser {
        val result = firebaseAuth.signInAnonymously().await()
        return requireNotNull(result.user) {
            "Firebase returned a null user after guest sign in"
        }
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun sendVerificationEmail() {
        val user = requireNotNull(firebaseAuth.currentUser) {
            "No authenticated user to send verification email to"
        }
        user.sendEmailVerification().await()
    }
}
