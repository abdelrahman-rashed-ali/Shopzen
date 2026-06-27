# AGENTS.md — `:presentation` Module (Shopzen)

> **Scope:** This document is the single source of truth for everything inside the `:presentation` Gradle module of the Shopzen project.
> No deviation from these guidelines without updating this document first.

Reference design: <https://pocket-shop-style.lovable.app>

---

## Table of Contents

1. [Module Role & Boundaries](#1-module-role--boundaries)
2. [Package Structure](#2-package-structure)
3. [MVI Pattern](#3-mvi-pattern)
4. [Common — Theme & Shared Components](#4-common--theme--shared-components)
5. [Confirmation Dialog Pattern](#5-confirmation-dialog-pattern)
6. [Feature-by-Feature Reference](#6-feature-by-feature-reference)
   - [auth](#61-auth)
   - [catalog](#62-catalog)
   - [search](#63-search)
   - [wishlist](#64-wishlist)
   - [cart](#65-cart)
   - [account](#66-account)
   - [checkout](#67-checkout)
7. [Navigation Contract](#7-navigation-contract)
8. [Visual Identity](#8-visual-identity)
9. [Coding Standards for `:presentation`](#9-coding-standards-for-presentation)
10. [Testing Strategy](#10-testing-strategy)

---

## 1. Module Role & Boundaries

The `:presentation` module owns all UI — Composables, ViewModels, MVI State, and Intent definitions. It depends on `:domain` only and knows nothing about `:data` or `:app`.

```
:presentation ──▶ :domain ◀── :data
                    ▲
              :app wires everything
```

### What `:presentation` IS

| Responsibility | Description |
|---|---|
| Composables | All screens and reusable UI components (stateless renderers) |
| ViewModels | `@HiltViewModel` classes that bridge intent → use case → state |
| MVI State | `data class` representing everything a screen needs to render |
| MVI Intent | `sealed class` representing every user or system event |
| Theme | `ShopzenTheme`, `Color.kt`, `Typography.kt` — global visual identity |
| Shared Components | `ProductCard`, `ConfirmationDialog`, `LoadingIndicator`, `ErrorScreen`, `EmptyStateView` |

### What `:presentation` MUST NOT contain

| Forbidden | Reason |
|---|---|
| Direct repository calls from ViewModels | All data access goes through UseCases from `:domain` |
| Business logic inside Composables | Composables are stateless renderers only |
| `import com.shopzen.data.*` | `:presentation` must not depend on `:data` |
| `import retrofit2.*` / `import androidx.room.*` | Infrastructure belongs in `:data` |
| Hardcoded strings in Composables | All user-facing strings go in `res/values/strings.xml` |
| Arbitrary `fontSize` values | Only Typography scale values from `Typography.kt` allowed |
| Re-implemented shared components per feature | Always use common components from `common/components/` |
| `LiveData` | Use `StateFlow` + `collectAsStateWithLifecycle()` only |
| `GlobalScope` | All coroutines launched inside `viewModelScope` only |

> **Enforcement:** `:presentation`'s Gradle dependencies must declare only `:domain`. Any build.gradle.kts that adds `:data` as a dependency is a violation.

---

## 2. Package Structure

```
com.shopzen.presentation/
│
├── common/
│   ├── theme/
│   │   ├── ShopzenTheme.kt          ← MaterialTheme wrapper; light + dark support
│   │   ├── Color.kt                 ← Material 3 color scheme definitions
│   │   └── Typography.kt            ← Type scale; all text styles defined here
│   └── components/                  ← Shared across ALL features
│       ├── ConfirmationDialog.kt
│       ├── LoadingIndicator.kt
│       ├── ErrorScreen.kt
│       ├── EmptyStateView.kt
│       └── ProductCard.kt
│
├── auth/
│   ├── screen/
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   └── EmailVerificationScreen.kt
│   ├── components/
│   │   └── (auth-scoped UI, e.g. GoogleSignInButton.kt)
│   ├── viewmodel/
│   │   └── AuthViewModel.kt
│   ├── state/
│   │   └── AuthState.kt
│   └── intent/
│       └── AuthIntent.kt
│
├── catalog/
│   ├── screen/
│   │   ├── HomeScreen.kt
│   │   ├── BrandListScreen.kt
│   │   ├── BrandProductsScreen.kt
│   │   ├── ProductListScreen.kt
│   │   └── ProductDetailScreen.kt
│   ├── components/
│   │   ├── FeaturedBanner.kt
│   │   ├── BrandGrid.kt
│   │   ├── CategoryChips.kt
│   │   ├── ProductImagePager.kt     ← HorizontalPager + dot indicators
│   │   ├── VariantChip.kt
│   │   └── SizeSelector.kt
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt
│   │   ├── ProductListViewModel.kt
│   │   └── ProductDetailViewModel.kt
│   ├── state/
│   │   ├── HomeState.kt
│   │   ├── ProductListState.kt
│   │   └── ProductDetailState.kt
│   └── intent/
│       ├── HomeIntent.kt
│       ├── ProductListIntent.kt
│       └── ProductDetailIntent.kt
│
├── search/
│   ├── screen/
│   │   └── SearchScreen.kt
│   ├── components/
│   │   ├── FilterChipRow.kt         ← Dismissable active filter chips
│   │   └── SortBottomSheet.kt
│   ├── viewmodel/
│   │   └── SearchViewModel.kt
│   ├── state/
│   │   └── SearchState.kt
│   └── intent/
│       └── SearchIntent.kt
│
├── wishlist/
│   ├── screen/
│   │   └── WishlistScreen.kt
│   ├── components/
│   │   └── WishlistItemCard.kt
│   ├── viewmodel/
│   │   └── WishlistViewModel.kt
│   ├── state/
│   │   └── WishlistState.kt
│   └── intent/
│       └── WishlistIntent.kt
│
├── cart/
│   ├── screen/
│   │   └── CartScreen.kt
│   ├── components/
│   │   ├── CartItemRow.kt
│   │   ├── QuantityStepper.kt
│   │   └── CartSummaryBar.kt
│   ├── viewmodel/
│   │   └── CartViewModel.kt
│   ├── state/
│   │   └── CartState.kt
│   └── intent/
│       └── CartIntent.kt
│
├── account/
│   ├── screen/
│   │   ├── ProfileScreen.kt
│   │   ├── OrderHistoryScreen.kt
│   │   ├── OrderDetailScreen.kt
│   │   ├── AddressListScreen.kt
│   │   ├── AddressFormScreen.kt
│   │   └── SettingsScreen.kt
│   ├── components/
│   │   ├── WishlistPreviewGrid.kt   ← Max 4 items; shown on ProfileScreen
│   │   ├── OrderHistoryCard.kt
│   │   ├── AddressCard.kt
│   │   └── CurrencySelector.kt
│   ├── viewmodel/
│   │   ├── ProfileViewModel.kt
│   │   ├── OrderHistoryViewModel.kt
│   │   ├── OrderDetailViewModel.kt
│   │   ├── AddressListViewModel.kt
│   │   ├── AddressFormViewModel.kt
│   │   └── SettingsViewModel.kt
│   ├── state/
│   │   ├── ProfileState.kt
│   │   ├── OrderHistoryState.kt
│   │   ├── OrderDetailState.kt
│   │   ├── AddressListState.kt
│   │   ├── AddressFormState.kt
│   │   └── SettingsState.kt
│   └── intent/
│       ├── ProfileIntent.kt
│       ├── OrderHistoryIntent.kt
│       ├── OrderDetailIntent.kt
│       ├── AddressListIntent.kt
│       ├── AddressFormIntent.kt
│       └── SettingsIntent.kt
│
└── checkout/
    ├── screen/
    │   ├── CheckoutScreen.kt
    │   ├── PaymentScreen.kt
    │   └── OrderConfirmationScreen.kt
    ├── components/
    │   ├── CouponInputField.kt      ← Shows inline error, not a dialog
    │   ├── PaymentMethodSelector.kt
    │   ├── OrderSummaryCard.kt
    │   └── ShippingAddressPicker.kt
    ├── viewmodel/
    │   ├── CheckoutViewModel.kt
    │   ├── PaymentViewModel.kt
    │   └── OrderConfirmationViewModel.kt
    ├── state/
    │   ├── CheckoutState.kt
    │   ├── PaymentState.kt
    │   └── OrderConfirmationState.kt
    └── intent/
        ├── CheckoutIntent.kt
        ├── PaymentIntent.kt
        └── OrderConfirmationIntent.kt
```

### Structural Rules

- Every feature has exactly five sub-packages: `screen/`, `components/`, `viewmodel/`, `state/`, `intent/`
- One file per screen, ViewModel, State, and Intent class
- Feature-scoped components in `{feature}/components/` must not be imported by other features — promote to `common/components/` if needed cross-feature
- No files placed at the `com.shopzen.presentation/` root level

---

## 3. MVI Pattern

Applied **uniformly** across every feature screen. No exceptions.

```
User Action
    │
    ▼
Intent (sealed class)           → dispatched via onIntent(intent)
    │
    ▼
ViewModel.processIntent()       → single entry point for all intents
    │  calls UseCase(s)
    │  updates _state via copy()
    ▼
StateFlow<FeatureState>         → exposed as state: StateFlow<FeatureState>
    │
    ▼
Composable (stateless renderer) → collects state, dispatches intents
```

### Intent Contract

```kotlin
// intent/CartIntent.kt
sealed class CartIntent {
    object LoadCart : CartIntent()
    data class RequestRemoveItem(val itemId: String, val itemName: String) : CartIntent()
    data class ConfirmRemoveItem(val itemId: String) : CartIntent()
    object RequestClearCart : CartIntent()
    object ConfirmClearCart : CartIntent()
    object DismissConfirmDialog : CartIntent()
    data class UpdateQuantity(val itemId: String, val quantity: Int) : CartIntent()
}
```

Rules:
- Every user or system event is a named branch in the `sealed class` — no generic catch-alls
- Destructive actions have a **pair** of intents: `Request*` (shows dialog) and `Confirm*` (executes action)
- `DismissConfirmDialog` clears all dialog flags in one intent; one instance per feature
- Intent names follow **Verb + Noun** pattern: `LoadCart`, `RequestRemoveItem`, `UpdateQuantity`

### State Contract

```kotlin
// state/CartState.kt
data class CartState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cart: Cart? = null,
    val total: Double = 0.0,
    // Confirmation dialog flags
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null,
    val pendingRemovalItemName: String? = null,
    val showClearCartDialog: Boolean = false
)
```

Rules:
- `data class` with **default values for every field** — never require callers to supply all fields
- `isLoading: Boolean = false` — required in every feature state
- `error: String? = null` — required in every feature state; null means no error
- `show*Dialog: Boolean = false` — one flag per destructive action in that feature
- `pending*Id` / `pending*Name` — stored in state alongside the dialog flag; cleared together when dialog is dismissed
- State is updated exclusively via `copy()` — never mutated directly

### ViewModel Contract

```kotlin
// viewmodel/CartViewModel.kt
@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val getCartTotalUseCase: GetCartTotalUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    fun processIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.LoadCart         -> loadCart()
            is CartIntent.RequestRemoveItem -> requestRemoveItem(intent.itemId, intent.itemName)
            is CartIntent.ConfirmRemoveItem -> confirmRemoveItem(intent.itemId)
            is CartIntent.RequestClearCart  -> _state.update { it.copy(showClearCartDialog = true) }
            is CartIntent.ConfirmClearCart  -> confirmClearCart()
            is CartIntent.DismissConfirmDialog -> dismissDialogs()
            is CartIntent.UpdateQuantity   -> updateQuantity(intent.itemId, intent.quantity)
        }
    }
}
```

Rules:
- Annotated `@HiltViewModel` with `@Inject constructor`
- Single `MutableStateFlow<FeatureState>` named `_state`; exposed as `val state: StateFlow<FeatureState>`
- Single public entry point: `fun processIntent(intent: FeatureIntent)`
- `processIntent` dispatches to private functions via `when` — no logic inline in `when` branches beyond trivial state copies
- All coroutines launched with `viewModelScope.launch { }` — never `GlobalScope`
- Never split beyond ~150 lines; if a screen is complex enough, split into multiple ViewModels (one per screen)
- Never call repository interfaces directly — only call UseCases from `:domain`

### Composable Contract

```kotlin
// screen/CartScreen.kt
@Composable
fun CartScreen(
    state: CartState,
    onIntent: (CartIntent) -> Unit
) {
    // Stateless renderer — read state, dispatch intents, zero logic
    LaunchedEffect(Unit) { onIntent(CartIntent.LoadCart) }

    if (state.isLoading) { LoadingIndicator() }
    else if (state.error != null) { ErrorScreen(message = state.error) }
    else if (state.cart?.items.isNullOrEmpty()) { EmptyStateView(...) }
    else { /* render cart items */ }

    if (state.showRemoveItemDialog) {
        ConfirmationDialog(
            message = stringResource(R.string.remove_item_from_cart, state.pendingRemovalItemName ?: ""),
            onConfirm = { onIntent(CartIntent.ConfirmRemoveItem(state.pendingRemovalItemId!!)) },
            onDismiss = { onIntent(CartIntent.DismissConfirmDialog) }
        )
    }
}
```

Rules:
- Composable functions are **stateless** — they receive `state` and `onIntent`, nothing else (no ViewModel reference passed down)
- No `if/else` business logic; only render decisions based on state fields
- `LaunchedEffect` is the only place a load intent is dispatched from a Composable
- The ViewModel is collected and `processIntent` is wired only at the screen's entry point in `AppNavGraph`
- All user-facing text via `stringResource(R.string.*)` — never hardcoded

---

## 4. Common — Theme & Shared Components

### Theme

**`ShopzenTheme.kt`** — wraps `MaterialTheme`. Applied once at the root in `MainActivity`. All feature screens inherit it automatically.

```kotlin
@Composable
fun ShopzenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShopzenTypography,
        content = content
    )
}
```

**`Color.kt`** — defines both `LightColorScheme` and `DarkColorScheme` using Material 3 `colorScheme { }`. No hex values scattered across feature code — reference tokens (`MaterialTheme.colorScheme.primary`, etc.) everywhere.

**`Typography.kt`** — defines `ShopzenTypography` using the Material 3 type scale. No `fontSize` values outside this file. Always use `MaterialTheme.typography.*` in Composables.

### Shared Components

These components live in `common/components/` and **must be used consistently** across all features. Never re-implement them per feature.

#### `ConfirmationDialog`

```kotlin
@Composable
fun ConfirmationDialog(
    message: String,
    confirmText: String = stringResource(R.string.confirm),
    dismissText: String = stringResource(R.string.cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

Used for all destructive actions. See Section 5 for complete wiring.

#### `LoadingIndicator`

```kotlin
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier)
```

Centered `CircularProgressIndicator`. Used when `state.isLoading == true`.

#### `ErrorScreen`

```kotlin
@Composable
fun ErrorScreen(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

Full-screen error with optional retry button. Used when `state.error != null`.

#### `EmptyStateView`

```kotlin
@Composable
fun EmptyStateView(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

Shown when a list is empty (empty cart, empty wishlist, no search results).

#### `ProductCard`

```kotlin
@Composable
fun ProductCard(
    product: Product,
    isInWishlist: Boolean,
    onCardClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier
)
```

Used across `HomeScreen`, `ProductListScreen`, `BrandProductsScreen`, `SearchScreen`, and `WishlistScreen`. The heart icon reflects `isInWishlist`. Tapping the heart dispatches the wishlist toggle intent at the parent screen level.

---

## 5. Confirmation Dialog Pattern

All destructive or high-impact actions **must** show `ConfirmationDialog` before the corresponding UseCase is called. This is enforced at the ViewModel and Composable level.

### Affected Actions

| Feature | Trigger | Dialog Message | Request Intent | Confirm Intent |
|---|---|---|---|---|
| `cart` | Remove single item | `"Remove [item name] from your cart?"` | `RequestRemoveItem(itemId, itemName)` | `ConfirmRemoveItem(itemId)` |
| `cart` | Clear entire cart | `"Remove all items from your cart?"` | `RequestClearCart` | `ConfirmClearCart` |
| `wishlist` | Remove wishlist item | `"Remove this item from your wishlist?"` | `RequestRemoveItem(itemId)` | `ConfirmRemoveItem(itemId)` |
| `account` | Delete address | `"Delete this address permanently?"` | `RequestDeleteAddress(addressId)` | `ConfirmDeleteAddress(addressId)` |
| `auth` | Logout | `"Are you sure you want to log out?"` | `RequestLogout` | `ConfirmLogout` |
| `checkout` | Place order | `"Confirm and place your order?"` | `RequestPlaceOrder` | `ConfirmPlaceOrder` |

### Full MVI Wiring

```
1. User taps destructive action button
       │
       ▼
2. Composable dispatches Request* intent
   e.g. onIntent(CartIntent.RequestRemoveItem(item.id, item.title))
       │
       ▼
3. ViewModel.processIntent() handles Request*:
   _state.update { it.copy(showRemoveItemDialog = true, pendingRemovalItemId = itemId, pendingRemovalItemName = itemName) }
       │
       ▼
4. Composable observes state.showRemoveItemDialog == true
   → renders ConfirmationDialog(message = "Remove $itemName from your cart?", ...)
       │
       ├── [User taps Confirm]
       │       ▼
       │   onIntent(CartIntent.ConfirmRemoveItem(state.pendingRemovalItemId!!))
       │       ▼
       │   ViewModel calls removeFromCartUseCase(itemId)
       │   On success: _state.update { it.copy(showRemoveItemDialog = false, pendingRemovalItemId = null, pendingRemovalItemName = null) }
       │
       └── [User taps Cancel / dismisses]
               ▼
           onIntent(CartIntent.DismissConfirmDialog)
               ▼
           ViewModel: _state.update { it.copy(showRemoveItemDialog = false, pendingRemovalItemId = null, pendingRemovalItemName = null) }
```

### Rules

- `DismissConfirmDialog` intent clears **all** dialog-related state fields in one `copy()` call
- The ViewModel clears dialog flags in **both** confirm and dismiss paths
- Dialog message strings come from `res/values/strings.xml` with string formatting (e.g. `%s` for item name)
- `ConfirmationDialog` is always rendered at the feature screen's root level, not inside nested components

---

## 6. Feature-by-Feature Reference

### 6.1 `auth`

**Screens:** `SplashScreen`, `LoginScreen`, `RegisterScreen`, `EmailVerificationScreen`

#### `AuthState`

```kotlin
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val showLogoutDialog: Boolean = false
)
```

#### `AuthIntent`

```kotlin
sealed class AuthIntent {
    object CheckSession : AuthIntent()
    data class LoginWithEmail(val email: String, val password: String) : AuthIntent()
    data class LoginWithGoogle(val idToken: String) : AuthIntent()
    data class Register(val email: String, val password: String) : AuthIntent()
    object ResendVerificationEmail : AuthIntent()
    object RequestLogout : AuthIntent()
    object ConfirmLogout : AuthIntent()
    object DismissConfirmDialog : AuthIntent()
}
```

#### `AuthViewModel` — Use Cases Injected

| Use Case | Purpose |
|---|---|
| `IsUserLoggedInUseCase` | Called on `CheckSession` to route from `SplashScreen` |
| `LoginWithEmailUseCase` | Called on `LoginWithEmail` |
| `LoginWithGoogleUseCase` | Called on `LoginWithGoogle` |
| `RegisterWithEmailUseCase` | Called on `Register` |
| `SendVerificationEmailUseCase` | Called on `ResendVerificationEmail` |
| `LogoutUseCase` | Called on `ConfirmLogout` |

#### Screen Behaviours

**`SplashScreen`**
- On launch, dispatches `AuthIntent.CheckSession`
- `ViewModel` calls `IsUserLoggedInUseCase`; on result navigates to `main/home` (authenticated) or `auth/login` (no session)
- No UI elements other than the Shopzen logo/animation

**`LoginScreen`**
- Email + password fields; Login button; "Sign in with Google" button; link to `RegisterScreen`
- Google Sign-In launches from the **Activity** level; the resulting `idToken` is passed back as `LoginWithGoogle(idToken)`
- On success: navigate to `main/home` (or `returnRoute` if present)
- On error: `state.error` is shown as a snackbar or inline error text

**`RegisterScreen`**
- Email + password + confirm-password fields
- On success: `ViewModel` triggers automatic verification email (via `RegisterWithEmailUseCase`) → navigate to `auth/verify-email`

**`EmailVerificationScreen`**
- Informs user to check their inbox
- "Resend email" button dispatches `ResendVerificationEmail`
- "I've verified" button checks session and navigates to `main/home`

---

### 6.2 `catalog`

**Screens:** `HomeScreen`, `BrandListScreen`, `BrandProductsScreen`, `ProductListScreen`, `ProductDetailScreen`

#### `HomeState`

```kotlin
data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val brands: List<Brand> = emptyList(),
    val categories: List<Category> = emptyList(),
    val newArrivals: List<Product> = emptyList()
)
```

#### `HomeIntent`

```kotlin
sealed class HomeIntent {
    object LoadHome : HomeIntent()
    data class SelectBrand(val brandName: String) : HomeIntent()
    data class SelectCategory(val category: String) : HomeIntent()
    data class OpenProduct(val productId: String) : HomeIntent()
}
```

#### `ProductDetailState`

```kotlin
data class ProductDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val product: Product? = null,
    val selectedVariant: ProductVariant? = null,
    val selectedSize: String? = null,
    val isInWishlist: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val addToCartSuccess: Boolean = false
)
```

#### `ProductDetailIntent`

```kotlin
sealed class ProductDetailIntent {
    data class LoadProduct(val productId: String) : ProductDetailIntent()
    data class SelectVariant(val variant: ProductVariant) : ProductDetailIntent()
    data class SelectSize(val size: String) : ProductDetailIntent()
    object AddToCart : ProductDetailIntent()
    object ToggleWishlist : ProductDetailIntent()
}
```

#### `ProductListState` / `ProductListIntent`

```kotlin
data class ProductListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val title: String = ""            // "All Products", brand name, or category name
)

sealed class ProductListIntent {
    data class LoadProducts(val brand: String? = null, val category: String? = null) : ProductListIntent()
    data class OpenProduct(val productId: String) : ProductListIntent()
}
```

#### ViewModels and Use Cases

| ViewModel | Use Cases Injected |
|---|---|
| `HomeViewModel` | `GetBrandsUseCase`, `GetCategoriesUseCase`, `GetProductsUseCase` |
| `ProductListViewModel` | `GetProductsUseCase`, `GetProductsByBrandUseCase`, `GetProductsByCategoryUseCase` |
| `ProductDetailViewModel` | `GetProductByIdUseCase`, `GetProductReviewsUseCase`, `IsProductInWishlistUseCase`, `AddToWishlistUseCase`, `RemoveFromWishlistUseCase`, `AddToCartUseCase`, `GetCurrentUserUseCase` |

#### Screen Behaviours

**`HomeScreen`**
- Sections: featured banner (first product image), brand horizontal grid, category chips, new arrivals grid
- Tapping a brand navigates to `main/brands/{brandName}`
- Tapping a category navigates to `main/products` filtered by category
- Tapping a product navigates to `main/products/{productId}`

**`ProductDetailScreen`**
- Product images in `HorizontalPager` with dot page indicators
- If `product.options` contains an option with `name == "Size"`, render `SizeSelector` — **Add to Cart is disabled until a size is selected**
- Out-of-stock variants (`inventoryQuantity == 0`) render as disabled `VariantChip`s
- Heart icon (wishlist toggle) reflects `state.isInWishlist` from `IsProductInWishlistUseCase`
- Unauthenticated "Add to Cart" or wishlist tap → navigate to `auth/login` with `returnRoute = "main/products/{productId}"`

---

### 6.3 `search`

**Screens:** `SearchScreen`

#### `SearchState`

```kotlin
data class SearchState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val results: List<Product> = emptyList(),
    val activeFilter: SearchFilter = SearchFilter(),
    val activeSortOption: SortOption = SortOption.DEFAULT,
    val showFilterSheet: Boolean = false,
    val showSortSheet: Boolean = false
)
```

#### `SearchIntent`

```kotlin
sealed class SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent()
    data class ApplyFilter(val filter: SearchFilter) : SearchIntent()
    data class ApplySort(val sortOption: SortOption) : SearchIntent()
    data class RemoveFilterChip(val field: String) : SearchIntent()
    object ClearFilters : SearchIntent()
    object OpenFilterSheet : SearchIntent()
    object OpenSortSheet : SearchIntent()
    object DismissSheets : SearchIntent()
    data class OpenProduct(val productId: String) : SearchIntent()
}
```

#### `SearchViewModel` — Use Cases Injected

| Use Case | Purpose |
|---|---|
| `SearchProductsUseCase` | Remote text search; triggered after 300 ms debounce |
| `FilterProductsUseCase` | Client-side filtering applied to search results |
| `SortProductsUseCase` | Client-side sorting applied after filtering |

#### Screen Behaviours

- Search field debounced 300 ms in the ViewModel before calling `SearchProductsUseCase`
- Order of operations: `SearchProductsUseCase` → `FilterProductsUseCase` → `SortProductsUseCase`
- Active filters displayed as dismissable `FilterChip`s in a row above the result grid
- Tapping a chip dispatches `RemoveFilterChip(field)` which clears that filter field and re-applies
- Results shown in a `LazyVerticalGrid` using `ProductCard` from `common/components/`

---

### 6.4 `wishlist`

**Screens:** `WishlistScreen`

#### `WishlistState`

```kotlin
data class WishlistState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<WishlistItem> = emptyList(),
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null
)
```

#### `WishlistIntent`

```kotlin
sealed class WishlistIntent {
    object LoadWishlist : WishlistIntent()
    data class RequestRemoveItem(val itemId: String) : WishlistIntent()
    data class ConfirmRemoveItem(val itemId: String) : WishlistIntent()
    object DismissConfirmDialog : WishlistIntent()
    data class OpenProduct(val productId: String) : WishlistIntent()
}
```

#### `WishlistViewModel` — Use Cases Injected

| Use Case | Purpose |
|---|---|
| `GetWishlistUseCase` | Reactive stream of wishlist items for current user |
| `RemoveFromWishlistUseCase` | Removes item after confirmation |
| `GetCurrentUserUseCase` | Provides `userId` to scope the wishlist |

#### Screen Behaviours

- `WishlistScreen` is auth-gated — `AppNavGraph` checks `IsUserLoggedInUseCase` before navigating here
- Displayed as a `LazyVerticalGrid` of `WishlistItemCard` components
- Remove button on each card dispatches `RequestRemoveItem` → shows `ConfirmationDialog`
- Tapping a card navigates to `main/products/{productId}`
- If `state.items` is empty, shows `EmptyStateView`

---

### 6.5 `cart`

**Screens:** `CartScreen`

#### `CartState`

```kotlin
data class CartState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cart: Cart? = null,
    val total: Double = 0.0,
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null,
    val pendingRemovalItemName: String? = null,
    val showClearCartDialog: Boolean = false
)
```

#### `CartIntent`

```kotlin
sealed class CartIntent {
    object LoadCart : CartIntent()
    data class RequestRemoveItem(val itemId: String, val itemName: String) : CartIntent()
    data class ConfirmRemoveItem(val itemId: String) : CartIntent()
    object RequestClearCart : CartIntent()
    object ConfirmClearCart : CartIntent()
    object DismissConfirmDialog : CartIntent()
    data class UpdateQuantity(val itemId: String, val quantity: Int, val maxQuantity: Int) : CartIntent()
    object ProceedToCheckout : CartIntent()
}
```

#### `CartViewModel` — Use Cases Injected

| Use Case | Purpose |
|---|---|
| `GetCartUseCase` | Reactive cart stream for current user |
| `AddToCartUseCase` | Used when coming from `ProductDetailScreen` via shared ViewModel or nav argument |
| `RemoveFromCartUseCase` | Removes single item after confirmation |
| `UpdateCartItemQuantityUseCase` | Stepper +/- with `maxQuantity` clamping |
| `GetCartTotalUseCase` | Pure total recalculated on each cart emission |
| `ClearCartUseCase` | Removes all items after confirmation |
| `GetCurrentUserUseCase` | Provides `userId` |

#### Screen Behaviours

- `CartScreen` is auth-gated
- Each `CartItemRow` shows: image, title, variant, `QuantityStepper`, remove button, price
- `QuantityStepper` increment button is **disabled** when `item.quantity == item.maxQuantity`
- Cart total displayed in `CartSummaryBar` at the bottom, recalculated locally on every change
- Cart item count badge on bottom navigation tab updates reactively from `GetCartUseCase`
- "Proceed to Checkout" dispatches `ProceedToCheckout` → ViewModel navigates to `checkout/summary`
- Two confirmation dialogs can appear: remove single item, or clear entire cart

---

### 6.6 `account`

**Screens:** `ProfileScreen`, `OrderHistoryScreen`, `OrderDetailScreen`, `AddressListScreen`, `AddressFormScreen`, `SettingsScreen`

#### `ProfileState`

```kotlin
data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null,
    val recentOrderCount: Int = 0,
    val wishlistPreview: List<WishlistItem> = emptyList(), // max 4
    val showLogoutDialog: Boolean = false
)
```

#### `ProfileIntent`

```kotlin
sealed class ProfileIntent {
    object LoadProfile : ProfileIntent()
    object RequestLogout : ProfileIntent()
    object ConfirmLogout : ProfileIntent()
    object DismissConfirmDialog : ProfileIntent()
    object OpenOrderHistory : ProfileIntent()
    object OpenAddresses : ProfileIntent()
    object OpenSettings : ProfileIntent()
}
```

#### `AddressFormState`

```kotlin
data class AddressFormState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val address1: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "",
    val zip: String = "",
    val countries: List<Country> = emptyList(),
    val isEditMode: Boolean = false,
    val saveSuccess: Boolean = false,
    // Field-level validation errors
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val phoneError: String? = null
)
```

#### `AddressListState`

```kotlin
data class AddressListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val addresses: List<Address> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val pendingDeleteAddressId: String? = null
)
```

#### `AddressListIntent`

```kotlin
sealed class AddressListIntent {
    object LoadAddresses : AddressListIntent()
    object OpenAddForm : AddressListIntent()
    data class OpenEditForm(val addressId: String) : AddressListIntent()
    data class RequestDeleteAddress(val addressId: String) : AddressListIntent()
    data class ConfirmDeleteAddress(val addressId: String) : AddressListIntent()
    object DismissConfirmDialog : AddressListIntent()
}
```

#### ViewModels and Use Cases

| ViewModel | Use Cases Injected |
|---|---|
| `ProfileViewModel` | `GetUserProfileUseCase`, `GetOrderHistoryUseCase`, `GetWishlistUseCase`, `LogoutUseCase`, `GetCurrentUserUseCase` |
| `OrderHistoryViewModel` | `GetOrderHistoryUseCase`, `GetCurrentUserUseCase` |
| `OrderDetailViewModel` | `GetOrderDetailUseCase` |
| `AddressListViewModel` | `GetAddressesUseCase`, `DeleteAddressUseCase`, `GetCurrentUserUseCase` |
| `AddressFormViewModel` | `GetCountriesUseCase`, `AddAddressUseCase`, `UpdateAddressUseCase`, `ValidateAddressUseCase`, `GetCurrentUserUseCase` |
| `SettingsViewModel` | `GetCurrencyRatesUseCase`, `GetCurrentUserUseCase` |

#### Screen Behaviours

**`ProfileScreen`**
- Personalized greeting using `userProfile.firstName`
- Shows recent order count and `WishlistPreviewGrid` (max 4 items) as a horizontal preview strip
- Logout button dispatches `RequestLogout` → `ConfirmationDialog` → `ConfirmLogout`

**`AddressFormScreen`**
- Used for both add (no `id` param) and edit (`?id={addressId}` param)
- Country dropdown populated from `GetCountriesUseCase` — never a static list
- Province dropdown updates reactively when country changes
- Required fields: `firstName`, `lastName`, `phone` — show field-level errors in `AddressFormState` when blank on submit
- On save, `AddAddressUseCase` / `UpdateAddressUseCase` internally calls `ValidateAddressUseCase` for GPS/Places enrichment

**`SettingsScreen`**
- Currency selector using `CurrencySelector` component
- Selected currency persisted in `DataStore` (managed in `:data`); applied globally to all price displays
- Changes dispatch a settings intent that saves via `SettingsViewModel`

---

### 6.7 `checkout`

**Screens:** `CheckoutScreen`, `PaymentScreen`, `OrderConfirmationScreen`

#### `CheckoutState`

```kotlin
data class CheckoutState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkout: Checkout? = null,
    val couponInput: String = "",
    val couponError: String? = null,   // Inline field error — not a dialog
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val showPlaceOrderDialog: Boolean = false
)
```

#### `CheckoutIntent`

```kotlin
sealed class CheckoutIntent {
    object LoadCheckout : CheckoutIntent()
    data class UpdateCouponInput(val code: String) : CheckoutIntent()
    object ApplyCoupon : CheckoutIntent()
    object RemoveCoupon : CheckoutIntent()
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutIntent()
    data class SelectShippingAddress(val address: Address) : CheckoutIntent()
    object RequestPlaceOrder : CheckoutIntent()
    object ConfirmPlaceOrder : CheckoutIntent()
    object DismissConfirmDialog : CheckoutIntent()
}
```

#### `PaymentState` / `PaymentIntent`

```kotlin
data class PaymentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMethod: PaymentMethod? = null,
    val availableMethods: List<PaymentMethod> = emptyList()
)

sealed class PaymentIntent {
    object LoadPaymentMethods : PaymentIntent()
    data class SelectMethod(val method: PaymentMethod) : PaymentIntent()
    object ConfirmPayment : PaymentIntent()
}
```

#### `OrderConfirmationState`

```kotlin
data class OrderConfirmationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderConfirmation: OrderConfirmation? = null
)
```

#### ViewModels and Use Cases

| ViewModel | Use Cases Injected |
|---|---|
| `CheckoutViewModel` | `GetCartUseCase`, `ValidateCouponUseCase`, `ApplyCouponUseCase`, `RemoveCouponUseCase`, `GetAddressesUseCase`, `GetAvailablePaymentMethodsUseCase`, `PlaceOrderUseCase`, `ClearCartUseCase`, `GetCurrentUserUseCase` |
| `PaymentViewModel` | `GetAvailablePaymentMethodsUseCase`, `ValidateCashLimitUseCase` |
| `OrderConfirmationViewModel` | `GetOrderDetailUseCase` |

#### Screen Behaviours

**`CheckoutScreen`**
- Loaded from cart items; `GetCartUseCase` provides line items
- `CouponInputField` shows `state.couponError` as **inline field error** below the input — never as a `ConfirmationDialog`
- `GetAvailablePaymentMethodsUseCase` determines visible payment methods; COD only shown when `totalPrice <= Constants.MAX_COD_AMOUNT`
- "Place Order" button dispatches `RequestPlaceOrder` → `ConfirmationDialog("Confirm and place your order?")` → `ConfirmPlaceOrder`

**Post-order flow (in `CheckoutViewModel.ConfirmPlaceOrder`):**

```
PlaceOrderUseCase(checkout)
    │
    ├── [Success] → ClearCartUseCase(userId)
    │               → navigate to checkout/confirmation/{orderId}
    │
    └── [Failure] → _state.update { it.copy(error = "...", showPlaceOrderDialog = false) }
```

**`OrderConfirmationScreen`**
- Receives `orderId` from nav argument
- Shows order number, total, and success illustration
- "Continue Shopping" navigates back to `main/home` clearing the checkout back stack

---

## 7. Navigation Contract

Navigation is owned by `:app`'s `AppNavGraph.kt`. The `:presentation` module's ViewModels **do not navigate directly** — they expose navigation events via a `SharedFlow` or a dedicated `navigationEvent: Flow<NavEvent>` in state, which `AppNavGraph` observes.

### Recommended Navigation Event Pattern

```kotlin
// In ViewModel
private val _navigationEvent = MutableSharedFlow<AuthNavEvent>()
val navigationEvent: SharedFlow<AuthNavEvent> = _navigationEvent.asSharedFlow()

sealed class AuthNavEvent {
    object NavigateToHome : AuthNavEvent()
    object NavigateToVerifyEmail : AuthNavEvent()
}
```

```kotlin
// In AppNavGraph (in :app)
LaunchedEffect(viewModel) {
    viewModel.navigationEvent.collect { event ->
        when (event) {
            AuthNavEvent.NavigateToHome -> navController.navigate(Routes.HOME) { ... }
            AuthNavEvent.NavigateToVerifyEmail -> navController.navigate(Routes.VERIFY_EMAIL)
        }
    }
}
```

### Route Reference (consumed by `:presentation` ViewModels via NavEvents)

| Screen | Route | Auth Gated |
|---|---|---|
| `SplashScreen` | `auth/splash` | No |
| `LoginScreen` | `auth/login` | No |
| `RegisterScreen` | `auth/register` | No |
| `EmailVerificationScreen` | `auth/verify-email` | No |
| `HomeScreen` | `main/home` | No |
| `BrandListScreen` | `main/brands` | No |
| `BrandProductsScreen` | `main/brands/{brandName}` | No |
| `ProductListScreen` | `main/products` | No |
| `ProductDetailScreen` | `main/products/{productId}` | No |
| `SearchScreen` | `main/search` | No |
| `WishlistScreen` | `main/wishlist` | **Yes** |
| `CartScreen` | `main/cart` | **Yes** |
| `ProfileScreen` | `main/profile` | **Yes** |
| `OrderHistoryScreen` | `main/profile/orders` | **Yes** |
| `OrderDetailScreen` | `main/profile/orders/{orderId}` | **Yes** |
| `AddressListScreen` | `main/profile/addresses` | **Yes** |
| `AddressFormScreen` | `main/profile/addresses/form?id={id}` | **Yes** |
| `SettingsScreen` | `main/profile/settings` | **Yes** |
| `CheckoutScreen` | `checkout/summary` | **Yes** |
| `PaymentScreen` | `checkout/payment` | **Yes** |
| `OrderConfirmationScreen` | `checkout/confirmation/{orderId}` | **Yes** |

### Auth Guard Rule

Auth-gated destinations must be checked before navigation. The check calls `IsUserLoggedInUseCase` in the navigation layer (`:app`). Unauthenticated access redirects to `auth/login` with a `returnRoute` argument so the user returns to the intended screen after login.

### Bottom Navigation Tabs

| # | Label | Icon | Route | Auth |
|---|---|---|---|---|
| 0 | Home | `Icons.Default.Home` | `main/home` | No |
| 1 | Search | `Icons.Default.Search` | `main/search` | No |
| 2 | Wishlist | `Icons.Default.FavoriteBorder` | `main/wishlist` | Yes |
| 3 | Cart | `Icons.Default.ShoppingCart` | `main/cart` | Yes |
| 4 | Profile | `Icons.Default.Person` | `main/profile` | Yes |

The cart tab badge count is driven reactively by `GetCartUseCase` collected in the bottom navigation's own ViewModel or in `MainActivity`.

---

## 8. Visual Identity

| Rule | Detail |
|---|---|
| App name | **Shopzen** |
| UI framework | Jetpack Compose only — **no XML layouts** |
| Design system | Material 3 |
| Color | Defined in `Color.kt`; accessed via `MaterialTheme.colorScheme.*` — never hardcode hex values in screens |
| Typography | Defined in `Typography.kt`; accessed via `MaterialTheme.typography.*` — never use arbitrary `fontSize` |
| Theme | Both light and dark supported via `ShopzenTheme.kt`; no screen should hardcode a specific theme |
| Launcher icon | Adaptive icon with foreground + background layers |
| Images | Loaded with **Coil** (`AsyncImage`) — never `BitmapFactory` or manual Glide |
| Product images | `AsyncImage` with a `placeholder` and `error` fallback in all `ProductCard` and detail views |
| Shared components | `ProductCard`, `ConfirmationDialog`, `LoadingIndicator`, `ErrorScreen`, `EmptyStateView` — always sourced from `common/components/`; **never re-implemented per feature** |

---

## 9. Coding Standards for `:presentation`

### Naming Conventions

| Element | Pattern | Example |
|---|---|---|
| Screen Composable | PascalCase + `Screen` | `CartScreen`, `ProductDetailScreen` |
| Feature component | PascalCase | `CartItemRow`, `QuantityStepper` |
| Shared component | PascalCase | `ConfirmationDialog`, `ProductCard` |
| ViewModel | PascalCase + `ViewModel` | `CartViewModel`, `AuthViewModel` |
| State class | PascalCase + `State` | `CartState`, `AuthState` |
| Intent class | PascalCase + `Intent` | `CartIntent`, `AuthIntent` |
| Intent branch | Verb + Noun | `LoadCart`, `RequestRemoveItem`, `ConfirmPlaceOrder` |
| State field — loading | `isLoading: Boolean` | always this name |
| State field — error | `error: String?` | always this name; null = no error |
| State field — dialog flag | `show*Dialog: Boolean` | `showRemoveItemDialog` |
| State field — pending id | `pending*Id: String?` | `pendingRemovalItemId` |
| Internal state flow | `_state` (private) | `private val _state = MutableStateFlow(...)` |
| Exposed state flow | `state` (public) | `val state: StateFlow<FeatureState>` |

### Mandatory Rules

- **No business logic in Composables** — if statements in Composables are render decisions only (e.g. `if (state.isLoading)`)
- **No direct UseCase calls from Composables** — only through ViewModel via `onIntent`
- **No direct repository calls from ViewModels** — always via UseCases from `:domain`
- **No God ViewModels** — split by screen if a ViewModel approaches or exceeds ~150 lines
- **No `LiveData`** — `StateFlow` collected with `collectAsStateWithLifecycle()` only
- **No `GlobalScope`** — `viewModelScope.launch { }` only
- **No `!!` operator** — use `?: return`, `?.let { }`, or named defaults
- **No hardcoded strings in Composables** — `stringResource(R.string.*)` always
- **No arbitrary font sizes** — `MaterialTheme.typography.*` only
- **No re-implementing shared components** — use `common/components/` consistently
- `DismissConfirmDialog` always clears **all** pending dialog state in a single `copy()` call

### Composable Function Signature Pattern

```kotlin
@Composable
fun FeatureScreen(
    state: FeatureState,
    onIntent: (FeatureIntent) -> Unit,
    modifier: Modifier = Modifier
)
```

Composables never accept a ViewModel reference — only `state` and `onIntent`. The ViewModel is wired in `AppNavGraph`.

### State Update Pattern

```kotlin
// Always use copy() — never direct mutation
_state.update { it.copy(isLoading = true, error = null) }

// After async operation
viewModelScope.launch {
    _state.update { it.copy(isLoading = true) }
    val result = someUseCase(param)
    result.fold(
        onSuccess = { data -> _state.update { it.copy(isLoading = false, data = data) } },
        onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
    )
}
```

---

## 10. Testing Strategy

All `:presentation` tests live in the `test/` source set of the `:presentation` module. UI tests using Compose Test live in `androidTest/`.

### Coverage Targets

| Layer | Target | Tools |
|---|---|---|
| ViewModels | ≥ 80% | JUnit 5, MockK, Turbine, `kotlinx-coroutines-test` |
| Composables (UI) | Critical flows only | Compose Test (`createComposeRule`) |

### ViewModel Test Structure

One test class per ViewModel: `{ViewModelName}Test.kt` in `test/com/shopzen/presentation/{feature}/viewmodel/`.

```kotlin
@ExtendWith(CoroutineTestExtension::class)
class CartViewModelTest {

    private val getCartUseCase: GetCartUseCase = mockk()
    private val removeFromCartUseCase: RemoveFromCartUseCase = mockk()
    private val clearCartUseCase: ClearCartUseCase = mockk()
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase = mockk()
    private val getCartTotalUseCase: GetCartTotalUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()

    private lateinit var viewModel: CartViewModel

    @BeforeEach
    fun setUp() {
        viewModel = CartViewModel(
            getCartUseCase, removeFromCartUseCase, clearCartUseCase,
            updateCartItemQuantityUseCase, getCartTotalUseCase, getCurrentUserUseCase
        )
    }

    @Test
    fun `LoadCart - emits isLoading true then cart data on success`() = runTest {
        // ...
    }

    @Test
    fun `RequestRemoveItem - sets showRemoveItemDialog true and stores pending id`() = runTest {
        // ...
    }

    @Test
    fun `DismissConfirmDialog - clears all dialog state fields`() = runTest {
        // ...
    }

    @Test
    fun `ConfirmRemoveItem - calls removeFromCartUseCase and clears dialog state`() = runTest {
        // ...
    }
}
```

### Mandatory ViewModel Test Cases

Every ViewModel must have tests covering:

1. **Load intent** — `isLoading = true` emitted before data, `isLoading = false` after
2. **Success path** — state contains the expected data after a successful UseCase call
3. **Error path** — `state.error` is non-null and `isLoading = false` after UseCase failure
4. **Request\* intent** — sets `show*Dialog = true` and stores `pending*Id` in state
5. **Confirm\* intent** — calls the relevant UseCase and clears dialog state
6. **DismissConfirmDialog** — clears all `show*Dialog` and `pending*` fields in one emission

### Flow Assertion Pattern (Turbine)

```kotlin
@Test
fun `LoadCart - emits loading then cart`() = runTest {
    val fakeCart = Cart(items = emptyList(), currency = "USD", subtotalPrice = 0.0, userId = "u1")
    every { getCurrentUserUseCase() } returns User("u1", "a@b.com", "User", null, true)
    every { getCartUseCase("u1") } returns flowOf(Result.success(fakeCart))
    every { getCartTotalUseCase(any()) } returns 0.0

    viewModel.state.test {
        viewModel.processIntent(CartIntent.LoadCart)
        assertThat(awaitItem().isLoading).isTrue()
        val loaded = awaitItem()
        assertThat(loaded.isLoading).isFalse()
        assertThat(loaded.cart).isEqualTo(fakeCart)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Compose UI Test Structure

One test class per critical screen: `{ScreenName}Test.kt` in `androidTest/com/shopzen/presentation/{feature}/screen/`.

```kotlin
@RunWith(AndroidJUnit4::class)
class CartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when isLoading true - shows LoadingIndicator`() {
        composeTestRule.setContent {
            CartScreen(state = CartState(isLoading = true), onIntent = {})
        }
        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun `when showRemoveItemDialog true - shows ConfirmationDialog`() {
        composeTestRule.setContent {
            CartScreen(
                state = CartState(showRemoveItemDialog = true, pendingRemovalItemName = "Blue Hoodie"),
                onIntent = {}
            )
        }
        composeTestRule.onNodeWithText("Remove Blue Hoodie from your cart?").assertIsDisplayed()
    }
}
```

### Critical UI Flows to Test

| Screen | Flow |
|---|---|
| `SplashScreen` | Navigates to home when logged in; to login when not |
| `LoginScreen` | Shows error on failed login; navigates on success |
| `CartScreen` | Shows `ConfirmationDialog` on remove; stepper disable when max quantity |
| `WishlistScreen` | Shows `ConfirmationDialog` on remove |
| `CheckoutScreen` | Shows inline coupon error (not dialog); shows `ConfirmationDialog` on place order |
| `ProductDetailScreen` | Add to Cart disabled when no size selected; heart reflects wishlist state |

---

*Any change to a screen, ViewModel, State, or Intent contract must be reflected in this document before being implemented in code. Changes here may cascade to `:app`'s `AppNavGraph` and `Routes`.*
