package shopzen.domain.auth.usecase

import shopzen.domain.auth.model.User
import shopzen.domain.auth.repository.AuthRepository
import shopzen.domain.customer.repository.ShopifyCustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Orchestrates the full, atomic registration flow:
 *
 * 1. Create Firebase user (email + password) — delegates to [AuthRepository].
 * 2. Create Shopify customer via REST API — delegates to [ShopifyCustomerRepository].
 *    ↳ On failure → delete the just-created Firebase user (rollback).
 * 3. Persist the Shopify customer ID to Firestore under users/{uid}.
 *    ↳ On failure → delete the Firebase user (rollback).
 * 4. Emit [Result.success] carrying the [User].
 *
 * If any step after Firebase registration fails the function rolls back
 * by deleting the just-created Firebase account before emitting the error,
 * leaving no inconsistent state between Firebase and Shopify.
 *
 * Consumers (e.g. [shopzen.presentation.auth.viewmodel.RegisterViewModel])
 * observe a [Flow]<[Result]<[User]>> — the contract is identical to the
 * previous single-step implementation; zero ViewModel changes required.
 */
class RegisterWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val shopifyCustomerRepository: ShopifyCustomerRepository,
) {
    operator fun invoke(
        email: String,
        password: String,
        displayName: String,
    ): Flow<Result<User>> = flow {

        // ── Step 1: Firebase Registration ─────────────────────────────────────
        // Collect the first (and only) emission from the repository flow.
        // Throws if the result is a failure, propagated to the outer catch.
        val user: User = authRepository
            .registerWithEmail(email, password, displayName)
            .first()
            .getOrThrow()

        // ── Step 2: Create Shopify customer ───────────────────────────────────
        // Split displayName into first / last the same way the rest of the
        // app does (first word = first name, remainder = last name).
        val firstName = displayName.substringBefore(' ').trim().ifEmpty { displayName }
        val lastName  = displayName.substringAfter(' ', missingDelimiterValue = "").trim()

        val customerId: Long = shopifyCustomerRepository
            .createShopifyCustomer(
                email     = email,
                firstName = firstName,
                lastName  = lastName,
            )
            .getOrElse { cause ->
                // Roll back: Firebase user created but Shopify failed.
                authRepository.deleteCurrentUser()
                throw cause
            }

        // ── Step 3: Persist customer ID to Firestore ──────────────────────────
        shopifyCustomerRepository
            .saveShopifyCustomerId(uid = user.uid, customerId = customerId)
            .getOrElse { cause ->
                // Roll back: Firestore write failed — the Firebase account exists
                // but has no Shopify link. Delete it to keep systems consistent.
                authRepository.deleteCurrentUser()
                throw cause
            }

        // ── Step 4: Emit success ──────────────────────────────────────────────
        emit(Result.success(user))

    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
