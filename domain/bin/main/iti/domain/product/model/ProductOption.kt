package iti.domain.product.model

/**
 * A product option definition (e.g. "Size" with values ["S", "M", "L"]).
 */
data class ProductOption(
    val id: Long,
    val name: String,
    val values: List<String>,
)
