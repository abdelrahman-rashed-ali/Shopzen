package shopzen.presentation.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.ai.model.AiAssistantRequest
import shopzen.domain.ai.model.AiChatMessage
import shopzen.domain.ai.model.AiConversationContext
import shopzen.domain.ai.model.AiMessageId
import shopzen.domain.ai.model.AiMessageRole
import shopzen.domain.ai.usecase.AiAssistantUseCase
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.product.usecase.GetProductByIdUseCase
import shopzen.domain.cart.usecase.AddToCartUseCase
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.model.CartItem
import shopzen.domain.ai.model.AiActionType
import shopzen.presentation.ai.intent.AiChatIntent
import shopzen.presentation.ai.state.AiChatState
import shopzen.presentation.ai.state.AiMessageRoleUi
import shopzen.presentation.ai.state.AiMessageUiModel
import shopzen.presentation.ai.state.AiProductSuggestionUiModel

@HiltViewModel
class AiChatViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    getProductByIdUseCase: GetProductByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getProductUseCase: GetProductByIdUseCase,
) : ViewModel() {

    private val aiAssistantUseCase = AiAssistantUseCase(
        getProductsUseCase = getProductsUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        getProductByIdUseCase = getProductByIdUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        getCartUseCase = getCartUseCase,
    )

    private val _state = MutableStateFlow(AiChatState())
    val state: StateFlow<AiChatState> = _state.asStateFlow()

    private var context = AiConversationContext()

    fun processIntent(intent: AiChatIntent) {
        when (intent) {
            is AiChatIntent.SendMessage -> sendMessage(intent.text)
            is AiChatIntent.ConfirmAction -> confirmAction(intent.actionId)
            is AiChatIntent.CancelAction -> cancelAction(intent.actionId)
            AiChatIntent.RetryLastMessage -> retryLastMessage()
            AiChatIntent.ClearChat -> clearChat()
            is AiChatIntent.OpenProduct -> Unit
        }
    }

    private fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || _state.value.isLoading) return

        val userMessage = AiMessageUiModel(
            id = "user-${System.nanoTime()}",
            role = AiMessageRoleUi.USER,
            content = text,
        )

        _state.update { current ->
            current.copy(
                messages = current.messages + userMessage,
                isLoading = true,
                error = null,
                currentInput = "",
                lastUserMessage = text,
            )
        }

        viewModelScope.launch {
            val history = _state.value.messages.map { it.toDomainMessage() }
            aiAssistantUseCase(
                AiAssistantRequest(
                    message = text,
                    history = history,
                    context = context,
                )
            ).onSuccess { response ->
                context = response.context
                _state.update { current ->
                    current.copy(
                        messages = current.messages + response.message.toUiModel(),
                        pendingActions = if (response.message.pendingAction != null) {
                            current.pendingActions + response.message.pendingAction!!
                        } else current.pendingActions,
                        isLoading = false,
                        error = null,
                        suggestions = response.suggestedFollowUps.ifEmpty { current.suggestions },
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable

                val message = throwable.message ?: "Assistant failed. Please try again."
                _state.update { current ->
                    current.copy(
                        messages = current.messages + AiMessageUiModel(
                            id = "error-${System.nanoTime()}",
                            role = AiMessageRoleUi.ASSISTANT,
                            content = message,
                            isError = true,
                        ),
                        isLoading = false,
                        error = message,
                    )
                }
            }
        }
    }

    private fun retryLastMessage() {
        val last = _state.value.lastUserMessage ?: return
        _state.update { current ->
            current.copy(
                messages = current.messages.dropLastWhile { it.role == AiMessageRoleUi.ASSISTANT },
                error = null,
            )
        }
        sendMessage(last)
    }

    private fun clearChat() {
        context = AiConversationContext()
        _state.value = AiChatState()
    }

    private fun confirmAction(actionId: String) {
        val action = _state.value.pendingActions.find { it.id.value == actionId } ?: return
        
        _state.update { current ->
            current.copy(
                pendingActions = current.pendingActions.filter { it.id.value != actionId },
                isLoading = true,
            )
        }

        viewModelScope.launch {
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null) {
                emitSystemMessage("You must be logged in to do that.")
                return@launch
            }

            val result = when (action.type) {
                AiActionType.CLEAR_CART -> clearCartUseCase(user.uid).map { "Your cart has been cleared." }
                AiActionType.ADD_TO_CART -> {
                    val productId = action.payload["productId"]?.toLongOrNull()
                    val variantId = action.payload["variantId"]
                    val quantity = action.payload["quantity"]?.toIntOrNull() ?: 1
                    if (productId == null || variantId == null) {
                        Result.failure(IllegalArgumentException("Missing product details."))
                    } else {
                        getProductUseCase(productId).mapCatching { product ->
                            val variant = product.variants.firstOrNull { it.id.toString() == variantId }
                                ?: throw IllegalArgumentException("Variant not found.")
                            val priceValue = try {
                                variant.price.toString().replace(",", "").toDouble()
                            } catch (e: Exception) {
                                0.0
                            }
                            val cartItem = CartItem(
                                id = "cart_item_${System.nanoTime()}",
                                productId = product.id.toString(),
                                variantId = variant.id.toString(),
                                title = product.title,
                                variantTitle = variant.title,
                                price = priceValue,
                                quantity = quantity,
                                maxQuantity = variant.inventoryQuantity,
                                imageUrl = product.images.firstOrNull()?.src.orEmpty(),
                                userId = user.uid,
                            )
                            addToCartUseCase(cartItem).getOrThrow()
                            "Added ${product.title} to your cart."
                        }
                    }
                }
                else -> Result.failure(UnsupportedOperationException("Action not supported yet."))
            }

            result.onSuccess { message ->
                emitSystemMessage(message)
            }.onFailure { error ->
                emitSystemMessage(error.message ?: "Failed to execute action.")
            }
        }
    }

    private fun cancelAction(actionId: String) {
        _state.update { current ->
            current.copy(
                pendingActions = current.pendingActions.filter { it.id.value != actionId }
            )
        }
        emitSystemMessage("Action cancelled.")
    }

    private fun emitSystemMessage(content: String) {
        _state.update { current ->
            current.copy(
                messages = current.messages + AiMessageUiModel(
                    id = "system-${System.nanoTime()}",
                    role = AiMessageRoleUi.ASSISTANT,
                    content = content,
                ),
                isLoading = false,
            )
        }
    }

    private fun AiChatMessage.toUiModel(): AiMessageUiModel =
        AiMessageUiModel(
            id = id.value,
            role = when (role) {
                AiMessageRole.USER -> AiMessageRoleUi.USER
                AiMessageRole.ASSISTANT -> AiMessageRoleUi.ASSISTANT
            },
            content = content,
            products = productSuggestions.map { product ->
                AiProductSuggestionUiModel(
                    id = product.id,
                    title = product.title,
                    description = product.description,
                    priceText = product.price?.let { "${product.currency} $it" } ?: product.currency,
                    imageUrl = product.imageUrl,
                    available = product.available,
                )
            },
        )

    private fun AiMessageUiModel.toDomainMessage(): AiChatMessage =
        AiChatMessage(
            id = AiMessageId(id),
            role = when (role) {
                AiMessageRoleUi.USER -> AiMessageRole.USER
                AiMessageRoleUi.ASSISTANT -> AiMessageRole.ASSISTANT
            },
            content = content,
        )
}
