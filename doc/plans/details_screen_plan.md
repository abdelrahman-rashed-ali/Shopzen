# Product Details Screen — Implementation Plan

Implement the full **Product Detail** feature across all four Shopzen modules, using the Shopify REST Admin API (`GET /admin/api/2026-01/products/{product_id}.json`) with Ktor Client, following Clean Architecture + MVI.

---

## User Review Required

> [!IMPORTANT]
> **Package naming inconsistency.** The AGENTS.md specifies `com.shopzen.*` packages, but the existing codebase uses `iti.*` (domain: `iti.domain`, data: `iti.data`) and `com.iti.myapplication` (app/presentation). I will follow the **existing codebase conventions** (`iti.domain`, `iti.data`, `com.iti.myapplication`) to avoid breaking changes. Confirm if you'd like a full rename to `com.shopzen.*` instead — that would be a separate refactoring task.

> [!WARNING]
> **Shopify Access Token.** Your plan mentions storing the token `shpat_90aa8bae...` in `local.properties` → `BuildConfig`. Currently `local.properties` has no token entry and the `:data` module has no `BuildConfig` generation enabled. I will:
> 1. Add the token to `local.properties`
> 2. Configure the `:data` module's `build.gradle.kts` to read it and expose it via `BuildConfig.SHOPIFY_ACCESS_TOKEN`
> 3. Configure the Shopify hostname + API version similarly
>
> **Please confirm the hostname** (e.g. `your-store.myshopify.com`) — I'll use a placeholder `BuildConfig.SHOPIFY_HOSTNAME` for now.

> [!WARNING]
> **`:data` module dependency syntax is broken.** Line 36 has `implementation(":domain")` which is not valid Gradle syntax for a project dependency — it should be `implementation(project(":domain"))`. Same issue in `:presentation`. I'll fix both as part of this work.

---

## Open Questions

1. **Store hostname**: What is your Shopify store hostname? (e.g. `mystore-xyz.myshopify.com`)
2. **API version**: Your plan says `2026-01` — confirm this is correct for your store.
3. **Coil dependency**: The AGENTS.md lists Coil for image loading but it's not in `libs.versions.toml`. Should I add it now, or will images be URLs for now?
4. **Kotlin Android plugin**: The `:data` module is missing the `kotlin-android` plugin — it only has `android.library`. I'll add it.
5. **kotlinx-serialization plugin**: `:data` needs the `kotlinx.serialization` Gradle plugin for `@Serializable` DTOs. I'll add it to the version catalog and `:data` build script.

---

## Proposed Changes

### `:domain` Module — Pure Kotlin Models, Repository Interface, Use Case

#### [NEW] [Product.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/model/Product.kt)
Domain model — pure Kotlin data class, zero Android imports:
```kotlin
data class Product(
    val id: Long,
    val title: String,
    val description: String,       // HTML-stripped plain text
    val vendor: String,
    val productType: String,
    val tags: List<String>,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val options: List<ProductOption>,
    val price: String,             // Display price (first variant price)
    val compareAtPrice: String?,   // Original price for discount display
)
```

#### [NEW] [ProductImage.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/model/ProductImage.kt)
```kotlin
data class ProductImage(
    val id: Long,
    val src: String,
    val alt: String?,
)
```

#### [NEW] [ProductVariant.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/model/ProductVariant.kt)
```kotlin
data class ProductVariant(
    val id: Long,
    val title: String,             // e.g. "Medium / Red"
    val price: String,
    val compareAtPrice: String?,
    val inventoryQuantity: Int,
    val selectedOptions: List<SelectedOption>,
)
```

#### [NEW] [ProductOption.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/model/ProductOption.kt)
```kotlin
data class ProductOption(
    val id: Long,
    val name: String,              // e.g. "Size", "Color"
    val values: List<String>,      // e.g. ["S", "M", "L", "XL"]
)
```

#### [NEW] [SelectedOption.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/model/SelectedOption.kt)
```kotlin
data class SelectedOption(
    val name: String,
    val value: String,
)
```

#### [NEW] [ProductRepository.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/repository/ProductRepository.kt)
Repository interface — suspend function returning `Result<Product>`:
```kotlin
interface ProductRepository {
    suspend fun getProductById(id: Long): Result<Product>
}
```

