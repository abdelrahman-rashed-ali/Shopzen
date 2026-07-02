package shopzen.domain.catalog.model

data class Product(
    val id: String,
    val title: String,
    val vendor: String,
    val productType: String,
    val price: String,
    val imageUrl: String
)
