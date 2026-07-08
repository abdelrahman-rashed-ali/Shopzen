package shopzen.domain.cart.usecase

import shopzen.domain.profile.repository.PreferencesRepository
import shopzen.domain.profile.model.AppCurrency
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Returns the current user-selected currency from local preferences.
 */
class GetCurrencySymbolUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(): AppCurrency {
        return preferencesRepository.getPreferences().first().currency
    }
}
