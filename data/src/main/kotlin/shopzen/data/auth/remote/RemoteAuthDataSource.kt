package shopzen.data.auth.remote

import com.google.firebase.auth.FirebaseUser

interface RemoteAuthDataSource {
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): FirebaseUser

    suspend fun sendVerificationEmail()
}
