package shopzen.domain.cart.usecase

import javax.inject.Inject

/** Removes a single cart item. Requires ConfirmationDialog in UI before calling. Mock until data layer. */
class RemoveFromCartUseCase @Inject constructor() {
    suspend operator fun invoke(itemId: String, userId: String): Result<Unit> = Result.success(Unit)
}
