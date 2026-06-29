package shopzen.domain.auth.usecase

import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> = authRepository.getCurrentUser()
}
