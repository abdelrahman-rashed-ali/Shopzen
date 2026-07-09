package shopzen.domain.ai.model

/**
 * Type-safe identifier for a pending assistant action.
 */
@JvmInline
value class PendingAiActionId(val value: String)

/**
 * State-changing action prepared by the assistant and blocked until confirmation.
 */
data class PendingAiAction(
    val id: PendingAiActionId,
    val type: AiActionType,
    val title: String,
    val description: String,
    val payload: Map<String, String>,
    val requiresConfirmation: Boolean = true,
)

enum class AiActionType {
    ADD_TO_CART,
    REMOVE_FROM_CART,
    UPDATE_CART_QUANTITY,
    CLEAR_CART,
    APPLY_DISCOUNT,
    OPEN_CHECKOUT,
    TOGGLE_WISHLIST,
    SAVE_USER_PREFERENCE,
}

