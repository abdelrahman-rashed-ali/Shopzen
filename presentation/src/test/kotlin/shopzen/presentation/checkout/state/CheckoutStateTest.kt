package shopzen.presentation.checkout.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address

class CheckoutStateTest {

    @Test
    fun `selectedAddress returns matching address when selected id exists`() {
        val address = sampleAddress(id = "address-1")
        val state = CheckoutState(
            addresses = listOf(address, sampleAddress(id = "address-2")),
            selectedAddressId = "address-1",
        )

        assertTrue(state.selectedAddress == address)
    }

    @Test
    fun `selectedAddress returns null when selected id is missing`() {
        val state = CheckoutState(
            addresses = listOf(sampleAddress(id = "address-1")),
            selectedAddressId = "missing",
        )

        assertNull(state.selectedAddress)
    }

    @Test
    fun `canContinueToPayment returns true when items and address exist`() {
        val state = CheckoutState(
            items = listOf(sampleItem()),
            addresses = listOf(sampleAddress(id = "address-1")),
            selectedAddressId = "address-1",
        )

        assertTrue(state.canContinueToPayment)
    }

    @Test
    fun `canPlaceOrder returns true when address, payment, and items exist`() {
        val state = CheckoutState(
            items = listOf(sampleItem()),
            addresses = listOf(sampleAddress(id = "address-1")),
            selectedAddressId = "address-1",
            selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
        )

        assertTrue(state.canPlaceOrder)
    }

    @Test
    fun `canPlaceOrder returns false while placing order`() {
        val state = CheckoutState(
            items = listOf(sampleItem()),
            addresses = listOf(sampleAddress(id = "address-1")),
            selectedAddressId = "address-1",
            selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
            isPlacingOrder = true,
        )

        assertFalse(state.canPlaceOrder)
    }

    private fun sampleItem() = CheckoutItemUi(
        id = "item-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        formattedUnitPrice = "$45.00",
        quantity = 1,
        formattedLineTotal = "$45.00",
        imageUrl = "",
    )

    private fun sampleAddress(id: String) = Address(
        id = id,
        recipientName = "Ada Lovelace",
        addressLine1 = "1 Main St",
        city = "Cairo",
        postalCode = "12345",
        country = "Egypt",
        phone = "+201000000000",
    )
}