#### [NEW] [GetProductByIdUseCase.kt](file:///home/yousef/Desktop/Shopzen/domain/src/main/java/iti/domain/product/usecase/GetProductByIdUseCase.kt)
Single-responsibility use case with `operator fun invoke`:
```kotlin
class GetProductByIdUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(productId: Long): Result<Product> {
        return repository.getProductById(productId)
    }
}
```

---

### `:data` Module — DTOs, Remote Data Source, Repository Impl, Mapper

#### [MODIFY] [build.gradle.kts](file:///home/yousef/Desktop/Shopzen/data/build.gradle.kts)
- Add `kotlin-android` plugin
- Add `kotlinx.serialization` plugin
- Fix `implementation(":domain")` → `implementation(project(":domain"))`
- Add Ktor bundle dependency
- Add `BuildConfig` generation for `SHOPIFY_ACCESS_TOKEN`, `SHOPIFY_HOSTNAME`, `SHOPIFY_API_VERSION`
- Enable `buildFeatures { buildConfig = true }`

#### [NEW] [ProductResponseDto.kt](file:///home/yousef/Desktop/Shopzen/data/src/main/java/iti/data/product/remote/dto/ProductResponseDto.kt)
Ktor/Kotlinx Serialization DTO matching the exact Shopify REST JSON shape:
```kotlin
@Serializable
data class ProductResponseDto(
    val product: ProductDto
)

@Serializable
data class ProductDto(
    val id: Long,
    val title: String,
    @SerialName("body_html") val bodyHtml: String? = null,
    val vendor: String = "",
    @SerialName("product_type") val productType: String = "",
    val tags: String = "",
    val variants: List<VariantDto> = emptyList(),
    val options: List<OptionDto> = emptyList(),
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class VariantDto(
    val id: Long,
    val title: String = "",
    val price: String = "0.00",
    @SerialName("compare_at_price") val compareAtPrice: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int = 0,
    val option1: String? = null,
    val option2: String? = null,
    val option3: String? = null,
)

@Serializable
data class OptionDto(
    val id: Long,
    val name: String = "",
    val values: List<String> = emptyList(),
)

@Serializable
data class ImageDto(
    val id: Long,
    val src: String = "",
    val alt: String? = null,
)
```

#### [NEW] [ProductMapper.kt](file:///home/yousef/Desktop/Shopzen/data/src/main/java/iti/data/product/mapper/ProductMapper.kt)
Extension functions mapping DTOs → Domain models. Includes HTML stripping for `body_html` and comma-split for tags:
```kotlin
fun ProductDto.toDomain(): Product { ... }
fun VariantDto.toDomain(options: List<OptionDto>): ProductVariant { ... }
fun OptionDto.toDomain(): ProductOption { ... }
fun ImageDto.toDomain(): ProductImage { ... }
```

#### [NEW] [ProductRemoteDataSource.kt](file:///home/yousef/Desktop/Shopzen/data/src/main/java/iti/data/product/remote/ProductRemoteDataSource.kt)
Ktor `HttpClient` call to `GET /admin/api/{version}/products/{id}.json`:
```kotlin
class ProductRemoteDataSource(private val client: HttpClient) {
    suspend fun getProductById(id: Long): ProductResponseDto {
        return client.get("admin/api/${BuildConfig.SHOPIFY_API_VERSION}/products/$id.json")
            .body()
    }
}
```

#### [NEW] [ProductRepositoryImpl.kt](file:///home/yousef/Desktop/Shopzen/data/src/main/java/iti/data/product/repository/ProductRepositoryImpl.kt)
Implements `ProductRepository`, wraps remote call in `runCatching` and maps DTO → Domain:
```kotlin
class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    override suspend fun getProductById(id: Long): Result<Product> {
        return runCatching {
            remoteDataSource.getProductById(id).product.toDomain()
        }
    }
}
```

---

### `:presentation` Module — MVI State, Intent, ViewModel, Compose UI

#### [MODIFY] [build.gradle.kts](file:///home/yousef/Desktop/Shopzen/presentation/build.gradle.kts)
- Fix `implementation(":domain")` → `implementation(project(":domain"))`
- Add `kotlin-android` plugin
- Add Hilt dependencies (for `@HiltViewModel`)
- Add KSP plugin
- Add lifecycle-compose and Coil dependencies

