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
import shopzen.domain.profile.usecase.GetOrderDetailUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText
import shopzen.presentation.profile.intent.OrderDetailIntent
import shopzen.presentation.profile.state.OrderDetailState

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderDetailUseCase: GetOrderDetailUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    private var lastOrderId: String? = null

    fun processIntent(intent: OrderDetailIntent) {
        when (intent) {
            is OrderDetailIntent.LoadOrder -> loadOrder(intent.orderId)
            OrderDetailIntent.Retry -> lastOrderId?.let(::loadOrder)
        }
    }

    private fun loadOrder(orderId: String) {
        lastOrderId = orderId
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currency = getCurrencySymbolUseCase()

            getOrderDetailUseCase(orderId).fold(
                onSuccess = { order ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            order = order,
                            currency = currency,
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.order_detail_error_load_failed),
                        )
                    }
                },
            )
        }
    }
}
