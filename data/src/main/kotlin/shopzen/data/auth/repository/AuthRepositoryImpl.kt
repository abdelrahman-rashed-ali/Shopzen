package shopzen.data.auth.repository

import shopzen.data.auth.mapper.toDomainModel
import shopzen.data.auth.mapper.toGuestSession
import shopzen.data.auth.remote.RemoteAuthDataSource
import shopzen.domain.auth.model.AuthSession
import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {

    override fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Flow<Result<User>> = flow {
        val firebaseUser = remoteAuthDataSource.register(email, password, displayName)
        emit(Result.success(firebaseUser.toDomainModel()))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    override fun signInWithEmail(
        email: String,
        password: String
    ): Flow<Result<User>> = flow {
        val firebaseUser = remoteAuthDataSource.signIn(email, password)
        emit(Result.success(firebaseUser.toDomainModel()))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    override fun signInWithGoogle(idToken: String): Flow<Result<User>> = flow {
        val firebaseUser = remoteAuthDataSource.signInWithGoogle(idToken)
        emit(Result.success(firebaseUser.toDomainModel()))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    override fun signInWithApple(
        idToken: String,
        rawNonce: String
    ): Flow<Result<User>> = flow {
        val firebaseUser = remoteAuthDataSource.signInWithApple(idToken, rawNonce)
        emit(Result.success(firebaseUser.toDomainModel()))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    override fun signInAsGuest(): Flow<Result<AuthSession>> = flow {
        val firebaseUser = remoteAuthDataSource.signInAnonymously()
        emit(Result.success(firebaseUser.toGuestSession()))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    override suspend fun getCurrentUser(): Result<User?> = runCatching {
        remoteAuthDataSource.getCurrentUser()?.toDomainModel()
    }

    override suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        remoteAuthDataSource.sendVerificationEmail()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        remoteAuthDataSource.signOut()
    }

    override suspend fun deleteCurrentUser(): Result<Unit> = runCatching {
        remoteAuthDataSource.deleteCurrentUser()
    }
}