#### [NEW] [ProductDetailState.kt](file:///home/yousef/Desktop/Shopzen/presentation/src/main/java/com/iti/myapplication/ui/product/state/ProductDetailState.kt)
MVI state with defaults per convention:
```kotlin
data class ProductDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val product: Product? = null,
    val selectedVariantId: Long? = null,
    val showSizeRequiredError: Boolean = false,
)
```

#### [NEW] [ProductDetailIntent.kt](file:///home/yousef/Desktop/Shopzen/presentation/src/main/java/com/iti/myapplication/ui/product/intent/ProductDetailIntent.kt)
MVI sealed class for all user interactions:
```kotlin
sealed class ProductDetailIntent {
    data class LoadProduct(val productId: Long) : ProductDetailIntent()
    data class SelectVariant(val variantId: Long) : ProductDetailIntent()
    object AddToCart : ProductDetailIntent()
}
```

#### [NEW] [ProductDetailViewModel.kt](file:///home/yousef/Desktop/Shopzen/presentation/src/main/java/com/iti/myapplication/ui/product/viewmodel/ProductDetailViewModel.kt)
`@HiltViewModel` with `MutableStateFlow<ProductDetailState>`, processes intents:
- `LoadProduct` → calls `GetProductByIdUseCase` → updates state
- `SelectVariant` → updates `selectedVariantId`, clears size error
- `AddToCart` → validates variant selection, shows error if none selected

#### [NEW] [ProductDetailScreen.kt](file:///home/yousef/Desktop/Shopzen/presentation/src/main/java/com/iti/myapplication/ui/product/screen/ProductDetailScreen.kt)
Stateless Compose screen receiving `state` and `onIntent`:
- **Image Gallery**: `HorizontalPager` with animated dot indicators
- **Product Info**: Title, vendor, price with discount strikethrough
- **Size Selector**: `FlowRow` of `FilterChip`s — disabled when `inventoryQuantity == 0`
- **Description**: HTML-stripped product body
- **Add to Cart Button**: Full-width Material 3 button, shows size-required error if no variant selected
- **Loading/Error States**: Centered `CircularProgressIndicator` / Error text with retry

#### [NEW] [ImagePagerIndicator.kt](file:///home/yousef/Desktop/Shopzen/presentation/src/main/java/com/iti/myapplication/ui/product/components/ImagePagerIndicator.kt)
Animated dot indicator for the image pager.

---

### `:app` Module — DI Wiring & Navigation

#### [MODIFY] [build.gradle.kts](file:///home/yousef/Desktop/Shopzen/app/build.gradle.kts)
- Add `implementation(project(":domain"))`, `implementation(project(":data"))`, `implementation(project(":presentation"))`

#### [MODIFY] [RemoteModule.kt](file:///home/yousef/Desktop/Shopzen/app/src/main/java/com/iti/myapplication/di/RemoteModule.kt)
Provide Ktor `HttpClient` configured with:
- `ContentNegotiation` + `KotlinxSerializer(ignoreUnknownKeys = true)`
- `defaultRequest { url("https://${BuildConfig.SHOPIFY_HOSTNAME}/") }` + `X-Shopify-Access-Token` header
- `Logging` plugin for debug builds
- Provide `ProductRemoteDataSource`

#### [NEW] [RepositoryModule.kt](file:///home/yousef/Desktop/Shopzen/app/src/main/java/com/iti/myapplication/di/RepositoryModule.kt)
`@Binds` `ProductRepositoryImpl` to `ProductRepository` interface.

#### [NEW] [UseCaseModule.kt](file:///home/yousef/Desktop/Shopzen/app/src/main/java/com/iti/myapplication/di/UseCaseModule.kt)
Provide `GetProductByIdUseCase` with injected `ProductRepository`.

#### [MODIFY] [NavScreen.kt](file:///home/yousef/Desktop/Shopzen/app/src/main/java/com/iti/myapplication/navigation/NavScreen.kt)
Add `ProductDetail` route: `main/products/{productId}` with `Long` nav argument.

#### [MODIFY] [AppNavHost.kt](file:///home/yousef/Desktop/Shopzen/app/src/main/java/com/iti/myapplication/navigation/AppNavHost.kt)
Register `ProductDetailScreen` composable destination with `productId` argument extraction.

