package com.shopzen.domain.auth.usecase

import com.shopzen.domain.auth.repository.AuthRepository

class SendVerificationEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.sendVerificationEmail()
}