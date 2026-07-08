package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.customer.usecase.GetCurrentShopifyCustomerIdUseCase
import shopzen.domain.profile.usecase.GetOrderHistoryUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText
import shopzen.presentation.profile.intent.OrderHistoryIntent
import shopzen.presentation.profile.state.OrderHistoryState

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getCurrentShopifyCustomerIdUseCase: GetCurrentShopifyCustomerIdUseCase,
    private val getOrderHistoryUseCase: GetOrderHistoryUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OrderHistoryState())
    val state: StateFlow<OrderHistoryState> = _state.asStateFlow()

    fun processIntent(intent: OrderHistoryIntent) {
        when (intent) {
            OrderHistoryIntent.LoadOrders,
            OrderHistoryIntent.Retry,
            -> loadOrders()
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val customerId = getCurrentShopifyCustomerIdUseCase().getOrElse {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.order_history_error_auth_required),
                        orders = emptyList(),
                    )
                }
                return@launch
            }

            val currency = getCurrencySymbolUseCase()

            getOrderHistoryUseCase(customerId).fold(
                onSuccess = { orders ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            orders = orders,
                            currency = currency,
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.order_history_error_load_failed),
                        )
                    }
                },
            )
        }
    }
}
