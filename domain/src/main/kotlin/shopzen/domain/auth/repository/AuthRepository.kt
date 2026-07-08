package shopzen.domain.auth.repository

import shopzen.domain.auth.model.User
import shopzen.domain.auth.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Flow<Result<User>>

    fun signInWithEmail(
        email: String,
        password: String
    ): Flow<Result<User>>

    fun signInWithGoogle(idToken: String): Flow<Result<User>>

    fun signInWithApple(
        idToken: String,
        rawNonce: String
    ): Flow<Result<User>>

    fun signInAsGuest(): Flow<Result<AuthSession>>

    suspend fun reloadCurrentUser(): Result<Unit>

    suspend fun getCurrentUser(): Result<User?>

    suspend fun sendVerificationEmail(): Result<Unit>

    suspend fun signOut(): Result<Unit>

    suspend fun deleteCurrentUser(): Result<Unit>
}
