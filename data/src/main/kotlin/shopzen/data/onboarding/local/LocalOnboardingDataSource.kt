package shopzen.data.onboarding.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.app.Application

class LocalOnboardingDataSource @Inject constructor(application: Application) {
    private val prefs: SharedPreferences = application.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
    
    private val _hasCompleted = MutableStateFlow(prefs.getBoolean(KEY_HAS_COMPLETED, false))
    val hasCompleted: Flow<Boolean> = _hasCompleted.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED, true).apply()
        _hasCompleted.value = true
    }

    companion object {
        private const val KEY_HAS_COMPLETED = "has_completed"
    }
}
