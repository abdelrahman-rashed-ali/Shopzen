package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.repository.PreferencesRepository
import javax.inject.Inject

class SetCurrencyUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(currency: AppCurrency): Result<Unit> =
        preferencesRepository.setCurrency(currency)
}
