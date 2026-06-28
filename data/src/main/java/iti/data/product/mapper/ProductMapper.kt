package iti.data.product.mapper

import iti.data.product.remote.dto.ImageDto
import iti.data.product.remote.dto.OptionDto
import iti.data.product.remote.dto.ProductDto
import iti.data.product.remote.dto.VariantDto
import iti.domain.product.model.Product
import iti.domain.product.model.ProductImage
import iti.domain.product.model.ProductOption
import iti.domain.product.model.ProductVariant
import iti.domain.product.model.SelectedOption

/**
 * Maps Shopify REST DTOs → Domain models.
 * - Strips HTML from body_html
 * - Splits comma-separated tags
 * - Reconstructs variant selectedOptions from option1/option2/option3
 */

fun ProductDto.toDomain(): Product {
    val domainVariants = variants.map { it.toDomain(options) }
    val firstVariant = domainVariants.firstOrNull()

    return Product(
        id = id,
        title = title,
        description = stripHtml(bodyHtml),
        vendor = vendor,
        productType = productType,
        tags = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        images = images.map { it.toDomain() },
        variants = domainVariants,
        options = options.map { it.toDomain() },
        price = firstVariant?.price ?: "0.00",
        compareAtPrice = firstVariant?.compareAtPrice,
    )
}

fun VariantDto.toDomain(options: List<OptionDto>): ProductVariant {
    // Reconstruct selected options from option1, option2, option3
    val optionValues = listOfNotNull(option1, option2, option3)
    val selectedOptions = options.zip(optionValues) { opt, value ->
        SelectedOption(name = opt.name, value = value)
    }

    return ProductVariant(
        id = id,
        title = title,
        price = price,
        compareAtPrice = compareAtPrice,
        inventoryQuantity = inventoryQuantity,
        selectedOptions = selectedOptions,
    )
}

fun OptionDto.toDomain(): ProductOption {
    return ProductOption(
        id = id,
        name = name,
        values = values,
    )
}

fun ImageDto.toDomain(): ProductImage {
    return ProductImage(
        id = id,
        src = src,
        alt = alt,
    )
}

/**
 * Strips HTML tags from Shopify's body_html field.
 * Returns empty string for null input.
 */
private fun stripHtml(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return html
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
        .trim()
}
