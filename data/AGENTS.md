# DATA_LAYER.md — Shopzen Data Layer Specification

> **Single source of truth** for all data layer architectures, networking implementations, local persistence rules, and data mapping strategies within the `:data` module.
> No deviation from these standards without updating this document and obtaining architectural clearance.

---

## Table of Contents

1. [Architectural Role & Guardrails](#1-architectural-role--guardrails)
2. [Data Layer Package Structure](#2-data-layer-package-structure)
3. [Remote Data Source (Network)](#3-remote-data-source-network)
4. [Local Data Source (Persistence)](#4-local-data-source-persistence)
5. [Data Transfer Objects (DTOs) & Entities](#5-data-transfer-objects-dtos--entities)
6. [Mappers (Data Transformation)](#6-mappers-data-transformation)
7. [Repository Implementations](#7-repository-implementations)
8. [Caching & Offline-First Strategy](#8-caching--offline-first-strategy)
9. [Error Handling & NetworkResult](#9-error-handling--networkresult)
10. [Dependency Injection Configuration](#10-dependency-injection-configuration)

---

## 1. Architectural Role & Guardrails

The `:data` module is responsible for serving as the execution bridge between raw data streams (Shopify APIs, Firebase, Room, DataStore) and the business layer (`:domain`). It encapsulates all implementation details regarding network protocols, caching strategies, database queries, and device hardware APIs.

### Mandatory Enforcement Rules

* **Boundary Isolation:** DTOs (`*Dto`, `*Response`) and Room Entities (`*Entity`) must **never** leak into the `:domain` or `:presentation` modules. They must be transformed into domain models before leaving the repository implementation layer.
* **Dependency Direction:** The `:data` module depends directly on the `:domain` module to implement its repository interfaces. It must **never** depend on or import anything from the `:presentation` or `:app` modules.
* **Threading Isolation:** All data source operations (disk I/O and network requests) must explicitly run on `Dispatchers.IO`. Repositories are responsible for ensuring safety across asynchronous calls.
* **No Framework Spillage:** Third-party network/database components (e.g., `Retrofit`, `ApolloClient`, `Dao`, `FirebaseFirestore`) must remain completely internal to this module.

---

## 2. Data Layer Package Structure

The package design follows a feature-centric structure under `com.shopzen.data`, grouping related remote, local, repository, and mapper files together, alongside a centralized database core.

```
com.shopzen.data/
├── core/
│   ├── network/
│   │   ├── interceptor/       ← ShopifyAuthInterceptor.kt, ConnectivityInterceptor.kt
│   │   ├── RetrofitFactory.kt
│   │   └── ApolloFactory.kt
│   ├── database/
│   │   ├── ShopzenDatabase.kt
│   │   └── Converters.kt
│   └── utils/
│       └── SafeApiCall.kt     ← Extension utilities wrapping network calls
│
├── {feature}/                 ← e.g., catalog, cart, auth, account, checkout
│   ├── remote/
│   │   ├── api/               ← Retrofit endpoints / Apollo operations (.graphql)
│   │   ├── dto/               ← Shopify REST and GraphQL JSON mapping models
│   │   └── Remote{Feature}DataSource.kt / Remote{Feature}DataSourceImpl.kt
│   ├── local/
│   │   ├── dao/               ← Room Data Access Objects
│   │   ├── entity/            ← Room database table definitions
│   │   └── Local{Feature}DataSource.kt / Local{Feature}DataSourceImpl.kt
│   ├── mapper/
│   │   └── {Feature}Mapper.kt ← High-performance DTO/Entity ↔ Domain Model mapping extensions
│   └── repository/
│       └── {Feature}RepositoryImpl.kt
```

---

## 3. Remote Data Source (Network)

The remote sub-layer handles interaction with external web services using two network clients executing against the same Shopify endpoint.

### Network Client Allocation Matrix

| Service Boundary | Protocol | Library | Reason |
|---|---|---|---|
| **Bulk Queries & Feeds** | REST | Retrofit + OkHttp | High performance for cursor-paginated product feeds, vendors, and collections. |
| **Complex Relationships** | GraphQL | Apollo Kotlin (v4) | Optimized single-trip query payloads for complex, deeply nested models (variants, option nodes). |
| **Identity / Social Sign-In** | Custom | Firebase Auth SDK | Native platform implementation for OAuth tokens and verification hooks. |
| **Localization & Rates** | REST | Retrofit | External exchange rate system handling JSON conversions. |Suffix

### Configuration Specs

```
Connect Timeout: 15 Seconds
Read Timeout:    15 Seconds
Write Timeout:   15 Seconds
```

#### Shopify HTTP Authentication Header Interceptor
```kotlin
class ShopifyAuthInterceptor(
    private val apiKey: String,
    private val apiPassword: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = Credentials.basic(apiKey, apiPassword)
        val request = chain.request().newBuilder()
            .header("Authorization", credentials)
            .header("X-Shopify-Access-Token", apiPassword)
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
```

---

## 4. Local Data Source (Persistence)

Local persistence acts as both a caching mechanism for product browsing and an authoritative transactional boundary for state components like the shopping cart.

### Room Database Configuration

```kotlin
@Database(
    entities = [
        CartItemEntity::class,
        WishlistEntity::class,
        ProductEntity::class,
        AddressEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class ShopzenDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun catalogDao(): CatalogDao
    abstract fun addressDao(): AddressDao
}
```

### Data Access Object (DAO) Rules
* **Reactive Streams:** Read operations for Cart and Wishlist metrics must return `Flow<List<Entity>>` to allow changes to update the UI immediately without polling.
* **Write Strategy:** Overwriting entity items must use `@Insert(onConflict = OnConflictStrategy.REPLACE)`.
* **Atomic Modifications:** Multi-step structural DB tasks (e.g., clearing a cart and initializing order drafts) must be wrapped inside a `@Transaction` block to safeguard database consistency.

---

## 5. Data Transfer Objects (DTOs) & Entities

### DTO Modeling Pattern (Shopify API Mapping)

All DTO components must implement strict, explicit parsing schemas using Kotlin serialization annotations to prevent failures caused by obfuscation during the production release build.

```kotlin
@Serializable
data class ProductDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("body_html") val bodyHtml: String?,
    @SerialName("vendor") val vendor: String,
    @SerialName("product_type") val productType: String,
    @SerialName("images") val images: List<ProductImageDto> = emptyList(),
    @SerialName("variants") val variants: List<ProductVariantDto> = emptyList()
)
```

### Entity Modeling Pattern (Room State Schema)

Entities represent structural schema layouts explicitly isolated from network layer mutations.

```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String, // Variant ID or composite key
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "user_id") val userId: String, // Multi-tenant structure support
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "variant_title") val variantTitle: String?,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "max_quantity") val maxQuantity: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 6. Mappers (Data Transformation)

Mappers run as Kotlin file extensions to map schemas across architectural boundaries. They must remain completely stateless.

### Concrete Implementation Sample

```kotlin
fun ProductDto.toDomainModel(): Product {
    return Product(
        id = this.id.toString(),
        title = this.title,
        description = this.bodyHtml.orEmpty(),
        vendor = this.vendor,
        category = this.productType,
        images = this.images.map { it.toDomainModel() },
        variants = this.variants.map { it.toDomainModel() }
    )
}

fun CartItemEntity.toDomainModel(): CartItem {
    return CartItem(
        id = this.id,
        productId = this.productId,
        variantId = this.id,
        title = this.title,
        variantTitle = this.variantTitle.orEmpty(),
        price = this.price,
        quantity = this.quantity,
        maxQuantity = this.maxQuantity,
        imageUrl = this.imageUrl.orEmpty(),
        userId = this.userId
    )
}

fun CartItem.toEntityModel(currentUserId: String): CartItemEntity {
    return CartItemEntity(
        id = this.id,
        productId = this.productId,
        userId = currentUserId,
        title = this.title,
        variantTitle = this.variantTitle,
        price = this.price,
        quantity = this.quantity,
        maxQuantity = this.maxQuantity,
        imageUrl = this.imageUrl
    )
}
```

---

## 7. Repository Implementations

Repositories coordinate operations between multiple underlying data sources, mapping downstream exceptions to structured, semantic domain results.

### Sample Implementation Pattern

```kotlin
class CartRepositoryImpl @Inject constructor(
    private val localDataSource: LocalCartDataSource,
    private val remoteDataSource: RemoteCatalogDataSource,
    private val authSessionProvider: AuthSessionProvider 
) : CartRepository {

    override fun getCart(): Flow<Result<Cart>> {
        return authSessionProvider.userIdFlow.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(Result.failure(Exception("User unauthenticated")))
            } else {
                localDataSource.getCartItemsForUser(userId)
                    .map { entities ->
                        val domainItems = entities.map { it.toDomainModel() }
                        Result.success(Cart.fromItems(domainItems))
                    }
            }
        }.catch { emit(Result.failure(it)) }
    }

    override suspend fun addOrUpdateItem(productId: String, variantId: String, qty: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = authSessionProvider.currentUserId ?: return@withContext Result.failure(Exception("Auth required"))
            
            // Query real-time Shopify inventory metrics to enforce stock limits
            val networkProduct = remoteDataSource.fetchProductVariantStock(variantId)
            val inventoryQuantity = networkProduct.inventoryQuantity
            
            val currentLocalItem = localDataSource.getCartItem(variantId, userId)
            val intendedQty = (currentLocalItem?.quantity ?: 0) + qty

            if (intendedQty > inventoryQuantity) {
                return@withContext Result.failure(IllegalStateException("Requested quantity exceeds available stock ($inventoryQuantity)"))
            }

            localDataSource.upsertCartItem(
                CartItemEntity(
                    id = variantId,
                    productId = productId,
                    userId = userId,
                    quantity = intendedQty,
                    maxQuantity = inventoryQuantity,
                    // Remaining fields populated from product variant reference info
                    title = networkProduct.productTitle,
                    variantTitle = networkProduct.title,
                    price = networkProduct.price,
                    imageUrl = networkProduct.imageUrl
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 8. Caching & Offline-First Strategy

The application enforces a dual persistence strategy optimized for speed during browsing operations, while ensuring transactional integrity for financial steps.

```
[Repository Client Invocation]
        │
        ├──▶ Read Action ──▶ Read Local Cache Database Flow (Instant UI Paint)
        │                         ▲
        │                         └─── Background Network Refresh Async Updates Cache
        │
        └──▶ Mutation Action ──▶ Verify Remote Business Constraint Rules (e.g., Stock)
                                  │
                                  └── [Success] ──▶ Update Local Storage Sync Cache
```

### Strategic Domain Caching Parameters

* **Catalog Syncing Policy:** The `CatalogRepository` fetches cache blocks from `products` table structures on cold launches. When connected to network access blocks, it pulls down updated payloads, saving records into Room to support offline browsing.
* **Wishlist & Cart Boundaries:** Local client records are bound to specific `userId` references. This prevents cross-account visibility when changing login accounts on a single physical device.
* **Exchange Rate Expiry Window:** Foreign currency parameters are stored inside `SharedPreferences` or `DataStore` with explicit generation time flags. Cached data is considered expired after an application window of **1 Hour**.

---

## 9. Error Handling & NetworkResult

Network-facing integrations wrap upstream HTTP responses inside sealed result components to bypass traditional unchecked exception structures.

```kotlin
sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String, val rawException: Throwable? = null) : NetworkResult<Nothing>
    data class Exception(val exception: Throwable) : NetworkResult<Nothing>
}
```

### Network Execution wrapper Strategy
```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            NetworkResult.Success(body)
        } else {
            NetworkResult.Error(code = response.code(), message = response.errorBody()?.string().orEmpty())
        }
    } catch (e: IOException) {
        NetworkResult.Exception(e) // Handles loss of active carrier connection exceptions
    } catch (e: Exception) {
        NetworkResult.Exception(e) // Catch-all for parsing exceptions
    }
}
```

---

## 10. Dependency Injection Configuration

All structural DI bindings for the data layer are compiled within the `:app` entry module components using strict lifecycle assignments.

### Layer Initialization Topography

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(ShopifyAuthInterceptor(BuildConfig.SHOPIFY_API_KEY, BuildConfig.SHOPIFY_PASSWORD))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://${BuildConfig.SHOPIFY_HOSTNAME}/admin/api/${BuildConfig.SHOPIFY_API_VERSION}/")
            .client(okHttpClient)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://${BuildConfig.SHOPIFY_HOSTNAME}/admin/api/${BuildConfig.SHOPIFY_API_VERSION}/graphql.json")
            .okHttpClient(okHttpClient)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShopzenDatabase {
        return Room.databaseBuilder(context, ShopzenDatabase::class.java, "shopzen_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCartDao(db: ShopzenDatabase) = db.cartDao()
    
    @Provides
    fun provideCatalogDao(db: ShopzenDatabase) = db.catalogDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository
}
```