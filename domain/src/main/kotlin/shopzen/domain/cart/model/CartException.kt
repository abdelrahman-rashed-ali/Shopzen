package shopzen.domain.cart.model

sealed class CartException(message: String) : Exception(message) {
    class OutOfStock(message: String = "Product is out of stock") : CartException(message)
    class MutationFailed(message: String = "Cart operation failed") : CartException(message)
}
