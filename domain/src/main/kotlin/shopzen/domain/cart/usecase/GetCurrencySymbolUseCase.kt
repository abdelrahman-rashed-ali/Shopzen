package shopzen.domain.cart.usecase

import javax.inject.Inject

/**
 * Returns the current user-selected currency symbol.
 * Mock returns "$" until DataStore / exchange-rate integration is wired.
 */
class GetCurrencySymbolUseCase @Inject constructor() {
    suspend operator fun invoke(): String = "$"
}
