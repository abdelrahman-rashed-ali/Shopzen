package iti.domain.auth.usecase

import com.shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SendVerificationEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.sendVerificationEmail()
}