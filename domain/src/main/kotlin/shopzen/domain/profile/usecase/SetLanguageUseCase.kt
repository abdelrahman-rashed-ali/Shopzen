package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.repository.PreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(language: AppLanguage): Result<Unit> =
        preferencesRepository.setLanguage(language)
}
