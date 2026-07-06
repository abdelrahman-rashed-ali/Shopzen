package shopzen.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    fun openAuthenticated(
        onAuthenticated: () -> Unit,
        onGuest: () -> Unit,
    ) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().getOrNull()
            if (user?.email.isNullOrBlank()) {
                onGuest()
            } else {
                onAuthenticated()
            }
        }
    }
}
