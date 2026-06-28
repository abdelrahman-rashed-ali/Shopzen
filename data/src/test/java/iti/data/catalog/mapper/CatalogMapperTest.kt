package iti.data.catalog.mapper

import iti.data.catalog.remote.dto.CollectionDto
import iti.data.catalog.remote.dto.CollectionImageDto
import iti.data.catalog.remote.dto.ImageDto
import iti.data.catalog.remote.dto.ProductDto
import iti.data.catalog.remote.dto.VariantDto
import iti.data.catalog.mapper.CatalogMapper.toDomain
import iti.data.catalog.mapper.CatalogMapper.toDistinctBrands
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogMapperTest {

    // --- ProductDto.toDomain() ---

    @Test
    fun `toDomain maps ProductDto fields correctly`() {
        val dto = ProductDto(
            id = 123L,
            title = "Test Shoe",
            vendor = "Nike",
            productType = "Footwear",
            images = listOf(
                ImageDto(id = 1, src = "https://img.com/1.png"),
                ImageDto(id = 2, src = "https://img.com/2.png")
            ),
            variants = listOf(
                VariantDto(id = 10, price = "129.99", inventoryQuantity = 5)
            ),
            createdAt = "2024-01-15T10:00:00Z"
        )

        val product = dto.toDomain()

        assertEquals("123", product.id)
        assertEquals("Test Shoe", product.title)
        assertEquals("Nike", product.vendor)
        assertEquals("Footwear", product.productType)
        assertEquals("129.99", product.price)
        assertEquals("https://img.com/1.png", product.imageUrl)
        assertEquals(2, product.images.size)
        assertEquals("2024-01-15T10:00:00Z", product.createdAt)
    }

    @Test
    fun `toDomain handles null fields safely`() {
        val dto = ProductDto(
            id = 1L,
            title = null,
            vendor = null,
            productType = null,
            images = null,
            variants = null,
            createdAt = null
        )

        val product = dto.toDomain()

        assertEquals("1", product.id)
        assertEquals("", product.title)
        assertEquals("", product.vendor)
        assertEquals("", product.productType)
        assertEquals("", product.price)
        assertEquals("", product.imageUrl)
        assertEquals(0, product.images.size)
        assertEquals("", product.createdAt)
    }

    @Test
    fun `toDomain uses first variant price as default`() {
        val dto = ProductDto(
            id = 1L,
            title = "Multi-Variant",
            vendor = "Brand",
            productType = "Type",
            images = null,
            variants = listOf(
                VariantDto(id = 1, price = "10.00", inventoryQuantity = 2),
                VariantDto(id = 2, price = "20.00", inventoryQuantity = 3)
            ),
            createdAt = null
        )

        val product = dto.toDomain()
        assertEquals("10.00", product.price)
    }

    // --- CollectionDto.toDomain() ---

    @Test
    fun `toDomain maps CollectionDto fields correctly`() {
        val dto = CollectionDto(
            id = 456L,
            title = "Summer Collection",
            image = CollectionImageDto(src = "https://img.com/summer.png")
        )

        val category = dto.toDomain()

        assertEquals("456", category.id)
        assertEquals("Summer Collection", category.title)
        assertEquals("https://img.com/summer.png", category.imageUrl)
    }

    @Test
    fun `toDomain handles null collection image`() {
        val dto = CollectionDto(
            id = 789L,
            title = "No Image",
            image = null
        )

        val category = dto.toDomain()
        assertNull(category.imageUrl)
    }

    // --- toDistinctBrands() ---

    @Test
    fun `toDistinctBrands extracts unique vendors`() {
        val dtos = listOf(
            ProductDto(1, "A", "Nike", null, null, null, null),
            ProductDto(2, "B", "Adidas", null, null, null, null),
            ProductDto(3, "C", "Nike", null, null, null, null),
            ProductDto(4, "D", "Puma", null, null, null, null)
        )

        val brands = dtos.toDistinctBrands()

        assertEquals(3, brands.size)
        assertEquals("Nike", brands[0].name)
        assertEquals("Adidas", brands[1].name)
        assertEquals("Puma", brands[2].name)
    }

    @Test
    fun `toDistinctBrands skips null vendors`() {
        val dtos = listOf(
            ProductDto(1, "A", null, null, null, null, null),
            ProductDto(2, "B", "Adidas", null, null, null, null)
        )

        val brands = dtos.toDistinctBrands()

        assertEquals(1, brands.size)
        assertEquals("Adidas", brands[0].name)
    }

    @Test
    fun `toDistinctBrands returns empty for empty list`() {
        val brands = emptyList<ProductDto>().toDistinctBrands()
        assertEquals(0, brands.size)
    }
}
