package shopzen.data.auth.remote

import com.google.firebase.auth.FirebaseUser

interface RemoteAuthDataSource {
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): FirebaseUser

    suspend fun signIn(
        email: String,
        password: String
    ): FirebaseUser

    suspend fun signInWithGoogle(idToken: String): FirebaseUser

    suspend fun signInWithApple(
        idToken: String,
        rawNonce: String
    ): FirebaseUser

    suspend fun signInAnonymously(): FirebaseUser

    suspend fun reloadCurrentUser()

    fun getCurrentUser(): FirebaseUser?

    suspend fun sendVerificationEmail()

    fun signOut()

    suspend fun deleteCurrentUser()
}
