package shopzen.domain.cart.usecase

import shopzen.domain.auth.repository.AuthRepository
import javax.inject.Inject

/**
 * Returns the current user-selected currency symbol.
 * Retrieves from Shopify Admin API customer profile. Falls back to "USD".
 */
class GetCurrencySymbolUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: shopzen.domain.profile.repository.ProfileRepository,
) {
    suspend operator fun invoke(): String {
        val user = authRepository.getCurrentUser().getOrNull()
        if (user == null || user.email == null) return "USD"
        
        return profileRepository.getUserCurrency(user.email)
    }
}
