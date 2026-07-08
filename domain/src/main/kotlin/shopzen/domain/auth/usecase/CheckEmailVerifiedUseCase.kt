package shopzen.domain.auth.usecase

import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class CheckEmailVerifiedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        // Reload current user to get the latest email verified status
        authRepository.reloadCurrentUser()
        val user = authRepository.getCurrentUser().getOrNull()
        return user?.isEmailVerified == true
    }
}
