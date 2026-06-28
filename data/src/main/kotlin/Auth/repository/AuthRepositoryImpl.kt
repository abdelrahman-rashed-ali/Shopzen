package com.shopzen.data.auth.repository

import com.shopzen.data.auth.mapper.toDomainModel
import com.shopzen.data.auth.remote.RemoteAuthDataSource
import com.shopzen.domain.auth.model.User
import com.shopzen.domain.auth.repository.AuthRepository
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

    override suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        remoteAuthDataSource.sendVerificationEmail()
    }
}
