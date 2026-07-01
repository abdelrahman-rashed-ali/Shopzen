package shopzen.domain.search.model

data class SearchFilter(
    val query: String = "",
    val mainCategory: String? = null,
    val subCategory: String? = null,
    val brand: String? = null
)
