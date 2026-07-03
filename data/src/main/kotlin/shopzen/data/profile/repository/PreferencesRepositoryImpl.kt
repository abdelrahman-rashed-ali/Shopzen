package shopzen.data.profile.repository

import shopzen.data.profile.local.LocalPreferencesDataSource
import shopzen.data.profile.mapper.toFirestoreMap
import shopzen.data.profile.mapper.toUserPreferencesOrDefault
import shopzen.data.profile.remote.RemoteProfileDataSource
import shopzen.domain.auth.repository.AuthRepository // existing :domain auth feature
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * getPreferences() reads LocalPreferencesDataSource exclusively — never
 * blocks on network. Writes go local-first (always succeeds), then mirror
 * to Firestore fire-and-forget, only for non-guest sessions.
 */
class PreferencesRepositoryImpl @Inject constructor(
    private val local: LocalPreferencesDataSource,
    private val remote: RemoteProfileDataSource,
    private val authRepository: AuthRepository,
) : PreferencesRepository {

    // Fire-and-forget scope for the remote mirror writes only.
    private val mirrorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getPreferences(): Flow<UserPreferences> = local.preferencesFlow

    override suspend fun setLanguage(language: AppLanguage): Result<Unit> = runCatching {
        local.setLanguage(language)
        mirrorCurrentPreferences()
    }

    override suspend fun setTheme(theme: AppTheme): Result<Unit> = runCatching {
        local.setTheme(theme)
        mirrorCurrentPreferences()
    }

    override suspend fun setCurrency(currency: AppCurrency): Result<Unit> = runCatching {
        local.setCurrency(currency)
        mirrorCurrentPreferences()
    }

    override suspend fun syncPreferencesFromRemote(uid: String): Result<Unit> {
        val remoteResult = remote.readPreferences(uid)
        val remoteData = remoteResult.getOrNull() ?: return remoteResult.map { }
        val remotePrefs = remoteData.toUserPreferencesOrDefault()
        local.setLanguage(remotePrefs.language)
        local.setTheme(remotePrefs.theme)
        local.setCurrency(remotePrefs.currency)
        return Result.success(Unit)
    }

    /** Only mirrors to Firestore for a signed-in, non-guest session. Never blocks the caller. */
    private suspend fun mirrorCurrentPreferences() {
        authRepository.getCurrentUser().onSuccess { user ->
            if (user != null && user.email.isNotBlank()) {
                mirrorScope.launch {
                    val prefs = local.preferencesFlow.first()
                    remote.writePreferences(user.uid, prefs.toFirestoreMap())
                }
            }
        }
    }
}
