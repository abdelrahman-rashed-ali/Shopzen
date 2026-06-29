package iti.domain.auth.usecase

import com.shopzen.domain.auth.model.User
import com.shopzen.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Flow<Result<User>> = authRepository.registerWithEmail(email, password, displayName)
}