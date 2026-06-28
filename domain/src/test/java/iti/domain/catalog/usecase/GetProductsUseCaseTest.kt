package iti.domain.catalog.usecase

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetProductsUseCaseTest {

    private val fakeProducts = listOf(
        Product(
            id = "1",
            title = "Test Product",
            vendor = "TestVendor",
            productType = "Shoes",
            price = "99.99",
            imageUrl = "https://example.com/img.png"
        )
    )

    @Test
    fun `invoke returns success with products from repository`() = runTest {
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.success(fakeProducts)
            override suspend fun getBrands() = Result.success(emptyList<Brand>())
            override suspend fun getCategories() = Result.success(emptyList<Category>())
        }
        val useCase = GetProductsUseCase(repo)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(fakeProducts, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = RuntimeException("Network error")
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.failure<List<Product>>(error)
            override suspend fun getBrands() = Result.success(emptyList<Brand>())
            override suspend fun getCategories() = Result.success(emptyList<Category>())
        }
        val useCase = GetProductsUseCase(repo)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
