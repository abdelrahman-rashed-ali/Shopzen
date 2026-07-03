package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.repository.PreferencesRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(theme: AppTheme): Result<Unit> =
        preferencesRepository.setTheme(theme)
}
