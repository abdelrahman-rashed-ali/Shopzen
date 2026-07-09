package shopzen.domain.ai.usecase

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import shopzen.domain.ai.model.AiAssistantRequest
import shopzen.domain.ai.model.AiAssistantResponse
import shopzen.domain.ai.model.AiChatMessage
import shopzen.domain.ai.model.AiConversationContext
import shopzen.domain.ai.model.AiMessageId
import shopzen.domain.ai.model.AiMessageRole
import shopzen.domain.ai.model.AiProductSuggestion
import shopzen.domain.ai.model.AiActionType
import shopzen.domain.ai.model.PendingAiAction
import shopzen.domain.ai.model.PendingAiActionId
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.product.usecase.GetProductByIdUseCase

/**
 * Produces safe read-only shopping assistant responses from domain use cases.
 */
class AiAssistantUseCase @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCartUseCase: GetCartUseCase,
) {
    /**
     * Handles one assistant turn without mutating app or user data.
     */
    suspend operator fun invoke(request: AiAssistantRequest): Result<AiAssistantResponse> {
        val text = request.message.trim()
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be blank."))
        }

        return try {
            Result.success(route(text, request))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun route(
        text: String,
        request: AiAssistantRequest,
    ): AiAssistantResponse {
        val normalized = text.lowercase(Locale.US)
        return when {
            normalized.hasAny("add", "put") && normalized.hasAny("cart", "basket", "bag") -> prepareAddToCart(text, request.context)
            normalized.hasAny("clear", "empty") && normalized.hasAny("cart", "basket", "bag") -> prepareClearCart()
            normalized.hasAny("cart", "basket", "bag") -> cartSummary()
            normalized.hasAny("return", "refund", "shipping", "delivery", "policy", "payment") -> policyAnswer(text)
            normalized.hasAny("category", "categories", "collection", "collections") -> categories()
            normalized.hasAny("compare", "cheaper", "cheapest between") -> compare(text, request.context)
            normalized.hasAny("detail", "details", "tell me more", "more about") -> productDetails(text, request.context)
            else -> searchProducts(text)
        }
    }

    private fun prepareClearCart(): AiAssistantResponse {
        val action = PendingAiAction(
            id = PendingAiActionId("action-${System.nanoTime()}"),
            type = AiActionType.CLEAR_CART,
            title = "Clear Cart",
            description = "Are you sure you want to remove all items from your cart?",
            payload = emptyMap(),
        )
        return assistantResponse("I can help you clear your cart.", pendingAction = action)
    }

    private suspend fun prepareAddToCart(text: String, context: AiConversationContext): AiAssistantResponse {
        val productId = text.extractLongId() ?: context.recentProductIds.firstOrNull()?.toLongOrNull()
        if (productId == null) {
            return assistantResponse("Please specify which product you want to add to your cart.")
        }
        val product = getProductByIdUseCase(productId).getOrNull()
        if (product == null) {
            return assistantResponse("I couldn't find that product.")
        }
        val variant = product.variants.firstOrNull { it.inventoryQuantity > 0 }
        if (variant == null) {
            return assistantResponse("Sorry, ${product.title} is currently out of stock.")
        }
        val action = PendingAiAction(
            id = PendingAiActionId("action-${System.nanoTime()}"),
            type = AiActionType.ADD_TO_CART,
            title = "Add to Cart",
            description = "Add 1x ${product.title} to your cart for ${variant.price} $DEFAULT_CURRENCY?",
            payload = mapOf(
                "productId" to product.id.toString(),
                "variantId" to variant.id.toString(),
                "quantity" to "1"
            )
        )
        return assistantResponse("I can add ${product.title} to your cart.", pendingAction = action)
    }

    private suspend fun searchProducts(text: String): AiAssistantResponse {
        val products = getProductsUseCase().getOrThrow()
        val constraints = SearchConstraints.from(text)
        val terms = text.searchTerms()

        val matches = products
            .asSequence()
            .filter { product -> product.matchesTerms(terms) || terms.isEmpty() }
            .filter { product -> constraints.matches(product.price.asPriceOrNull()) }
            .take(MAX_RESULTS)
            .map { it.toSuggestion() }
            .toList()

        val answer = when {
            matches.isEmpty() -> "I could not find matching products in the current catalog."
            constraints.hasPriceLimit -> "Found ${matches.size} matching products within that price range."
            terms.isEmpty() -> "Here are ${matches.size} products worth checking."
            else -> "Found ${matches.size} products matching your request."
        }

        return assistantResponse(
            content = answer,
            products = matches,
            followUps = listOf("Compare these products", "Show cheaper options"),
        )
    }

    private suspend fun categories(): AiAssistantResponse {
        val categories = getCategoriesUseCase().getOrThrow()
        val names = categories.take(MAX_CATEGORY_RESULTS).joinToString { it.displayName }
        val content = if (names.isBlank()) {
            "No categories are available right now."
        } else {
            "Available categories: $names."
        }

        return assistantResponse(
            content = content,
            followUps = listOf("Show products from ${categories.firstOrNull()?.displayName.orEmpty()}".trim()),
        )
    }

    private suspend fun productDetails(
        text: String,
        context: AiConversationContext,
    ): AiAssistantResponse {
        val productId = text.extractLongId() ?: context.recentProductIds.firstOrNull()?.toLongOrNull()
        if (productId == null) {
            return assistantResponse("Pick a product first, then I can explain its details.")
        }

        val product = getProductByIdUseCase(productId).getOrThrow()
        val availableVariants = product.variants.count { it.inventoryQuantity > 0 }
        val firstPrice = product.variants.firstOrNull()?.price ?: product.price
        val content = buildString {
            append(product.title)
            append(" is from ")
            append(product.vendor.ifBlank { "this store" })
            append(". Price starts at ")
            append(firstPrice)
            append(". ")
            append(if (availableVariants > 0) "$availableVariants variants are in stock. " else "No variants are currently in stock. ")
            if (product.description.isNotBlank()) {
                append(product.description.take(DESCRIPTION_LIMIT))
            } else {
                append("No description is available.")
            }
        }

        return assistantResponse(
            content = content,
            products = listOf(
                AiProductSuggestion(
                    id = product.id.toString(),
                    title = product.title,
                    description = product.description.take(DESCRIPTION_LIMIT),
                    price = firstPrice.asPriceOrNull(),
                    currency = DEFAULT_CURRENCY,
                    imageUrl = product.images.firstOrNull()?.src,
                    available = availableVariants > 0,
                )
            ),
            followUps = listOf("Compare it with another product", "Find similar products"),
        )
    }

    private suspend fun compare(
        text: String,
        context: AiConversationContext,
    ): AiAssistantResponse {
        val ids = text.extractLongIds().ifEmpty {
            context.recentProductIds.mapNotNull { it.toLongOrNull() }
        }.take(2)

        if (ids.size < 2) {
            return assistantResponse("I need two products to compare. Choose products from search results first.")
        }

        val products = ids.map { getProductByIdUseCase(it).getOrThrow() }
        val suggestions = products.map { product ->
            val price = product.variants.firstOrNull()?.price ?: product.price
            AiProductSuggestion(
                id = product.id.toString(),
                title = product.title,
                description = product.description.take(DESCRIPTION_LIMIT),
                price = price.asPriceOrNull(),
                currency = DEFAULT_CURRENCY,
                imageUrl = product.images.firstOrNull()?.src,
                available = product.variants.any { it.inventoryQuantity > 0 },
            )
        }
        val cheapest = suggestions.filter { it.price != null }.minByOrNull { it.price ?: Double.MAX_VALUE }
        val content = buildString {
            append("Compared ${suggestions.size} products by price and availability.")
            if (cheapest != null) {
                append(" Cheapest option: ${cheapest.title} at ${cheapest.price} ${cheapest.currency}.")
            }
        }

        return assistantResponse(
            content = content,
            products = suggestions,
            followUps = listOf("Tell me more about ${suggestions.first().title}", "Find similar products"),
        )
    }

    private suspend fun cartSummary(): AiAssistantResponse {
        val user = getCurrentUserUseCase().getOrThrow()
            ?: return assistantResponse("Please log in to view your cart.")

        val cart = getCartUseCase(user.uid).first().getOrThrow()
        val content = if (cart.items.isEmpty()) {
            "Your cart is empty."
        } else {
            val itemSummary = cart.items.joinToString { "${it.quantity} x ${it.title}" }
            "Your cart has ${cart.items.size} items: $itemSummary. Subtotal: ${cart.subtotalPrice} ${cart.currency}."
        }

        return assistantResponse(content = content)
    }

    private fun policyAnswer(question: String): AiAssistantResponse {
        return assistantResponse(
            content = "I cannot confirm store policy from app data yet. Please check store support for: $question",
            followUps = listOf("Search products", "Show categories"),
        )
    }

    private fun assistantResponse(
        content: String,
        products: List<AiProductSuggestion> = emptyList(),
        followUps: List<String> = emptyList(),
        pendingAction: PendingAiAction? = null,
    ): AiAssistantResponse {
        return AiAssistantResponse(
            message = AiChatMessage(
                id = AiMessageId("assistant-${System.nanoTime()}"),
                role = AiMessageRole.ASSISTANT,
                content = content,
                productSuggestions = products,
                pendingAction = pendingAction,
            ),
            context = AiConversationContext(recentProductIds = products.map { it.id }),
            suggestedFollowUps = followUps.filter { it.isNotBlank() },
        )
    }

    private data class SearchConstraints(
        val minPrice: Double? = null,
        val maxPrice: Double? = null,
    ) {
        val hasPriceLimit: Boolean = minPrice != null || maxPrice != null

        fun matches(price: Double?): Boolean {
            if (price == null) return true
            return (minPrice == null || price >= minPrice) &&
                (maxPrice == null || price <= maxPrice)
        }

        companion object {
            fun from(text: String): SearchConstraints {
                val normalized = text.lowercase(Locale.US)
                fun containsAny(vararg needles: String): Boolean =
                    needles.any { normalized.contains(it, ignoreCase = true) }

                val prices = Regex("""\d+(?:\.\d+)?""")
                    .findAll(normalized)
                    .mapNotNull { it.value.toDoubleOrNull() }
                    .toList()
                val price = prices.firstOrNull()
                return when {
                    price == null -> SearchConstraints()
                    containsAny("under", "below", "less than", "cheaper than", "max") ->
                        SearchConstraints(maxPrice = price)
                    containsAny("over", "above", "more than", "min") ->
                        SearchConstraints(minPrice = price)
                    else -> SearchConstraints(maxPrice = price)
                }
            }
        }
    }

    private fun Product.matchesTerms(terms: List<String>): Boolean {
        val searchable = listOf(title, vendor, productType).joinToString(" ").lowercase(Locale.US)
        return terms.all { searchable.contains(it) }
    }

    private fun Product.toSuggestion(): AiProductSuggestion =
        AiProductSuggestion(
            id = id,
            title = title,
            description = productType,
            price = price.asPriceOrNull(),
            currency = DEFAULT_CURRENCY,
            imageUrl = imageUrl.takeIf { it.isNotBlank() },
            available = true,
        )

    private fun String.searchTerms(): List<String> {
        val stopWords = setOf(
            "show", "find", "me", "please", "product", "products", "under", "below",
            "less", "than", "over", "above", "more", "max", "min", "egp", "usd",
            "recommend", "something", "daily", "use", "for", "the", "a", "an",
        )
        return lowercase(Locale.US)
            .replace(Regex("""[^\p{L}\p{N}\s]"""), " ")
            .split(Regex("""\s+"""))
            .filter { it.length > 2 && it !in stopWords && it.toDoubleOrNull() == null }
    }

    private fun String.extractLongId(): Long? = extractLongIds().firstOrNull()

    private fun String.extractLongIds(): List<Long> =
        Regex("""\d+""").findAll(this).mapNotNull { it.value.toLongOrNull() }.toList()

    private fun String.asPriceOrNull(): Double? =
        replace(",", "").filter { it.isDigit() || it == '.' }.toDoubleOrNull()

    private fun String.hasAny(vararg needles: String): Boolean =
        needles.any { contains(it, ignoreCase = true) }

    private val Category.displayName: String
        get() = title.ifBlank { id }

    private companion object {
        const val MAX_RESULTS = 6
        const val MAX_CATEGORY_RESULTS = 8
        const val DESCRIPTION_LIMIT = 220
        const val DEFAULT_CURRENCY = "EGP"
    }
}
