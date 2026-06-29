package shopzen.domain.auth.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String
    ): Flow<Result<User>> = authRepository.signInWithEmail(email, password)
}
