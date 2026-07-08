package shopzen.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.presentation.common.theme.ShopzenTheme

/**
 * The ONLY place in the app allowed to read UserPreferences for the
 * purpose of driving MaterialTheme. Individual screens must never build
 * their own MaterialTheme instance — they consume LocalAppTheme instead.
 */
val LocalAppTheme: ProvidableCompositionLocal<AppTheme> = compositionLocalOf { AppTheme.SYSTEM }

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = getUserPreferencesUseCase()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())
}

/**
 * Collects preferences once at the MainActivity / root Composable level,
 * above AppNavGraph. Wraps content in ShopzenTheme(darkTheme = ...) so
 * this is the single root theme source — no screen builds its own
 * MaterialTheme instance; every screen consumes MaterialTheme.colorScheme
 * or LocalAppTheme.current instead.
 *
 * Usage in MainActivity:
 *   setContent { AppThemeController { AppNavGraph(...) } }
 */
@Composable
fun AppThemeController(content: @Composable () -> Unit) {
    val viewModel: AppThemeViewModel = hiltViewModel()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (preferences.theme) {
        AppTheme.SYSTEM -> systemDarkTheme
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    CompositionLocalProvider(LocalAppTheme provides preferences.theme) {
        ShopzenTheme(darkTheme = darkTheme) {
            content()
        }
    }
}
