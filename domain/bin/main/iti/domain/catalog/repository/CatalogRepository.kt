package iti.domain.catalog.repository

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product

/**
 * Repository interface for catalog data operations.
 * Implementations live in the :data module.
 */
interface CatalogRepository {

    /**
     * Fetches all products from the catalog.
     * @return [Result] wrapping the list of products or an error.
     */
    suspend fun getProducts(): Result<List<Product>>

    /**
     * Fetches distinct brands/vendors from the catalog.
     * @return [Result] wrapping the list of brands or an error.
     */
    suspend fun getBrands(): Result<List<Brand>>

    /**
     * Fetches product categories (custom collections).
     * @return [Result] wrapping the list of categories or an error.
     */
    suspend fun getCategories(): Result<List<Category>>
}
