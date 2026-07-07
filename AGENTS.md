# AGENTS.md — Shopzen

> **Single source of truth** for all architectural, organizational, and implementation decisions.
> No deviation from these guidelines without updating this document first.

Reference design: <https://pocket-shop-style.lovable.app>

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Architecture](#3-architecture)
4. [Module & Package Structure](#4-module--package-structure)
5. [MVI Pattern](#5-mvi-pattern)
6. [Feature Map](#6-feature-map)
7. [Shopify API Integration](#7-shopify-api-integration)
8. [Local Database — Room](#8-local-database--room)
9. [Dependency Injection — Hilt](#9-dependency-injection--hilt)
10. [Navigation](#10-navigation)
11. [Confirmation Dialog Pattern](#11-confirmation-dialog-pattern)
12. [Coding Standards](#12-coding-standards)
13. [Testing Strategy](#13-testing-strategy)
14. [Visual Identity](#14-visual-identity)
15. [Secrets & BuildConfig](#15-secrets--buildconfig)
16. [Collaboration & Git](#16-collaboration--git)

---

## 1. Project Overview

**Shopzen** is an Android e-commerce client backed by the **Shopify API** (REST + GraphQL). Supports guest browsing plus full authenticated flows: wishlist, cart, checkout, addresses, and order history.

### Core Business Rules

| Rule | Detail |
|---|---|
| Guest Access | Browse products and categories freely without signing in |
| Auth Gate | Cart and Wishlist require authentication; unauthenticated users are redirected to Login |
| Shopify Auth | Basic HTTP authentication over TLS for all Shopify API calls |
| Destructive Actions | All delete / logout / order-submit actions require a `ConfirmationDialog` |
| Stock Enforcement | Cart item quantity cannot exceed real-time Shopify `inventoryQuantity` |
| COD Limit | Cash on Delivery is blocked when `totalPrice > Constants.MAX_COD_AMOUNT` |
| Address Validation | Addresses must be validated via GPS, Google Places autocomplete, HERE Maps, or Mapbox search/map pick |
| Verification Email | Sent automatically on successful registration |
| Order Email | Shopify sends a confirmation email automatically on order creation with `send_receipt: true` |

---

## 2. Tech Stack

| Concern | Technology |
|---|---|
| Platform | Android — minSdk 26, targetSdk 35 |
| Language | Kotlin |
| UI | Jetpack Compose (no XML layouts) |
| Design System | Material 3 |
| Architecture | Clean Architecture + Multi-Module + Feature-Based packages |
| UI Pattern | MVI |
| DI | Hilt |
| Networking | Ktor Client (REST) · Apollo Android (GraphQL) |
| Auth | Firebase Auth (Email/Password + Google Sign-In) |
| Local DB | Room |
| Async | Kotlin Coroutines + Flow + StateFlow |
| Navigation | Jetpack Compose Navigation |
| Address | Google Places API, HERE Maps SDK, or Mapbox Search/Maps SDK |
| Currency | External exchange-rate API (e.g. Open Exchange Rates) |
| Payment Gateway | Paymob Mobile SDK + Ktor intention API |
| Images | Coil |
| Testing | JUnit 5, MockK, kotlinx-coroutines-test, Turbine |

---

## 3. Architecture

```
:presentation ──▶ :domain ◀── :data
                    ▲
              :app wires everything
```

Four Gradle modules. Dependencies flow in one direction only. The `:app` module is the only one that knows about all others — it owns wiring (DI, navigation, entry points) but contains no business logic.

| Module | Role | Must NOT contain |
|---|---|---|
| `:domain` | Pure Kotlin models, repository interfaces, use cases | `android.*`, Retrofit, Room, Firebase |
| `:data` | DTOs, Entities, `RepositoryImpl`, API services, DAOs, mappers | Domain models used without mapping |
| `:presentation` | Composables, ViewModels, MVI State + Intent | Direct repository calls, business logic |
| `:app` | `ShopzenApp`, `MainActivity`, DI modules, `AppNavGraph`, `Routes` | Business logic, use cases, data sources, UI components |

### Mandatory Enforcement

- `:domain` — zero imports from `android.*`, `retrofit2.*`, `androidx.room.*`
- `:data` — DTOs and Room entities never leave the module without being mapped first
- `:presentation` — ViewModels call only UseCases, never repositories directly
- `:app` — no business logic; wires and launches only
- Composable functions contain zero business logic

---

## 4. Module & Package Structure

### `:domain` module

```
com.shopzen.domain/
└── {feature}/
    ├── model/          ← Pure Kotlin data classes; no annotations
    ├── repository/     ← Interfaces only
    └── usecase/        ← One class, one public `operator fun invoke`
```

### `:data` module

```
com.shopzen.data/
├── {feature}/
│   ├── remote/
│   │   ├── api/                    ← Ktor request contracts + Apollo operations (.graphql)
│   │   ├── dto/                    ← JSON/GraphQL response shapes
│   │   └── Remote{Feature}DataSource.kt
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── Local{Feature}DataSource.kt
│   ├── repository/
│   │   └── {Feature}RepositoryImpl.kt
│   └── mapper/
│       └── {Feature}Mapper.kt      ← DTO/Entity ↔ Domain model
└── database/
    └── ShopzenDatabase.kt
```

### `:presentation` module

```
com.shopzen.presentation/
├── common/
│   ├── theme/
│   │   ├── ShopzenTheme.kt
│   │   ├── Color.kt
│   │   └── Typography.kt
│   └── components/             ← Shared across features
│       ├── ConfirmationDialog.kt
│       ├── LoadingIndicator.kt
│       ├── ErrorScreen.kt
│       ├── EmptyStateView.kt
│       └── ProductCard.kt
└── {feature}/
    ├── screen/         ← Stateless Composable entry points
    ├── components/     ← Feature-scoped reusable UI
    ├── viewmodel/      ← @HiltViewModel
    ├── state/          ← data class with defaults
    └── intent/         ← sealed class
```

### `:app` module

```
com.shopzen.app/
├── ShopzenApp.kt               ← @HiltAndroidApp Application class
├── MainActivity.kt             ← Single activity; hosts NavHost
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UseCaseModule.kt
│   └── FirebaseModule.kt
└── navigation/
    ├── AppNavGraph.kt      ← Single NavHost; all routes registered here
    └── Routes.kt
```

**Features:** `auth`, `catalog`, `search`, `wishlist`, `cart`, `account`, `checkout`

---

## 5. MVI Pattern

Applied uniformly across every feature screen.

```
User Action
    │
    ▼
Intent (sealed class)
    │
    ▼
ViewModel.processIntent()
    │  (calls UseCase, updates state)
    ▼
StateFlow<FeatureState> (data class)
    │
    ▼
Composable (stateless renderer)
```

### Contracts

- **Intent** — `sealed class`; every user or system event is an explicit branch
- **State** — `data class` with default values; updated via `copy()`
- **ViewModel** — holds a single `MutableStateFlow<FeatureState>`; launches coroutines only in `viewModelScope`
- **Composable** — receives `state` and `onIntent: (Intent) -> Unit`; zero business logic

### State Fields Convention

Every feature state includes: `isLoading: Boolean`, `error: String?`, and `show*Dialog: Boolean` flags for each destructive action. Dialog-pending IDs (e.g. `pendingRemovalItemId`) are stored in state alongside their flag.

---

## 6. Feature Map

### Screens & Routes

| Feature | Screen | Route | Auth |
|---|---|---|---|
| **auth** | SplashScreen | `auth/splash` | No |
| | LoginScreen | `auth/login` | No |
| | RegisterScreen | `auth/register` | No |
| | EmailVerificationScreen | `auth/verify-email` | No |
| **catalog** | HomeScreen | `main/home` | No |
| | BrandListScreen | `main/brands` | No |
| | BrandProductsScreen | `main/brands/{brandName}` | No |
| | ProductListScreen | `main/products` | No |
| | ProductDetailScreen | `main/products/{productId}` | No |
| **search** | SearchScreen | `main/search` | No |
| **wishlist** | WishlistScreen | `main/wishlist` | **Yes** |
| **cart** | CartScreen | `main/cart` | **Yes** |
| **account** | ProfileScreen | `main/profile` | **Yes** |
| | OrderHistoryScreen | `main/profile/orders` | **Yes** |
| | OrderDetailScreen | `main/profile/orders/{orderId}` | **Yes** |
| | AddressListScreen | `main/profile/addresses` | **Yes** |
| | AddressFormScreen | `main/profile/addresses/form?id={id}` | **Yes** |
| | SettingsScreen | `main/settings` | No |
| **checkout** | CheckoutScreen | `checkout/summary` | **Yes** |
| | PaymentScreen | `checkout/payment` | **Yes** |
| | OrderConfirmationScreen | `checkout/confirmation/{orderId}` | **Yes** |

---

### Feature: `auth`

**Responsibility:** Identity management — email/password login, Google Sign-In, registration, email verification, logout, and auth-gated routing.

**Domain models:** `User(uid, email, displayName, photoUrl, isEmailVerified)`

**Use cases:**

| Use Case | Output | Description |
|---|---|---|
| `LoginWithEmailUseCase` | `Flow<Result<User>>` | Firebase email/password sign-in |
| `LoginWithGoogleUseCase` | `Flow<Result<User>>` | Firebase Google credential sign-in |
| `RegisterWithEmailUseCase` | `Flow<Result<User>>` | Creates account + triggers verification email |
| `SendVerificationEmailUseCase` | `Result<Unit>` | Sends email to current user |
| `LogoutUseCase` | `Result<Unit>` | Signs out; clears local session data |
| `IsUserLoggedInUseCase` | `Boolean` | True if a Firebase session exists |
| `GetCurrentUserUseCase` | `User?` | Returns the mapped current Firebase user |

**Business rules:**
- `SplashScreen` calls `IsUserLoggedInUseCase` and routes to `HomeScreen` or `LoginScreen`
- Unauthenticated users tapping Cart or Wishlist are redirected to `LoginScreen` (pass `returnRoute`)
- Registration → send verification email automatically → navigate to `EmailVerificationScreen`
- Logout always requires `ConfirmationDialog` before calling `LogoutUseCase`
- Google Sign-In is launched at the Activity level; the resulting `idToken` is passed to `LoginWithGoogleUseCase`

---

### Feature: `catalog`

**Responsibility:** Browse the Shopify product catalog — home feed, brands, categories, product list, product detail with image gallery, size selection, and reviews.

**Domain models:** `Product`, `ProductImage`, `ProductVariant`, `ProductOption`, `SelectedOption`, `Brand`, `Category`, `SubCategory`, `Review`

**Key domain model fields:**
- `ProductVariant` includes `inventoryQuantity` (enforces stock limit) and `selectedOptions`
- `Product` includes `options: List<ProductOption>` (drives size selector)

**Use cases & data sources:**

| Use Case | Source | Description |
|---|---|---|
| `GetProductsUseCase` | REST — `GET /products.json?limit=50` | Cursor-paginated list |
| `GetProductByIdUseCase` | GraphQL — `product(id:)` query | Full detail with variants/images |
| `GetProductsByBrandUseCase` | REST — `GET /products.json?vendor={name}` | Products by vendor |
| `GetProductsByCategoryUseCase` | REST — `GET /products.json?product_type={type}` | Products by type |
| `GetBrandsUseCase` | REST — `GET /products.json?fields=vendor` | Distinct vendors |
| `GetCategoriesUseCase` | REST — `GET /custom_collections.json` | Collections as categories |
| `GetProductReviewsUseCase` | External reviews API | Reviews by product ID |

**Business rules:**
- `HomeScreen` shows: featured banner, brand grid, category chips, new arrivals
- Product images use `HorizontalPager` with dot indicators
- If `ProductOption.name == "Size"`, user must select a size before adding to cart
- Out-of-stock variants (`inventoryQuantity == 0`) render as disabled chips
- Unauthenticated "Add to Cart" / wishlist heart → redirect to `LoginScreen`
- Products cached in Room (`ProductEntity`) for offline browsing

---

### Feature: `search`

**Responsibility:** Global product search with filtering (category, sub-category, brand) and sorting.

**Domain models:** `SearchFilter(query, mainCategory, subCategory, brand)`, `SortOption(DEFAULT, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW, BEST_SELLER, BY_SUB_CATEGORY)`

**Use cases:**

| Use Case | Source | Description |
|---|---|---|
| `SearchProductsUseCase` | REST — `GET /products.json?title={query}` | Text search |
| `FilterProductsUseCase` | Client-side | Applies `SearchFilter` to loaded list |
| `SortProductsUseCase` | Client-side | Sorts by `SortOption` |

**Business rules:**
- Search input debounced 300 ms
- Filter then sort: apply filter first, then sort the filtered result
- Active filters shown as dismissable chips above the result grid

---

### Feature: `wishlist`

**Responsibility:** Save and remove favourite products. Persisted in Room per `userId`. Heart icon reflects live state on `ProductCard` and `ProductDetailScreen`.

**Domain models:** `WishlistItem(id, productId, title, vendor, price, imageUrl, userId, addedAt)`

**Use cases:**

| Use Case | Output | Description |
|---|---|---|
| `GetWishlistUseCase` | `Flow<List<WishlistItem>>` | Reactive stream for current user |
| `AddToWishlistUseCase` | `Result<Unit>` | Inserts snapshot into Room |
| `RemoveFromWishlistUseCase` | `Result<Unit>` | Deletes item — confirmation required |
| `IsProductInWishlistUseCase` | `Flow<Boolean>` | Drives heart icon toggle |

**Business rules:**
- Items stored with `userId`; multi-user devices show the correct list
- Removing an item always shows `ConfirmationDialog`
- Heart icon on `ProductDetailScreen` reacts to `IsProductInWishlistUseCase`

---

### Feature: `cart`

**Responsibility:** Full cart management — add, update, remove items. Enforces real-time stock limits. Totals recalculated locally. Stored in Room; optionally synced as Shopify Draft Order.

**Domain models:** `Cart(items, currency, subtotalPrice, discountAmount, totalPrice, appliedCoupon, userId)`, `CartItem(id, productId, variantId, title, variantTitle, price, quantity, maxQuantity, imageUrl, userId)`, `DiscountCode(code, discountType, value)`, `DiscountType(PERCENTAGE | FIXED_AMOUNT)`

**Use cases:**

| Use Case | Description |
|---|---|
| `GetCartUseCase` | `Flow<Result<Cart>>` for current user; total recalculated on each emission |
| `AddToCartUseCase` | Validates stock before insert; updates quantity if variant already in cart |
| `RemoveFromCartUseCase` | Deletes item — confirmation required |
| `UpdateCartItemQuantityUseCase` | Clamps quantity to `[1, maxQuantity]` before saving |
| `GetCartTotalUseCase` | Pure: `sum(item.price * item.quantity)` minus discount |
| `ClearCartUseCase` | Deletes all items for user — confirmation required |
| `ValidateCouponUseCase` | REST — `GET /price_rules/{id}/discount_codes.json` Checks code validity |
| `ApplyCouponUseCase` | Client-side Deducts discount; updates CartState |
| `RemoveCouponUseCase` | Client-side Restores original subtotal |

**Business rules:**
- Quantity increment disabled when `quantity == maxQuantity`
- Cart badge on bottom nav shows item count reactively
- Total recalculated on every mutation without a network call
- Apply Coupon: Users can apply a discount code. The app must validate it against the Shopify REST API (GET /price_rules/{id}/discount_codes.json). If valid, it deducts the discount and updates the checkout state.
- Remove Coupon: Users can remove an applied coupon, which restores the original subtotal.
- Error Handling (Coupons): If a user enters an invalid coupon, the app must show an inline field error (do not use a dialog for this).

---

### Feature: `account`

**Responsibility:** User profile, order history, address CRUD with geolocation validation, currency selection.

**Domain models:** `UserProfile`, `Order`, `OrderLineItem`, `Address(id?, firstName, lastName, phone, address1, city, province, country, countryCode, zip, latitude, longitude)`, `Country`, `Province`, `CurrencyRate(base, rates, fetchedAt)`

`Order` includes `subtotalPrice`, `discountAmount`, optional `discountCode`, `totalPrice`, and `paymentMethod` so order history and details show the same final pricing and payment selection users saw at checkout.

**Use cases & data sources:**

| Use Case | Source | Description |
|---|---|---|
| `GetUserProfileUseCase` | REST — `GET /customers/{id}.json` | Shopify customer record |
| `GetOrderHistoryUseCase` | REST — `GET /customers/{id}/orders.json` | All past orders |
| `GetOrderDetailUseCase` | REST — `GET /orders/{order_id}.json` | Full order with line items |
| `GetAddressesUseCase` | REST — `GET /customers/{id}/addresses.json` | Saved addresses |
| `AddAddressUseCase` | REST — `POST /customers/{id}/addresses.json` | Creates address |
| `UpdateAddressUseCase` | REST — `PUT /customers/{id}/addresses/{addr_id}.json` | Updates address |
| `DeleteAddressUseCase` | REST — `DELETE /customers/{id}/addresses/{addr_id}.json` | Deletes — confirmation required |
| `GetCountriesUseCase` | External countries API | Dynamic country + province list |
| `ValidateAddressUseCase` | Google Places / HERE Maps / Mapbox | GPS, autocomplete, or map-pick validation |
| `GetCurrencyRatesUseCase` | External exchange rate API | Live rates; cached 1 hour |

**Business rules:**
- `AddressFormScreen` required fields: `firstName`, `lastName`, `phone`
- Country dropdown populated from `GetCountriesUseCase` — never hardcoded
- Selected currency persisted in `DataStore`; applied globally to all price displays
- `ProfileScreen` shows: personalized greeting, recent order count, `WishlistPreviewGrid` (max 4 items)
- `SettingsScreen` is separate from `ProfileScreen`; it is available to guests and stores preferences locally when unauthenticated
- `OrderHistoryScreen` and `OrderDetailScreen` show payment method plus discount code/amount when present.

---

### Feature: `checkout`

**Responsibility:** Order summary, coupon application, shipping address selection, payment method, and final order placement.

**Domain models:** `Checkout(lineItems, shippingAddress, subtotalPrice, discountAmount, totalPrice, currency, appliedCoupon, selectedPaymentMethod)`, `PaymentMethod(CASH_ON_DELIVERY | ONLINE_PAYMENT)`, `OrderConfirmation`

**Use cases:**

| Use Case | Source | Description |
|---|---|---|
| `GetAvailablePaymentMethodsUseCase` | Client-side | Filters COD based on `MAX_COD_AMOUNT` |
| `ValidateCashLimitUseCase` | Client-side | Returns `false` if `totalPrice > Constants.MAX_COD_AMOUNT` |
| `CreatePaymobPaymentIntentionUseCase` | Ktor — Paymob `POST /v1/intention/` | Creates the client secret for Paymob card SDK checkout |
| `PlaceOrderUseCase` | GraphQL — `orderCreate` mutation | Submits order; Shopify triggers confirmation email |

**Business rules:**
- COD shown only when `totalPrice <= Constants.MAX_COD_AMOUNT`
- Online payment uses Paymob Mobile SDK. The app creates a Paymob intention through `:data` using Ktor, launches the SDK from `:presentation`, and creates the Shopify order only after the SDK reports success.
- Order creation persists the selected payment method and actual discount amount in the Shopify order payload so account order history remains accurate.
- Order placement requires `ConfirmationDialog`
- After successful order: clear Room cart → navigate to `OrderConfirmationScreen`

---

## 7. Shopify API Integration

Shopzen uses **both** the Shopify REST Admin API and the Shopify GraphQL Admin API. Choose the appropriate transport per operation (see Feature Map use case tables). Both transports share the same hostname and credentials.

### Endpoints

| Transport | Base URL |
|---|---|
| REST | `https://{hostname}/admin/api/{version}/{resource}.json` |
| GraphQL | `https://{hostname}/admin/api/{version}/graphql.json` |

All credentials come from `BuildConfig`. REST calls use Ktor clients configured in `NetworkModule`; Shopify Admin requests send `X-Shopify-Access-Token`, and Paymob intention requests send `Authorization: Token {secret}`.

### REST — Key Endpoints

| Resource | Method | Path |
|---|---|---|
| All products | GET | `/products.json?limit=50` |
| Products by vendor | GET | `/products.json?vendor={name}` |
| Products by type | GET | `/products.json?product_type={type}` |
| Custom collections | GET | `/custom_collections.json` |
| Customer record | GET | `/customers/{id}.json` |
| Customer orders | GET | `/customers/{id}/orders.json` |
| Single order | GET | `/orders/{order_id}.json` |
| Customer addresses | GET/POST | `/customers/{id}/addresses.json` |
| Single address | PUT/DELETE | `/customers/{id}/addresses/{addr_id}.json` |
| Discount codes | GET | `/price_rules/{id}/discount_codes.json` |
| Create draft order | POST | `/draft_orders.json` |

### GraphQL — Key Operations

| Operation | Type | Description |
|---|---|---|
| `product(id:)` | Query | Full product detail with variants and images |
| `orderCreate` | Mutation | Place a new order |

Place `.graphql` files in `data/{feature}/remote/api/`. Use Apollo Android for type-safe GraphQL — generated types live alongside the operation files.

### Error Handling

All network calls are wrapped in `NetworkResult<T>` (`Success`, `Error`, `Loading`) inside `RemoteDataSource`. Use the `safeApiCall {}` extension — it catches `IOException` and general exceptions and converts them to `NetworkResult.Error`. `NetworkResult` is defined in `:data`.

---

## 8. Local Database — Room

### Entities

| Entity | Table | Feature |
|---|---|---|
| `CartItemEntity` | `cart_items` | cart |
| `WishlistEntity` | `wishlist_items` | wishlist |
| `ProductEntity` | `products` | catalog (offline cache) |
| `AddressEntity` | `addresses` | account |

All entities include a `userId` field where user-specific data is required.

### Rules

- DAOs are accessed **only** through their `LocalDataSource` — never injected directly into repositories
- Entities are mapped to domain models via `mapper/` before leaving `RepositoryImpl`
- `AppDatabase` is named `ShopzenDatabase`; defined in `data/database/ShopzenDatabase.kt`
- Firebase Auth links to Shopify customer records by **email address**

---

## 9. Dependency Injection — Hilt

All DI modules live in `:app/di/`. They are the only place that imports from both `:data` and `:domain`.

| Module | Provides |
|---|---|
| `NetworkModule` | Ktor `HttpClient` instances, `ApolloClient`, network configuration objects (`@Singleton`) |
| `DatabaseModule` | `ShopzenDatabase`, all DAOs |
| `RepositoryModule` | `@Binds` — each `RepositoryImpl` bound to its interface (`@Singleton`) |
| `UseCaseModule` | Use case instances where manual wiring is needed |
| `FirebaseModule` | `FirebaseAuth`, `GoogleSignInClient` |

- `ShopzenApp` is annotated `@HiltAndroidApp`
- All repositories are `@Singleton`
- ViewModels are `@HiltViewModel` with `@Inject constructor`
- Never instantiate concrete classes manually — always inject interfaces

---

## 10. Navigation

Single-Activity architecture. `MainActivity` (in `:app`) hosts one `NavHost` backed by a **single nav graph** — `AppNavGraph.kt`. All destinations are registered flat in that one file; there are no nested sub-graphs. `Routes.kt` holds all route strings as constants.

### Navigation Flow

```
AppNavGraph (single graph — all destinations registered flat)
│
├── auth/splash  ──▶  [authenticated] ──▶  main/home
│                └──  [no session]    ──▶  auth/login
│                                           ├── ──▶  auth/register ──▶  auth/verify-email
│                                           └── [success] ──▶  main/home
│
├── main/home ──▶  main/brands ──▶  main/brands/{brandName} ──▶  main/products/{productId}
│             ──▶  main/products ──▶  main/products/{productId}
│
├── main/search ──▶  main/products/{productId}
│
├── main/wishlist [auth] ──▶  main/products/{productId}
│
├── main/cart [auth] ──▶  checkout/summary ──▶  checkout/payment
│                                            └── ──▶  checkout/confirmation/{orderId}
│
└── main/profile [auth]
      ├── main/profile/orders ──▶  main/profile/orders/{orderId}
      ├── main/profile/addresses ──▶  main/profile/addresses/form
      └── main/profile/settings
```

### Bottom Nav Tabs

| # | Label | Icon | Route | Auth |
|---|---|---|---|---|
| 0 | Home | `Icons.Default.Home` | `main/home` | No |
| 1 | Search | `Icons.Default.Search` | `main/search` | No |
| 2 | Wishlist | `Icons.Default.FavoriteBorder` | `main/wishlist` | Yes |
| 3 | Settings | `Icons.Default.Settings` | `main/settings` | No |

Profile and Cart are top app bar actions, not bottom tabs. Guests still see Wishlist, Profile, and Cart icons; tapping any auth-gated destination redirects to Login. Settings remains accessible to guests. Firebase sync from Settings requires authentication; guest preference changes are saved locally only.

### Auth Guard

Auth-gated routes check `IsUserLoggedInUseCase` at navigation time. Unauthenticated access redirects to `auth/login` with a `returnRoute` argument.

---

## 11. Confirmation Dialog Pattern

All destructive or high-impact actions **must** show `ConfirmationDialog` (from `:presentation/ui/components/`) before executing.

### Affected Actions

| Trigger | Message |
|---|---|
| Remove cart item | "Remove [item name] from your cart?" |
| Clear entire cart | "Remove all items from your cart?" |
| Remove wishlist item | "Remove this item from your wishlist?" |
| Delete address | "Delete this address permanently?" |
| Logout | "Are you sure you want to log out?" |
| Place order | "Confirm and place your order?" |

### MVI Wiring

```
User taps destructive action
  → dispatch Request* intent
  → ViewModel sets showXxxDialog = true (+ pendingId in state)
  → Composable renders ConfirmationDialog

  [Confirm] → dispatch Confirm* intent → ViewModel executes UseCase → clears flags
  [Cancel]  → dispatch DismissConfirmDialog → ViewModel clears flags
```

---

## 12. Coding Standards

### Naming Conventions

| Element | Pattern | Example |
|---|---|---|
| Class | PascalCase | `ProductDetailViewModel` |
| Interface | PascalCase | `CatalogRepository` |
| Function / variable | camelCase | `processIntent()`, `cartItems` |
| Constant | SCREAMING_SNAKE_CASE | `MAX_COD_AMOUNT` |
| Package | lowercase | `com.shopzen.data.catalog.remote.api` |
| Route string | `noun/noun/{param}` | `"main/products/{productId}"` |
| Intent | Verb + Noun | `LoadCart`, `RequestRemoveItem` |
| State | Noun + `State` | `CartState` |
| UseCase | Verb + Noun + `UseCase` | `AddToCartUseCase` |
| Application class | `ShopzenApp` | — |
| Database class | `ShopzenDatabase` | — |

### Mandatory Rules

- **No business logic in Composables** — logic lives in ViewModel → UseCase only
- **No direct repository calls from ViewModels** — always via a UseCase
- **No God ViewModels** — split by screen if a ViewModel exceeds ~150 lines
- **No `LiveData`** — use `StateFlow` + `collectAsStateWithLifecycle()`
- **No `GlobalScope`** — all coroutines launched in `viewModelScope`
- **No `!!` operator** outside Hilt-provided non-null objects; prefer `?: return` or `?.let`
- All constants and magic numbers → `Constants.kt` (in `:data` or `:domain` as appropriate)
- All user-facing strings → `res/values/strings.xml`
- KDoc on all public functions in `:domain` and `:data`

### SOLID at a Glance

- **SRP** — Each UseCase, ViewModel, and DataSource has one job
- **OCP** — Add behaviour via new UseCases, not by modifying existing ones
- **LSP** — `RepositoryImpl` is always interchangeable with its interface
- **ISP** — Split repository interfaces if they grow beyond their feature boundary
- **DIP** — Inject interfaces everywhere; never instantiate concrete classes manually

---

## 13. Testing Strategy

### Coverage Targets

| Layer | Target | Tools |
|---|---|---|
| Domain (UseCases) | ≥ 90% | JUnit 5, MockK, coroutines-test |
| Data (Repositories) | ≥ 70% | JUnit 5, MockK |
| Presentation (ViewModels) | ≥ 80% | JUnit 5, MockK, Turbine |
| UI (Compose) | Critical flows only | Compose Test |

### Test Structure

- **UseCase tests** — mock the repository interface; test success and failure flows
- **Repository tests** — mock `RemoteDataSource` and `LocalDataSource`; verify mapping and delegation
- **ViewModel tests** — mock all UseCases; use Turbine to assert state transitions on `StateFlow`
- **Compose tests** — set content with a fake ViewModel; assert node visibility and interaction

### Key ViewModel Test Patterns

- Verify `isLoading = true` is emitted before the result
- Verify `Request*` intent sets `showXxxDialog = true` and stores pending ID
- Verify `DismissConfirmDialog` clears all dialog flags

---

## 14. Visual Identity

- App name: **Shopzen**
- Adaptive launcher icon required (foreground + background layers)
- Material 3 color scheme defined in `:presentation/ui/theme/Color.kt` — used consistently across all screens
- Light **and** dark theme both supported via `ShopzenTheme.kt`
- Typography uses the scale in `Typography.kt` — no arbitrary `fontSize` values outside the scale
- Shared components (`ProductCard`, `ConfirmationDialog`, `LoadingIndicator`, `ErrorScreen`, `EmptyStateView`) live in `:presentation/ui/components/` and must be used consistently — **do not re-implement per feature**

---

## 15. Secrets & BuildConfig

All credentials live in `local.properties` (never committed).

```properties
# local.properties
SHOPIFY_API_KEY=
SHOPIFY_PASSWORD=
SHOPIFY_HOSTNAME=yourstore.myshopify.com
SHOPIFY_API_VERSION=2024-01
GOOGLE_WEB_CLIENT_ID=
CURRENCY_API_KEY=
GOOGLE_PLACES_API_KEY=
MAPBOX_ACCESS_TOKEN=
PAYMOB_BASE_URL=https://accept.paymob.com
PAYMOB_PUBLIC_KEY=
PAYMOB_SECRET_KEY=
PAYMOB_CURRENCY=EGP
PAYMOB_ONLINE_CARD_INTEGRATION_ID=
```

Injected via `buildConfigField` in `app/build.gradle.kts`. Access at runtime via `BuildConfig.*`.

**`.gitignore` must include:** `local.properties`, `google-services.json`

---

## 16. Collaboration & Git

| Rule | Detail |
|---|---|
| Branch naming | `feature/{feature-name}` — e.g. `feature/auth`, `feature/cart` |
| Commit format | Conventional Commits — e.g. `feat(auth): add google sign-in`, `fix(cart): clamp quantity to max stock` |
| PR policy | Minimum 1 team member review and approval before merge to `main` |
| Individual contribution | Every team member must have verifiable commits and push history |
| Task tracking | Jira/Trello task titles match feature names from this document |
| Mentor access | Mentors added as GitHub collaborators and board members on Jira |

---

*Any architectural change must be reflected in this document before being implemented in code.*
