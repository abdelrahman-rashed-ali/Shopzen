package iti.domain.catalog.usecase

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCategoriesUseCaseTest {

    private val fakeCategories = listOf(
        Category(id = "1", title = "Shoes"),
        Category(id = "2", title = "Bags", imageUrl = "https://example.com/bags.png")
    )

    @Test
    fun `invoke returns success with categories from repository`() = runTest {
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.success(emptyList<Product>())
            override suspend fun getBrands() = Result.success(emptyList<Brand>())
            override suspend fun getCategories() = Result.success(fakeCategories)
        }
        val useCase = GetCategoriesUseCase(repo)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(fakeCategories, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val error = RuntimeException("Timeout")
        val repo = object : CatalogRepository {
            override suspend fun getProducts() = Result.success(emptyList<Product>())
            override suspend fun getBrands() = Result.success(emptyList<Brand>())
            override suspend fun getCategories() = Result.failure<List<Category>>(error)
        }
        val useCase = GetCategoriesUseCase(repo)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }
}
