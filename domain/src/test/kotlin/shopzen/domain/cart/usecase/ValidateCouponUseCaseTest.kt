package shopzen.domain.cart.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.repository.CartRepository

class ValidateCouponUseCaseTest {
    private val repository: CartRepository = mockk()
    private val useCase = ValidateCouponUseCase(repository)

    @Test
    fun `invoke - delegates trimmed code to repository`() = runTest {
        val expected = CouponValidationResult.Valid(
            code = "SAVE20",
            discountPercent = 20.0,
            discountFixed = null,
        )
        coEvery { repository.validateCoupon("SAVE20") } returns Result.success(expected)

        val result = useCase(" SAVE20 ")

        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.validateCoupon("SAVE20") }
    }

    @Test
    fun `invoke - propagates repository failure`() = runTest {
        coEvery { repository.validateCoupon("SAVE20") } returns Result.failure(IllegalStateException("Network down"))

        val result = useCase("SAVE20")

        assertTrue(result.isFailure)
        assertEquals("Network down", result.exceptionOrNull()?.message)
    }
}
