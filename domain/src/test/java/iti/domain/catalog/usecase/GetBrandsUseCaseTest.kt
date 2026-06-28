package iti.domain.catalog.usecase

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetBrandsUseCaseTest {

    private val fakeBrands = listOf(
        Brand(name = "Nike"),
        Brand(name = "Adidas")
    )

    @Test
    fun `invoke returns success with brands from repository`() = runTest {
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.success(emptyList<Product>())
            override suspend fun getBrands() = Result.success(fakeBrands)
            override suspend fun getCategories() = Result.success(emptyList<Category>())
        }
        val useCase = GetBrandsUseCase(repo)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(fakeBrands, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = RuntimeException("API error")
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.success(emptyList<Product>())
            override suspend fun getBrands() = Result.failure<List<Brand>>(error)
            override suspend fun getCategories() = Result.success(emptyList<Category>())
        }
        val useCase = GetBrandsUseCase(repo)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }
}
