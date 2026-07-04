# AGENTS.md — `:domain` Module (Shopzen)

> **Scope:** This document is the single source of truth for everything inside the `:domain` Gradle module of the Shopzen project.
> No deviation from these guidelines without updating this document first.

---

## Table of Contents

1. [Module Role & Boundaries](#1-module-role--boundaries)
2. [Package Structure](#2-package-structure)
3. [Domain Models](#3-domain-models)
4. [Repository Interfaces](#4-repository-interfaces)
5. [Use Cases](#5-use-cases)
6. [Feature-by-Feature Reference](#6-feature-by-feature-reference)
   - [auth](#61-auth)
   - [catalog](#62-catalog)
   - [search](#63-search)
   - [wishlist](#64-wishlist)
   - [cart](#65-cart)
   - [account](#66-account)
   - [checkout](#67-checkout)
7. [Coding Standards for `:domain`](#7-coding-standards-for-domain)
8. [Testing Strategy](#8-testing-strategy)

---

## 1. Module Role & Boundaries

The `:domain` module is the **core business layer** of Shopzen. It is a **pure Kotlin module** — it has zero knowledge of Android, networking, or databases.

```
:presentation ──▶ :domain ◀── :data
                    ▲
              :app wires everything
```

### What `:domain` IS

| Responsibility | Description |
|---|---|
| Domain Models | Pure Kotlin `data class`es representing business entities |
| Repository Interfaces | Contracts (`interface`) that `:data` implements |
| Use Cases | Single-responsibility classes encapsulating one business operation |
| Business Rules | All logic that is not UI or infrastructure |

### What `:domain` MUST NOT contain

| Forbidden | Reason |
|---|---|
| `import android.*` | Platform-independent; no Android SDK |
| `import retrofit2.*` | Networking is an infrastructure detail |
| `import androidx.room.*` | Persistence is an infrastructure detail |
| `import com.google.firebase.*` | Auth infrastructure belongs in `:data` |
| DTOs / Entities | Shaped by API/DB — not domain concerns |
| `@Inject`, `@Singleton`, Hilt annotations | DI wiring belongs in `:app` |
| Any Composable or ViewModel import | UI belongs in `:presentation` |

> **Enforcement:** The `:domain` Gradle module must declare no dependency on `android`, `retrofit`, `room`, or `firebase` libraries. A CI lint check should fail the build if any such import is found.

---

## 2. Package Structure

```
com.shopzen.domain/
├── auth/
│   ├── model/
│   │   └── User.kt
│   ├── repository/
│   │   └── AuthRepository.kt
│   └── usecase/
│       ├── LoginWithEmailUseCase.kt
│       ├── LoginWithGoogleUseCase.kt
│       ├── RegisterWithEmailUseCase.kt
│       ├── SendVerificationEmailUseCase.kt
│       ├── LogoutUseCase.kt
│       ├── IsUserLoggedInUseCase.kt
│       └── GetCurrentUserUseCase.kt
│
├── catalog/
│   ├── model/
│   │   ├── Product.kt
│   │   ├── ProductImage.kt
│   │   ├── ProductVariant.kt
│   │   ├── ProductOption.kt
│   │   ├── SelectedOption.kt
│   │   ├── Brand.kt
│   │   ├── Category.kt
│   │   ├── SubCategory.kt
│   │   └── Review.kt
│   ├── repository/
│   │   └── CatalogRepository.kt
│   └── usecase/
│       ├── GetProductsUseCase.kt
│       ├── GetProductByIdUseCase.kt
│       ├── GetProductsByBrandUseCase.kt
│       ├── GetProductsByCategoryUseCase.kt
│       ├── GetBrandsUseCase.kt
│       ├── GetCategoriesUseCase.kt
│       └── GetProductReviewsUseCase.kt
│
├── search/
│   ├── model/
│   │   ├── SearchFilter.kt
│   │   └── SortOption.kt
│   ├── repository/
│   │   └── SearchRepository.kt
│   └── usecase/
│       ├── SearchProductsUseCase.kt
│       ├── FilterProductsUseCase.kt
│       └── SortProductsUseCase.kt
│
├── wishlist/
│   ├── model/
│   │   └── WishlistItem.kt
│   ├── repository/
│   │   └── WishlistRepository.kt
│   └── usecase/
│       ├── GetWishlistUseCase.kt
│       ├── AddToWishlistUseCase.kt
│       ├── RemoveFromWishlistUseCase.kt
│       └── IsProductInWishlistUseCase.kt
│
├── cart/
│   ├── model/
│   │   ├── Cart.kt
│   │   └── CartItem.kt
│   ├── repository/
│   │   └── CartRepository.kt
│   └── usecase/
│       ├── GetCartUseCase.kt
│       ├── AddToCartUseCase.kt
│       ├── RemoveFromCartUseCase.kt
│       ├── UpdateCartItemQuantityUseCase.kt
│       ├── GetCartTotalUseCase.kt
│       ├── ClearCartUseCase.kt
│       └── ValidateCouponUseCase.kt
│
├── account/
│   ├── model/
│   │   ├── UserProfile.kt
│   │   ├── Order.kt
│   │   ├── OrderLineItem.kt
│   │   ├── Address.kt
│   │   ├── Country.kt
│   │   ├── Province.kt
│   │   └── CurrencyRate.kt
│   ├── repository/
│   │   └── AccountRepository.kt
│   └── usecase/
│       ├── GetUserProfileUseCase.kt
│       ├── GetOrderHistoryUseCase.kt
│       ├── GetOrderDetailUseCase.kt
│       ├── GetAddressesUseCase.kt
│       ├── AddAddressUseCase.kt
│       ├── UpdateAddressUseCase.kt
│       ├── DeleteAddressUseCase.kt
│       ├── GetCountriesUseCase.kt
│       ├── ValidateAddressUseCase.kt
│       └── GetCurrencyRatesUseCase.kt
│
└── checkout/
    ├── model/
    │   ├── Checkout.kt
    │   ├── DiscountCode.kt
    │   ├── DiscountType.kt
    │   ├── PaymentMethod.kt
    │   └── OrderConfirmation.kt
    ├── repository/
    │   └── CheckoutRepository.kt
    └── usecase/
        ├── GetAvailablePaymentMethodsUseCase.kt
        ├── ValidateCashLimitUseCase.kt
        └── PlaceOrderUseCase.kt
```

### Structural Rules

- Every feature gets its own sub-package with exactly three sub-packages: `model/`, `repository/`, `usecase/`
- No files are placed at the root `com.shopzen.domain/` level except shared utilities if strictly necessary
- Sub-packages must not cross feature boundaries — `cart/model/` never imports from `catalog/model/` directly

---

## 3. Domain Models

### Rules for All Domain Models

- Pure Kotlin `data class` with no annotations (`@Entity`, `@SerializedName`, `@Json`, etc. are forbidden)
- All fields use Kotlin primitive types, `String`, `List`, other domain models, or Kotlin standard types
- No nullability unless the business rule genuinely allows absence — use meaningful defaults instead
- KDoc is required on every public `data class` and every non-obvious field

### Model Definitions

#### `auth`

```kotlin
/** Represents an authenticated user session. */
data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)
```

#### `catalog`

```kotlin
/** Full product as returned from Shopify, including all variants and images. */
data class Product(
    val id: String,
    val title: String,
    val vendor: String,            // Brand name
    val productType: String,       // Maps to Category
    val description: String,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val options: List<ProductOption>  // Drives size/color selectors
)

data class ProductImage(
    val id: String,
    val src: String,
    val altText: String?
)

/**
 * A purchasable variant (e.g. "Size M / Red").
 * [inventoryQuantity] is the real-time stock count — enforces cart quantity cap.
 */
data class ProductVariant(
    val id: String,
    val title: String,
    val price: Double,
    val inventoryQuantity: Int,
    val selectedOptions: List<SelectedOption>
)

/** An option definition on the product level (e.g. name = "Size", values = ["S","M","L"]). */
data class ProductOption(
    val id: String,
    val name: String,
    val values: List<String>
)

/** The chosen value for a specific option on a variant (e.g. name = "Size", value = "M"). */
data class SelectedOption(
    val name: String,
    val value: String
)

data class Brand(
    val name: String
)

data class Category(
    val id: String,
    val title: String,
    val subCategories: List<SubCategory>
)

data class SubCategory(
    val id: String,
    val title: String
)

data class Review(
    val id: String,
    val productId: String,
    val authorName: String,
    val rating: Float,
    val body: String,
    val createdAt: String
)
```

#### `search`

```kotlin
/**
 * Holds all active filter criteria for a search operation.
 * Null fields mean "no filter applied" for that dimension.
 */
data class SearchFilter(
    val query: String = "",
    val mainCategory: String? = null,
    val subCategory: String? = null,
    val brand: String? = null
)

/** Sort options available on the search results screen. */
enum class SortOption {
    DEFAULT,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    BEST_SELLER,
    BY_SUB_CATEGORY
}
```

#### `wishlist`

```kotlin
/**
 * A snapshot of a product saved to the wishlist.
 * Stored locally in Room — must include [userId] for multi-user support.
 */
data class WishlistItem(
    val id: String,
    val productId: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val imageUrl: String,
    val userId: String,
    val addedAt: Long  // epoch millis
)
```

#### `cart`

```kotlin
/**
 * The user's cart. [currency] is the currently selected display currency.
 * [subtotalPrice] is always recalculated locally — never fetched from API.
 */
data class Cart(
    val items: List<CartItem>,
    val currency: String,
    val subtotalPrice: Double,
    val discountAmount: Double,
    val totalPrice: Double,
    val appliedCoupon: DiscountCode?,
    val userId: String
)

/**
 * A single line item in the cart.
 * [maxQuantity] mirrors [ProductVariant.inventoryQuantity] — quantity must never exceed it.
 */
data class CartItem(
    val id: String,
    val productId: String,
    val variantId: String,
    val title: String,
    val variantTitle: String,
    val price: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
    val userId: String
)
```

#### `account`

```kotlin
data class UserProfile(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?
)

data class Order(
    val id: String,
    val orderNumber: Int,
    val totalPrice: Double,
    val currency: String,
    val financialStatus: String,
    val fulfillmentStatus: String,
    val createdAt: String,
    val lineItems: List<OrderLineItem>
)

data class OrderLineItem(
    val id: String,
    val title: String,
    val quantity: Int,
    val price: Double,
    val variantTitle: String?
)

/**
 * A shipping/billing address.
 * [latitude] and [longitude] are populated after GPS / Google Places validation.
 */
data class Address(
    val id: String?,   // null for unsaved addresses
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address1: String,
    val city: String,
    val province: String,
    val country: String,
    val countryCode: String,
    val zip: String,
    val latitude: Double?,
    val longitude: Double?
)

data class Country(
    val name: String,
    val code: String,
    val provinces: List<Province>
)

data class Province(
    val name: String,
    val code: String
)

/**
 * Exchange rate data fetched from the currency API.
 * [fetchedAt] is used to enforce a 1-hour cache TTL.
 */
data class CurrencyRate(
    val base: String,
    val rates: Map<String, Double>,
    val fetchedAt: Long  // epoch millis
)
```

#### `checkout`

```kotlin
/**
 * The full checkout state passed between checkout screens.
 * [appliedCoupon] is null when no coupon is active.
 */
data class Checkout(
    val lineItems: List<CartItem>,
    val shippingAddress: Address?,
    val subtotalPrice: Double,
    val discountAmount: Double,
    val totalPrice: Double,
    val currency: String,
    val appliedCoupon: DiscountCode?,
    val selectedPaymentMethod: PaymentMethod?
)

data class DiscountCode(
    val code: String,
    val discountType: DiscountType,
    val value: Double   // percentage (0–100) or fixed amount
)

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    ONLINE_PAYMENT
}

data class OrderConfirmation(
    val orderId: String,
    val orderNumber: Int,
    val totalPrice: Double,
    val currency: String
)
```

---

## 4. Repository Interfaces

### Rules for All Repository Interfaces

- Defined as `interface` only — no implementations in `:domain`
- Return types are `Flow<T>`, `Flow<Result<T>>`, or `Result<T>` — never raw types that force callers to handle exceptions
- Method names use verb phrases matching the use case that calls them
- KDoc required on every interface and every method

### Interface Definitions

#### `AuthRepository`

```kotlin
interface AuthRepository {
    /** Sign in with email and password via Firebase. */
    suspend fun loginWithEmail(email: String, password: String): Result<User>

    /** Sign in using a Firebase Google credential id token. */
    suspend fun loginWithGoogle(idToken: String): Result<User>

    /** Create a new Firebase account and trigger verification email. */
    suspend fun registerWithEmail(email: String, password: String): Result<User>

    /** Send a verification email to the currently signed-in user. */
    suspend fun sendVerificationEmail(): Result<Unit>

    /** Sign out and clear local session data. */
    suspend fun logout(): Result<Unit>

    /** Returns true if an active Firebase session exists. */
    fun isUserLoggedIn(): Boolean

    /** Returns the currently authenticated user, or null if no session. */
    fun getCurrentUser(): User?
}
```

#### `CatalogRepository`

```kotlin
interface CatalogRepository {
    /** Paginated list of all products. */
    fun getProducts(): Flow<Result<List<Product>>>

    /** Full product detail including variants and images. */
    suspend fun getProductById(productId: String): Result<Product>

    /** Products filtered by vendor name. */
    fun getProductsByBrand(brandName: String): Flow<Result<List<Product>>>

    /** Products filtered by product type (category). */
    fun getProductsByCategory(category: String): Flow<Result<List<Product>>>

    /** Distinct vendor names derived from the product catalog. */
    fun getBrands(): Flow<Result<List<Brand>>>

    /** All custom collections (categories). */
    fun getCategories(): Flow<Result<List<Category>>>

    /** Reviews for a given product from the external reviews API. */
    fun getProductReviews(productId: String): Flow<Result<List<Review>>>
}
```

#### `SearchRepository`

```kotlin
interface SearchRepository {
    /** Performs a text-based product search by title. */
    fun searchProducts(query: String): Flow<Result<List<Product>>>
}
```

> `FilterProductsUseCase` and `SortProductsUseCase` are client-side — they do not require a repository method.

#### `WishlistRepository`

```kotlin
interface WishlistRepository {
    /** Reactive stream of all wishlist items for the given user. */
    fun getWishlist(userId: String): Flow<List<WishlistItem>>

    /** Persist a new wishlist item snapshot for the user. */
    suspend fun addToWishlist(item: WishlistItem): Result<Unit>

    /** Remove a wishlist item by its ID. */
    suspend fun removeFromWishlist(itemId: String): Result<Unit>

    /** Reactive boolean indicating whether a product is in the user's wishlist. */
    fun isProductInWishlist(productId: String, userId: String): Flow<Boolean>
}
```

#### `CartRepository`

```kotlin
interface CartRepository {
    /** Reactive stream of the user's full cart; total is recalculated on each emission. */
    fun getCart(userId: String): Flow<Result<Cart>>

    /** Insert or increment a cart item after validating against stock. */
    suspend fun addToCart(item: CartItem): Result<Unit>

    /** Remove a single cart item by ID. */
    suspend fun removeFromCart(itemId: String): Result<Unit>

    /** Update the quantity of an existing cart item (already clamped by UseCase). */
    suspend fun updateCartItemQuantity(itemId: String, quantity: Int): Result<Unit>

    /** Remove all cart items belonging to the user. */
    suspend fun clearCart(userId: String): Result<Unit>

    /** Validate a coupon code against Shopify price rules. */
    suspend fun validateCoupon(code: String): Result<CouponValidationResult>
}
```

#### `AccountRepository`

```kotlin
interface AccountRepository {
    /** Fetch the Shopify customer record for the given customer ID. */
    suspend fun getUserProfile(customerId: String): Result<UserProfile>

    /** Fetch all past orders for a customer. */
    suspend fun getOrderHistory(customerId: String): Result<List<Order>>

    /** Fetch full order detail (line items etc.) for a single order. */
    suspend fun getOrderDetail(orderId: String): Result<Order>

    /** Fetch all saved addresses for a customer. */
    suspend fun getAddresses(customerId: String): Result<List<Address>>

    /** Create a new address for a customer. */
    suspend fun addAddress(customerId: String, address: Address): Result<Address>

    /** Update an existing address. */
    suspend fun updateAddress(customerId: String, address: Address): Result<Address>

    /** Delete an address by its ID. */
    suspend fun deleteAddress(customerId: String, addressId: String): Result<Unit>

    /** Fetch the full country + province list from the external countries API. */
    suspend fun getCountries(): Result<List<Country>>

    /** Validate an address using GPS or Google Places autocomplete. */
    suspend fun validateAddress(address: Address): Result<Address>

    /** Fetch live currency exchange rates. Implementation caches for 1 hour. */
    suspend fun getCurrencyRates(base: String): Result<CurrencyRate>
}
```

#### `CheckoutRepository`

```kotlin
interface CheckoutRepository {
    /** Submit a new order via GraphQL orderCreate mutation. */
    suspend fun placeOrder(checkout: Checkout): Result<OrderConfirmation>
}
```

---

## 5. Use Cases

### Use Case Rules

- Each use case is a **single Kotlin class** with **one public method**: `operator fun invoke(...)`
- The class name must follow the pattern: **Verb + Noun + `UseCase`** (e.g. `AddToCartUseCase`)
- Constructor accepts only repository interfaces — never concrete types
- No Android-specific types in parameters or return types
- `suspend` if the operation is async; returns `Flow<T>` if it must be observed over time
- KDoc required on the class and its `invoke` function
- Business rules (validation, clamping, filtering, sorting) live **here**, not in the repository

### Use Case Skeleton

```kotlin
/**
 * [Description of what this use case does and when to call it.]
 */
class VerbNounUseCase @Inject constructor(
    private val repository: FeatureRepository
) {
    /**
     * @param param description
     * @return description
     */
    suspend operator fun invoke(param: Type): Result<ReturnType> {
        // business logic here
        return repository.someMethod(param)
    }
}
```

---

## 6. Feature-by-Feature Reference

### 6.1 `auth`

**Responsibility:** Identity management — email/password login, Google Sign-In, registration, email verification, logout, and auth-gated routing.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `LoginWithEmailUseCase` | `suspend operator fun invoke(email: String, password: String): Flow<Result<User>>` | Firebase email/password sign-in |
| `LoginWithGoogleUseCase` | `suspend operator fun invoke(idToken: String): Flow<Result<User>>` | Firebase Google credential sign-in |
| `RegisterWithEmailUseCase` | `suspend operator fun invoke(email: String, password: String): Flow<Result<User>>` | Creates account + triggers verification email |
| `SendVerificationEmailUseCase` | `suspend operator fun invoke(): Result<Unit>` | Sends email to current user |
| `LogoutUseCase` | `suspend operator fun invoke(): Result<Unit>` | Signs out; clears local session |
| `IsUserLoggedInUseCase` | `operator fun invoke(): Boolean` | Returns true if a Firebase session exists |
| `GetCurrentUserUseCase` | `operator fun invoke(): User?` | Returns mapped current Firebase user |

#### Business Rules Enforced in Use Cases

- `RegisterWithEmailUseCase` must call `SendVerificationEmailUseCase` internally after account creation — do not rely on callers to chain this
- `LogoutUseCase` must clear any locally cached user-specific data (cart, wishlist) — coordinate with `CartRepository.clearCart` and `WishlistRepository`
- `IsUserLoggedInUseCase` is the single authority for auth-gate checks; all navigation guards call this

---

### 6.2 `catalog`

**Responsibility:** Browse the Shopify product catalog — home feed, brands, categories, product list, product detail, image gallery, size selection, and reviews.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `GetProductsUseCase` | `operator fun invoke(): Flow<Result<List<Product>>>` | Paginated product list |
| `GetProductByIdUseCase` | `suspend operator fun invoke(productId: String): Result<Product>` | Full detail with variants and images |
| `GetProductsByBrandUseCase` | `operator fun invoke(brandName: String): Flow<Result<List<Product>>>` | Products by vendor |
| `GetProductsByCategoryUseCase` | `operator fun invoke(category: String): Flow<Result<List<Product>>>` | Products by product type |
| `GetBrandsUseCase` | `operator fun invoke(): Flow<Result<List<Brand>>>` | Distinct vendor names |
| `GetCategoriesUseCase` | `operator fun invoke(): Flow<Result<List<Category>>>` | Collections as categories |
| `GetProductReviewsUseCase` | `operator fun invoke(productId: String): Flow<Result<List<Review>>>` | External reviews by product ID |

#### Business Rules Enforced in Use Cases

- `GetProductByIdUseCase` must return a `Product` with `options` populated — the size-selector in the UI depends on `ProductOption.name == "Size"`
- Out-of-stock logic (variant `inventoryQuantity == 0`) is a **display rule** handled in `:presentation` based on the domain model — `GetProductByIdUseCase` does not filter variants
- `GetProductsUseCase` enables offline reading; it should emit cached Room data first, then refresh from network (implemented in `:data`, transparent to `:domain`)

---

### 6.3 `search`

**Responsibility:** Global product search with filtering (category, sub-category, brand) and client-side sorting.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `SearchProductsUseCase` | `operator fun invoke(query: String): Flow<Result<List<Product>>>` | Remote text search by title |
| `FilterProductsUseCase` | `operator fun invoke(products: List<Product>, filter: SearchFilter): List<Product>` | Pure client-side filtering |
| `SortProductsUseCase` | `operator fun invoke(products: List<Product>, sortOption: SortOption): List<Product>` | Pure client-side sorting |

#### Business Rules Enforced in Use Cases

- `FilterProductsUseCase` applies all non-null `SearchFilter` fields as AND conditions
- `SortProductsUseCase` sorts the **already filtered** list — callers must always filter first, then sort
- Both `FilterProductsUseCase` and `SortProductsUseCase` are **pure functions** with no repository dependency — they are injected as-is from `:app`'s `UseCaseModule`
- Search debounce (300 ms) is enforced in the ViewModel, not here

---

### 6.4 `wishlist`

**Responsibility:** Save and remove favourite products. Persisted per `userId` in Room. Heart icon reflects live wishlist state.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `GetWishlistUseCase` | `operator fun invoke(userId: String): Flow<List<WishlistItem>>` | Reactive stream for current user |
| `AddToWishlistUseCase` | `suspend operator fun invoke(item: WishlistItem): Result<Unit>` | Inserts snapshot into Room |
| `RemoveFromWishlistUseCase` | `suspend operator fun invoke(itemId: String): Result<Unit>` | Deletes item |
| `IsProductInWishlistUseCase` | `operator fun invoke(productId: String, userId: String): Flow<Boolean>` | Drives heart icon toggle |

#### Business Rules Enforced in Use Cases

- `AddToWishlistUseCase` stores a **snapshot** of the product at add-time (title, price, imageUrl) — live price changes do not mutate wishlist entries
- `RemoveFromWishlistUseCase` enforces no internal confirmation — the `ConfirmationDialog` is the ViewModel's responsibility before invoking this use case
- `IsProductInWishlistUseCase` must emit immediately on subscription so the heart icon renders the correct state without delay

---

### 6.5 `cart`

**Responsibility:** Full cart management — add, update quantity, remove items. Enforces real-time stock limits. All totals recalculated locally.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `GetCartUseCase` | `operator fun invoke(userId: String): Flow<Result<Cart>>` | Reactive cart for current user; total recalculated per emission |
| `AddToCartUseCase` | `suspend operator fun invoke(item: CartItem): Result<Unit>` | Validates stock; increments quantity if variant already in cart |
| `RemoveFromCartUseCase` | `suspend operator fun invoke(itemId: String): Result<Unit>` | Deletes a single item |
| `UpdateCartItemQuantityUseCase` | `suspend operator fun invoke(itemId: String, quantity: Int, maxQuantity: Int): Result<Unit>` | Clamps quantity to `[1, maxQuantity]` before delegating |
| `GetCartTotalUseCase` | `operator fun invoke(items: List<CartItem>): Double` | Pure: `Σ(item.price × item.quantity)` |
| `ClearCartUseCase` | `suspend operator fun invoke(userId: String): Result<Unit>` | Removes all cart items for user |
| `ValidateCouponUseCase` | `suspend operator fun invoke(code: String): Result<CouponValidationResult>` | Checks code validity via Shopify price rules |

#### Business Rules Enforced in Use Cases

- `AddToCartUseCase`: if `item.quantity > item.maxQuantity`, return `Result.failure` with a descriptive exception — never silently clamp on add
- `UpdateCartItemQuantityUseCase`: clamp `quantity = quantity.coerceIn(1, maxQuantity)` before calling the repository — the repository receives only valid values
- `GetCartTotalUseCase` is a **pure function** — no repository dependency; takes the current items list and returns a `Double`
- `ClearCartUseCase` and `RemoveFromCartUseCase` do not show dialogs — that is the ViewModel's job

---

### 6.6 `account`

**Responsibility:** User profile, order history, address CRUD with geolocation validation, and currency selection.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `GetUserProfileUseCase` | `suspend operator fun invoke(customerId: String): Result<UserProfile>` | Shopify customer record |
| `GetOrderHistoryUseCase` | `suspend operator fun invoke(customerId: String): Result<List<Order>>` | All past orders |
| `GetOrderDetailUseCase` | `suspend operator fun invoke(orderId: String): Result<Order>` | Full order with line items |
| `GetAddressesUseCase` | `suspend operator fun invoke(customerId: String): Result<List<Address>>` | Saved addresses |
| `AddAddressUseCase` | `suspend operator fun invoke(customerId: String, address: Address): Result<Address>` | Creates address |
| `UpdateAddressUseCase` | `suspend operator fun invoke(customerId: String, address: Address): Result<Address>` | Updates address |
| `DeleteAddressUseCase` | `suspend operator fun invoke(customerId: String, addressId: String): Result<Unit>` | Deletes address |
| `GetCountriesUseCase` | `suspend operator fun invoke(): Result<List<Country>>` | Dynamic country + province list |
| `ValidateAddressUseCase` | `suspend operator fun invoke(address: Address): Result<Address>` | GPS / Places validation; returns enriched address with lat/lng |
| `GetCurrencyRatesUseCase` | `suspend operator fun invoke(base: String): Result<CurrencyRate>` | Live rates; `:data` caches for 1 hour |

#### Business Rules Enforced in Use Cases

- `AddAddressUseCase` and `UpdateAddressUseCase` must call `ValidateAddressUseCase` internally — addresses without valid coordinates must not be saved
- `AddAddressUseCase` required fields before passing to repository: `firstName`, `lastName`, `phone` — validate non-empty before network call
- `GetCountriesUseCase` must never return a hardcoded list — always delegates to the repository which calls the external API
- `GetCurrencyRatesUseCase` returns a `CurrencyRate` with `fetchedAt`; the 1-hour TTL enforcement lives in `:data`'s `LocalDataSource`, but the domain model carries `fetchedAt` so callers can surface staleness if needed

---

### 6.7 `checkout`

**Responsibility:** Order summary, coupon application, shipping address selection, payment method selection, and final order placement.

#### Use Cases

| Class | Signature | Description |
|---|---|---|
| `GetAvailablePaymentMethodsUseCase` | `operator fun invoke(totalPrice: Double): List<PaymentMethod>` | Filters COD based on `MAX_COD_AMOUNT` |
| `ValidateCashLimitUseCase` | `operator fun invoke(totalPrice: Double): Boolean` | Returns `false` if `totalPrice > Constants.MAX_COD_AMOUNT` |
| `PlaceOrderUseCase` | `suspend operator fun invoke(checkout: Checkout): Result<OrderConfirmation>` | Submits order; Shopify triggers confirmation email |

#### Business Rules Enforced in Use Cases

- `GetAvailablePaymentMethodsUseCase` calls `ValidateCashLimitUseCase` internally — `PaymentMethod.CASH_ON_DELIVERY` is only in the returned list when `totalPrice <= Constants.MAX_COD_AMOUNT`
- `PlaceOrderUseCase` must **not** clear the cart — clearing the Room cart after successful order is the ViewModel's responsibility (call `ClearCartUseCase` after `PlaceOrderUseCase` succeeds)
- Invalid coupon results (`Result.failure`) are surfaced as inline field errors in the UI — not as dialogs; this is a presentation concern, but use cases must return descriptive error messages

---

## 7. Coding Standards for `:domain`

### Naming Conventions

| Element | Pattern | Example |
|---|---|---|
| Domain model class | PascalCase `data class` | `CartItem`, `DiscountCode` |
| Repository interface | PascalCase + `Repository` | `CartRepository`, `AuthRepository` |
| Use case class | Verb + Noun + `UseCase` | `AddToCartUseCase`, `ValidateCouponUseCase` |
| Invoke method | `operator fun invoke` | always `invoke`, never `execute` or `run` |
| Enum | PascalCase class, SCREAMING_SNAKE entries | `enum class SortOption { PRICE_LOW_TO_HIGH }` |
| Package | lowercase | `com.shopzen.domain.cart.usecase` |

### Mandatory Rules

- **One class per file** — model, repository, or use case each has its own `.kt` file
- **No `!!` operator** — use `?: return Result.failure(...)` or `?.let { ... }`
- **No companion objects with constants** in use cases — constants belong in `Constants.kt` (in `:domain` root or `:data` as appropriate)
- **KDoc on all public symbols** — classes, interfaces, functions, and non-obvious properties
- **`Result<T>` wraps all fallible operations** — use cases never throw; they return `Result.failure(exception)`
- **`Flow<T>` for reactive streams** — use cases that observe ongoing state return `Flow`, not a one-shot suspend function
- **No `GlobalScope`** — use cases are `suspend` or return `Flow`; coroutine scope is owned by the ViewModel

### SOLID in `:domain`

| Principle | Application |
|---|---|
| **SRP** | Each use case has exactly one `invoke` method and one responsibility |
| **OCP** | Add new behaviour via new use case classes — never modify existing ones |
| **LSP** | `RepositoryImpl` (in `:data`) must be fully substitutable for the interface defined here |
| **ISP** | If a repository interface grows beyond its feature boundary, split it |
| **DIP** | Use cases depend only on repository interfaces — never on concrete implementations |

---

## 8. Testing Strategy

All `:domain` tests live in the `test/` source set of the `:domain` module. No instrumentation tests are needed here.

### Coverage Target: ≥ 90%

### Tools

| Tool | Purpose |
|---|---|
| JUnit 5 | Test runner and assertions |
| MockK | Mocking repository interfaces |
| `kotlinx-coroutines-test` | Testing suspend functions and `Flow` |
| Turbine | Asserting `Flow` emissions in sequence |

### Test Structure per Use Case

Every use case class gets a corresponding `[UseCaseName]Test.kt` file in `test/com/shopzen/domain/{feature}/usecase/`.

```kotlin
class AddToCartUseCaseTest {

    private val cartRepository: CartRepository = mockk()
    private val useCase = AddToCartUseCase(cartRepository)

    @Test
    fun `invoke - when quantity exceeds maxQuantity - returns failure`() { ... }

    @Test
    fun `invoke - when variant already in cart - increments quantity`() { ... }

    @Test
    fun `invoke - on repository error - propagates failure`() { ... }

    @Test
    fun `invoke - happy path - delegates to repository`() { ... }
}
```

### Mandatory Test Cases per Use Case

Every use case must have tests covering:

1. **Happy path** — correct input, repository returns success
2. **Validation failure** — business rule violation caught before repository call
3. **Repository error propagation** — repository returns `Result.failure`; use case propagates it
4. **Loading state** (for `Flow`-returning use cases) — Turbine asserts emission order

### Key Patterns

- Mock only repository interfaces — never concrete classes
- Use `coEvery { repo.method() } returns Result.success(...)` for suspend mocks
- Use `every { repo.method() } returns flowOf(...)` for Flow mocks
- Use `turbineScope { flow.test { ... } }` to assert `Flow` emissions
- Assert that `isLoading`-equivalent states are emitted **before** results (tested in ViewModel layer, but use case `Flow` ordering must support it)
- For pure use cases (`FilterProductsUseCase`, `SortProductsUseCase`, `GetCartTotalUseCase`, `ApplyCouponUseCase`, `RemoveCouponUseCase`): no mocks needed — test with plain inputs and assert outputs

---

*Any change to a domain model, repository interface, or use case contract must be reflected in this document before being implemented in code. Changes here cascade to `:data` (mapper/RepositoryImpl) and `:presentation` (ViewModel).*
