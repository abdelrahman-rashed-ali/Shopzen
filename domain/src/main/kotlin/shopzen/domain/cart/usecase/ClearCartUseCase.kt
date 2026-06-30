package shopzen.domain.cart.usecase

import javax.inject.Inject

/** Deletes all cart items for user. Requires ConfirmationDialog before calling. Mock until data layer. */
class ClearCartUseCase @Inject constructor() {
    suspend operator fun invoke(userId: String): Result<Unit> = Result.success(Unit)
}
