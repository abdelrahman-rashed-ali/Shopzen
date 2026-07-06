package shopzen.domain.checkout.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.domain.common.Constants

class ValidateCashLimitUseCaseTest {
    private val useCase = ValidateCashLimitUseCase()

    @Test
    fun `invoke - below limit - returns true`() {
        assertTrue(useCase(Constants.MAX_COD_AMOUNT - 1.0))
    }

    @Test
    fun `invoke - equal limit - returns true`() {
        assertTrue(useCase(Constants.MAX_COD_AMOUNT))
    }

    @Test
    fun `invoke - above limit - returns false`() {
        assertFalse(useCase(Constants.MAX_COD_AMOUNT + 1.0))
    }
}
