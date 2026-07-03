package shopzen.domain.profile.repository

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    /** Always reads local DataStore — instant, no Result wrapper needed. */
    fun getPreferences(): Flow<UserPreferences>

    suspend fun setLanguage(language: AppLanguage): Result<Unit>
    suspend fun setTheme(theme: AppTheme): Result<Unit>
    suspend fun setCurrency(currency: AppCurrency): Result<Unit>

    /** One-shot pull used only at sign-in. */
    suspend fun syncPreferencesFromRemote(uid: String): Result<Unit>
}
