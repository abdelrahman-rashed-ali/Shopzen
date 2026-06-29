package shopzen.domain.auth.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Result<User>> =
        authRepository.signInWithGoogle(idToken)
}
