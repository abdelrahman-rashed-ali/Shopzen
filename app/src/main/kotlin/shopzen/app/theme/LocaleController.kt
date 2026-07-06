package shopzen.app.theme

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Wraps AppCompatDelegate.setApplicationLocales(). This is the ONLY
 * place in the app allowed to switch the app locale — never called from
 * :presentation or :domain.
 *
 * On API 33+, Compose's LayoutDirection follows the configured locale
 * automatically once setApplicationLocales() is applied — no manual RTL
 * mirroring required in individual screens. Below API 33, an
 * Activity.recreate() fallback is required for the change to take full
 * effect, matching AndroidX's documented per-app language guidance.
 */
object LocaleController {
    private var lastAppliedTags: String? = null

    fun applyLanguage(activity: Activity, language: AppLanguage) {
        val localeList = LocaleListCompat.forLanguageTags(language.tag)
        val requestedTags = localeList.toLanguageTags()
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()

        if (currentTags == requestedTags || lastAppliedTags == requestedTags) {
            return
        }

        lastAppliedTags = requestedTags
        AppCompatDelegate.setApplicationLocales(localeList)
        if (Build.VERSION.SDK_INT < 33) {
            activity.recreate()
        }
    }
}

@HiltViewModel
class LocaleControllerViewModel @Inject constructor(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
) : ViewModel() {
    val language: StateFlow<AppLanguage> = getUserPreferencesUseCase()
        .map { it.language }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences().language)
}

/**
 * Call once whenever GetUserPreferencesUseCase emits a changed AppLanguage.
 * Mount this above AppNavGraph, alongside AppThemeController.
 */
@Composable
fun LocaleControllerEffect() {
    val viewModel: LocaleControllerViewModel = hiltViewModel()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(context, language) {
        val activity = context as? Activity ?: return@LaunchedEffect
        LocaleController.applyLanguage(activity, language)
    }
}
