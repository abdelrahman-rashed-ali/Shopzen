package shopzen.domain.auth.usecase

import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import shopzen.domain.customer.repository.ShopifyCustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val shopifyCustomerRepository: ShopifyCustomerRepository,
) {
    operator fun invoke(idToken: String): Flow<Result<User>> = flow {
        val user = authRepository.signInWithGoogle(idToken).first().getOrThrow()

        val existingCustomerId = shopifyCustomerRepository.getShopifyCustomerId(user.uid).getOrNull()
        
        if (existingCustomerId == null) {
            val displayName = user.displayName
            val firstName = displayName.substringBefore(' ').trim().ifEmpty { displayName }
            val lastName = displayName.substringAfter(' ', missingDelimiterValue = "").trim()

            val customerId = shopifyCustomerRepository.createShopifyCustomer(
                email = user.email,
                firstName = firstName,
                lastName = lastName
            ).getOrElse { cause ->
                authRepository.deleteCurrentUser()
                throw cause
            }

            shopifyCustomerRepository.saveShopifyCustomerId(user.uid, customerId).getOrElse { cause ->
                authRepository.deleteCurrentUser()
                throw cause
            }
        }

        emit(Result.success(user))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
