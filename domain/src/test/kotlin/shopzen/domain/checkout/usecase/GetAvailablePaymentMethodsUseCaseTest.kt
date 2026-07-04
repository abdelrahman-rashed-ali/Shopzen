package shopzen.domain.checkout.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.common.Constants

class GetAvailablePaymentMethodsUseCaseTest {
    private val useCase = GetAvailablePaymentMethodsUseCase(ValidateCashLimitUseCase())

    @Test
    fun `invoke - below limit - returns cash on delivery and online payment`() {
        val result = useCase(Constants.MAX_COD_AMOUNT - 1.0)

        assertEquals(
            listOf(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.ONLINE_PAYMENT),
            result,
        )
    }

    @Test
    fun `invoke - equal limit - returns cash on delivery and online payment`() {
        val result = useCase(Constants.MAX_COD_AMOUNT)

        assertEquals(
            listOf(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.ONLINE_PAYMENT),
            result,
        )
    }

    @Test
    fun `invoke - above limit - returns online payment only`() {
        val result = useCase(Constants.MAX_COD_AMOUNT + 1.0)

        assertEquals(listOf(PaymentMethod.ONLINE_PAYMENT), result)
    }
}
