package shopzen.presentation.brand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shopzen.domain.catalog.usecase.GetBrandsUseCase
import shopzen.presentation.brand.intent.BrandListIntent
import shopzen.presentation.brand.state.BrandListState
import javax.inject.Inject

@HiltViewModel
class BrandListViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BrandListState())
    val state: StateFlow<BrandListState> = _state.asStateFlow()

    init {
        processIntent(BrandListIntent.LoadBrands)
    }

    fun processIntent(intent: BrandListIntent) {
        when (intent) {
            is BrandListIntent.LoadBrands -> loadBrands()
        }
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = getBrandsUseCase()
            result.onSuccess { brands ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    brands = brands
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load brands."
                )
            }
        }
    }
}
