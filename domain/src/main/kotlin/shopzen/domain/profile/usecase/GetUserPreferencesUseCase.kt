package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserPreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = preferencesRepository.getPreferences()
}
