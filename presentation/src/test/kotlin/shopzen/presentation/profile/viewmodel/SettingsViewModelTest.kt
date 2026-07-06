package shopzen.presentation.profile.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shopzen.domain.auth.model.User
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.auth.usecase.SignOutUseCase
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.domain.profile.usecase.SetCurrencyUseCase
import shopzen.domain.profile.usecase.SetLanguageUseCase
import shopzen.domain.profile.usecase.SetThemeUseCase
import shopzen.presentation.MainDispatcherRule
import shopzen.presentation.profile.intent.SettingsIntent

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase = mockk()
    private val setCurrencyUseCase: SetCurrencyUseCase = mockk()
    private val setLanguageUseCase: SetLanguageUseCase = mockk()
    private val setThemeUseCase: SetThemeUseCase = mockk()
    private val signOutUseCase: SignOutUseCase = mockk()

    @Test
    fun `init - guest user keeps settings accessible`() = runTest {
        every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isGuest)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `change currency - saves through local preferences use case`() = runTest {
        every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())
        coEvery { getCurrentUserUseCase() } returns Result.success(null)
        coEvery { setCurrencyUseCase(AppCurrency.GBP) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.processIntent(SettingsIntent.ChangeCurrency(AppCurrency.GBP))
        advanceUntilIdle()

        coVerify(exactly = 1) { setCurrencyUseCase(AppCurrency.GBP) }
    }

    @Test
    fun `request firebase sync - guest shows auth required dialog`() = runTest {
        every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processIntent(SettingsIntent.RequestFirebaseSync)

        assertTrue(viewModel.state.value.showAuthRequiredDialog)
    }

    @Test
    fun `confirm logout - signed in user becomes guest`() = runTest {
        every { getUserPreferencesUseCase() } returns flowOf(UserPreferences())
        coEvery { getCurrentUserUseCase() } returns Result.success(
            User("user-1", "ada@example.com", "Ada", null, true)
        )
        coEvery { signOutUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.processIntent(SettingsIntent.RequestLogout)

        assertTrue(viewModel.state.value.showLogoutDialog)

        viewModel.processIntent(SettingsIntent.ConfirmLogout)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isGuest)
        assertFalse(viewModel.state.value.showLogoutDialog)
        coVerify(exactly = 1) { signOutUseCase() }
    }

    private fun createViewModel() = SettingsViewModel(
        getCurrentUserUseCase = getCurrentUserUseCase,
        getUserPreferencesUseCase = getUserPreferencesUseCase,
        setCurrencyUseCase = setCurrencyUseCase,
        setLanguageUseCase = setLanguageUseCase,
        setThemeUseCase = setThemeUseCase,
        signOutUseCase = signOutUseCase,
    )
}
