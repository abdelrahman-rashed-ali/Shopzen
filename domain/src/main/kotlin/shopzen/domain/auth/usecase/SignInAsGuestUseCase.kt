package shopzen.domain.auth.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.auth.model.AuthSession
import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SignInAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<AuthSession>> = authRepository.signInAsGuest()
}
