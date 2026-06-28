package iti.presentation.catalog.viewmodel

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository
import iti.domain.catalog.usecase.GetBrandsUseCase
import iti.domain.catalog.usecase.GetCategoriesUseCase
import iti.domain.catalog.usecase.GetProductsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeProducts = listOf(
        Product("1", "Product A", "Nike", "Shoes", "100.00", "https://img.com/a.png",
            listOf("https://img.com/a.png")),
        Product("2", "Product B", "Adidas", "Bags", "200.00", "https://img.com/b.png",
            listOf("https://img.com/b.png")),
        Product("3", "Product C", "Puma", "Hats", "50.00", "https://img.com/c.png",
            listOf("https://img.com/c.png"))
    )

    private val fakeBrands = listOf(Brand("Nike"), Brand("Adidas"), Brand("Puma"))
    private val fakeCategories = listOf(Category("1", "Shoes"), Category("2", "Bags"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        productsResult: Result<List<Product>> = Result.success(fakeProducts),
        brandsResult: Result<List<Brand>> = Result.success(fakeBrands),
        categoriesResult: Result<List<Category>> = Result.success(fakeCategories)
    ): HomeViewModel {
        val repo = object : CatalogRepository {
            override suspend fun getProducts(): Result<List<Product>> {
                delay(500)
                return productsResult
            }
            override suspend fun getBrands(): Result<List<Brand>> {
                delay(500)
                return brandsResult
            }
            override suspend fun getCategories(): Result<List<Category>> {
                delay(500)
                return categoriesResult
            }
        }
        return HomeViewModel(
            GetProductsUseCase(repo),
            GetBrandsUseCase(repo),
            GetCategoriesUseCase(repo)
        )
    }

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = createViewModel()
        runCurrent()
        // Before coroutines execute, check loading was set
        val state = viewModel.state.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `successful load populates all state fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(fakeProducts, state.newArrivals)
        assertEquals(fakeBrands, state.brands)
        assertEquals(fakeCategories, state.categories)
    }

    @Test
    fun `banner images derived from first products`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.bannerImages.size)
        assertEquals("https://img.com/a.png", state.bannerImages[0])
    }

    @Test
    fun `all failures sets error message`() = runTest {
        val error = RuntimeException("Network error")
        val viewModel = createViewModel(
            productsResult = Result.failure(error),
            brandsResult = Result.failure(error),
            categoriesResult = Result.failure(error)
        )
        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
        assertTrue(state.newArrivals.isEmpty())
        assertTrue(state.brands.isEmpty())
        assertTrue(state.categories.isEmpty())
    }

    @Test
    fun `partial failure still populates available data`() = runTest {
        val viewModel = createViewModel(
            productsResult = Result.success(fakeProducts),
            brandsResult = Result.failure(RuntimeException("Brand error")),
            categoriesResult = Result.success(fakeCategories)
        )
        advanceUntilIdle()

        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error) // Not all failed, so no error
        assertEquals(fakeProducts, state.newArrivals)
        assertTrue(state.brands.isEmpty()) // Brands failed
        assertEquals(fakeCategories, state.categories)
    }

    @Test
    fun `banner images limited to 5`() = runTest {
        val manyProducts = (1..10).map {
            Product("$it", "P$it", "V", "T", "10", "https://img.com/$it.png")
        }
        val viewModel = createViewModel(productsResult = Result.success(manyProducts))
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.bannerImages.size)
    }

    @Test
    fun `empty products produces empty banner images`() = runTest {
        val viewModel = createViewModel(productsResult = Result.success(emptyList()))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.bannerImages.isEmpty())
    }
}
