package com.shopzen.domain.auth.repository

import com.shopzen.domain.auth.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Flow<Result<User>>

    suspend fun sendVerificationEmail(): Result<Unit>
}
