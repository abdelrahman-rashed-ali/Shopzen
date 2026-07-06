package shopzen.domain.auth.usecase

import javax.inject.Inject

/**
 * Returns true when a valid Firebase session exists.
 * Stub: always returns true until the auth feature branch is merged.
 */
class IsUserLoggedInUseCase @Inject constructor() {
    operator fun invoke(): Boolean = true
}