---

### Gradle Infrastructure

#### [MODIFY] [libs.versions.toml](file:///home/yousef/Desktop/Shopzen/gradle/libs.versions.toml)
Add:
- `kotlinx-serialization` plugin
- `coil-compose` library (for image loading)
- `lifecycle-runtime-compose` / `lifecycle-viewmodel-compose` for `collectAsStateWithLifecycle`

#### [MODIFY] [build.gradle.kts (root)](file:///home/yousef/Desktop/Shopzen/build.gradle.kts)
Add `kotlin-android` plugin declaration with `apply false`.

#### [MODIFY] [local.properties](file:///home/yousef/Desktop/Shopzen/local.properties)
Add Shopify credentials:
```properties
SHOPIFY_ACCESS_TOKEN=************
SHOPIFY_HOSTNAME=your-store.myshopify.com
SHOPIFY_API_VERSION=2026-01
```

---

## File Summary

| Module | Action | File | Purpose |
|--------|--------|------|---------|
| `:domain` | NEW | `product/model/Product.kt` | Main product model |
| `:domain` | NEW | `product/model/ProductImage.kt` | Image model |
| `:domain` | NEW | `product/model/ProductVariant.kt` | Variant model |
| `:domain` | NEW | `product/model/ProductOption.kt` | Option model (Size/Color) |
| `:domain` | NEW | `product/model/SelectedOption.kt` | Selected option pair |
| `:domain` | NEW | `product/repository/ProductRepository.kt` | Repository interface |
| `:domain` | NEW | `product/usecase/GetProductByIdUseCase.kt` | Use case |
| `:data` | MODIFY | `build.gradle.kts` | Add plugins, deps, BuildConfig |
| `:data` | NEW | `product/remote/dto/ProductResponseDto.kt` | Shopify JSON DTOs |
| `:data` | NEW | `product/mapper/ProductMapper.kt` | DTO → Domain mappers |
| `:data` | NEW | `product/remote/ProductRemoteDataSource.kt` | Ktor API calls |
| `:data` | NEW | `product/repository/ProductRepositoryImpl.kt` | Repository impl |
| `:presentation` | MODIFY | `build.gradle.kts` | Add Hilt, KSP, Coil deps |
| `:presentation` | NEW | `ui/product/state/ProductDetailState.kt` | MVI state |
| `:presentation` | NEW | `ui/product/intent/ProductDetailIntent.kt` | MVI intents |
| `:presentation` | NEW | `ui/product/viewmodel/ProductDetailViewModel.kt` | ViewModel |
| `:presentation` | NEW | `ui/product/screen/ProductDetailScreen.kt` | Main UI |
| `:presentation` | NEW | `ui/product/components/ImagePagerIndicator.kt` | Dot indicator |
| `:app` | MODIFY | `build.gradle.kts` | Add module deps |
| `:app` | MODIFY | `di/RemoteModule.kt` | Provide HttpClient, DataSource |
| `:app` | NEW | `di/RepositoryModule.kt` | Bind repository |
| `:app` | NEW | `di/UseCaseModule.kt` | Provide use case |
| `:app` | MODIFY | `navigation/NavScreen.kt` | Add product route |
| `:app` | MODIFY | `navigation/AppNavHost.kt` | Register destination |
| gradle | MODIFY | `libs.versions.toml` | Add new deps |
| gradle | MODIFY | `build.gradle.kts` (root) | Add plugins |
| — | MODIFY | `local.properties` | Add Shopify creds |

---

## Verification Plan

### Automated Tests
- Run `./gradlew :domain:build` — verify pure Kotlin module compiles with no Android deps
- Run `./gradlew :data:build` — verify DTOs, mappers, and data source compile
- Run `./gradlew :presentation:build` — verify Compose UI and ViewModel compile
- Run `./gradlew :app:assembleDebug` — full integration build

### Manual Verification
- Launch the app and navigate to `main/products/{productId}` with a known Shopify product ID
- Verify image pager loads and swipes between images with dot indicators
- Verify size chips render and disabled variants show as unclickable
- Verify "Add to Cart" shows size-required error when no variant is selected
- Verify error state displays on network failure with retry
