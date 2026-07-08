package shopzen.presentation.wishlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.wishlist.usecase.GetWishlistUseCase
import shopzen.domain.wishlist.usecase.RemoveFromWishlistUseCase
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.state.WishlistState
import javax.inject.Inject
import shopzen.domain.wishlist.usecase.AddToWishlistUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val getWishlistUseCase: GetWishlistUseCase,
    private val removeFromWishlistUseCase: RemoveFromWishlistUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addToWishlistUseCase: AddToWishlistUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WishlistState())
    val state: StateFlow<WishlistState> = _state.asStateFlow()

    init {
        processIntent(WishlistIntent.LoadWishlist)
    }

    fun processIntent(intent: WishlistIntent) {
        when (intent) {
            is WishlistIntent.LoadWishlist -> loadWishlist()
            is WishlistIntent.AddToWishlist -> addToWishlist(intent.product)
            is WishlistIntent.RequestAddToWishlist -> {
                viewModelScope.launch {
                    val user = getCurrentUserUseCase().getOrNull()
                    if (user == null || user.email.isBlank()) {
                        _state.value = _state.value.copy(showLoginRequiredDialog = true)
                    } else {
                        _state.value = _state.value.copy(
                            showAddConfirmationDialog = true,
                            pendingAddProduct = intent.product
                        )
                    }
                }
            }
            is WishlistIntent.DismissLoginRequiredDialog -> {
                _state.value = _state.value.copy(showLoginRequiredDialog = false)
            }
            is WishlistIntent.ConfirmAddToWishlist -> {
                _state.value = _state.value.copy(
                    showAddConfirmationDialog = false,
                    pendingAddProduct = null
                )
                addToWishlist(intent.product)
            }
            is WishlistIntent.DismissAddConfirmDialog -> {
                _state.value = _state.value.copy(
                    showAddConfirmationDialog = false,
                    pendingAddProduct = null
                )
            }
            is WishlistIntent.RequestRemoveItem -> {
                _state.value = _state.value.copy(
                    showRemoveItemDialog = true,
                    pendingRemovalItemId = intent.itemId
                )
            }
            is WishlistIntent.ConfirmRemoveItem -> {
                confirmRemoveItem(intent.itemId)
            }
            is WishlistIntent.DismissConfirmDialog -> {
                _state.value = _state.value.copy(
                    showRemoveItemDialog = false,
                    pendingRemovalItemId = null
                )
            }
            is WishlistIntent.OpenProduct -> {
                // Handled in navigation
            }
            is WishlistIntent.AddToCart -> {
                // Cart integration can be added here
            }
        }
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val currency = getCurrencySymbolUseCase()
            val userResult = getCurrentUserUseCase()
            val userId = userResult.getOrNull()?.uid ?: "guest_user"

            getWishlistUseCase(userId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load wishlist",
                        currency = currency
                    )
                }
                .collect { items ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        items = items,
                        error = null,
                        currency = currency
                    )
                }
        }
    }

    private fun confirmRemoveItem(itemId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = removeFromWishlistUseCase(itemId)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    showRemoveItemDialog = false,
                    pendingRemovalItemId = null
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Failed to remove item",
                    showRemoveItemDialog = false,
                    pendingRemovalItemId = null
                )
            }
        }
    }

    private fun addToWishlist(product: shopzen.domain.catalog.model.Product) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userResult = getCurrentUserUseCase()
            val userId = userResult.getOrNull()?.uid ?: "guest_user"
            val item = shopzen.domain.wishlist.model.WishlistItem(
                id = java.util.UUID.randomUUID().toString(),
                productId = product.id,
                title = product.title,
                vendor = product.vendor,
                price = product.price,
                imageUrl = product.imageUrl,
                userId = userId,
                addedAt = System.currentTimeMillis()
            )
            val result = addToWishlistUseCase(item)
            if (result.isSuccess) {
                _state.value = _state.value.copy(isLoading = false)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Failed to add item to wishlist"
                )
            }
        }
    }
}

