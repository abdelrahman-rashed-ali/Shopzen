# Checkout and Cart Coupon Features - Iteration 2 Domain Plan

Iteration 2 owns pure Kotlin domain work only: models, repository contracts, use cases, and tests. It must not add Android framework imports, data DTOs, Room entities, Ktor types, Apollo types, or presentation state.

## Current State Snapshot

Already present:

- `Cart`, `CartItem`, `DiscountCode`, `DiscountType`, and `CouponValidationResult`.
- Cart use cases for get/add/remove/update/clear cart, apply coupon, remove coupon, cart total, and currency symbol.
- `Checkout`, `PaymentMethod`, `OrderConfirmation`, and `CheckoutRepository`.

Still needed:

- Checkout use cases.
- A clear coupon validation domain contract.
- Constants ownership for COD limit.
- Domain unit tests for coupon math and checkout payment rules.
- Optional cleanup where current domain contracts drift from the original plan.

## Goals

- Make cart coupon behavior explicit and testable in domain.
- Make checkout order placement callable from presentation through a use case.
- Enforce COD eligibility in pure domain logic.
- Keep all domain APIs free from Android, Ktor, Room, Firebase, Apollo, and localization resource IDs.

## Non-Goals

- No Compose UI.
- No ViewModel/MVI changes.
- No Ktor request changes.
- No Room schema changes.
- No navigation work.
- No localization resources in this iteration.

## Cart Domain Work

### 1. Cart Model Audit

Validate existing `Cart` fields:

- `items`
- `currency`
- `subtotalPrice`
- `discountAmount`
- `totalPrice`
- `appliedCoupon`
- `userId`

Rules:

- `subtotalPrice` is sum of `item.price * item.quantity`.
- `discountAmount` is always clamped to `0.0..subtotalPrice`.
- `totalPrice` is `subtotalPrice - discountAmount`.
- Empty cart has zero subtotal, zero discount, zero total.

Acceptance:

- No model imports outside `shopzen.domain.*` and Kotlin stdlib.
- No DTO/entity annotations.

### 2. Coupon Validation Contract

Current gap: `CouponValidationResult` exists but `CartRepository` has no explicit `validateCoupon` function. Decide and implement one of these paths:

- Preferred: add `suspend fun validateCoupon(code: String): Result<CouponValidationResult>` to `CartRepository`, then create `ValidateCouponUseCase`.
- Alternative: keep validation hidden inside `applyCoupon`, delete unused `CouponValidationResult`, and document that `ApplyCouponUseCase` is the single coupon entry point.

Recommended path: explicit `ValidateCouponUseCase`, because presentation needs inline validation error before applying or showing coupon state.

Acceptance:

- Invalid coupon is represented as data or `Result.failure`, but not both for the same branch.
- No localized string resources in domain. Domain returns stable reason strings or typed failure.

### 3. Coupon Math Use Cases

Audit and harden:

- `ApplyCouponUseCase`
- `RemoveCouponUseCase`
- `GetCartTotalUseCase`

Rules:

- Percentage discount: `subtotal * value / 100`.
- Fixed discount: `value`.
- Discount cannot make total negative.
- Removing coupon restores `discountAmount = 0.0`, `appliedCoupon = null`, `totalPrice = subtotalPrice`.

Acceptance:

- `GetCartTotalUseCase` clamps result at minimum `0.0`.
- Unit tests cover percent, fixed, over-discount, zero subtotal, and no coupon.

## Checkout Domain Work

### 1. Constants Ownership

Create or verify domain-level constant source for COD:

- `Constants.MAX_COD_AMOUNT`

Location:

- Prefer `domain/src/main/kotlin/shopzen/domain/common/Constants.kt`.

Rules:

- Domain use cases must not import constants from `data`.
- The value should be a single source used by checkout domain logic.

### 2. GetAvailablePaymentMethodsUseCase

Create:

- `domain/checkout/usecase/GetAvailablePaymentMethodsUseCase.kt`

Behavior:

- Returns `ONLINE_PAYMENT` always.
- Returns `CASH_ON_DELIVERY` only when `totalPrice <= Constants.MAX_COD_AMOUNT`.
- Output order: `CASH_ON_DELIVERY`, `ONLINE_PAYMENT` when both allowed.

Acceptance:

- Pure function, no repository dependency.
- Tests for below limit, equal limit, above limit.

### 3. ValidateCashLimitUseCase

Create:

- `domain/checkout/usecase/ValidateCashLimitUseCase.kt`

Behavior:

- Returns `true` when `totalPrice <= Constants.MAX_COD_AMOUNT`.
- Returns `false` when `totalPrice > Constants.MAX_COD_AMOUNT`.

Acceptance:

- Pure function.
- Tests for below/equal/above limit.

### 4. PlaceOrderUseCase

Create:

- `domain/checkout/usecase/PlaceOrderUseCase.kt`

Behavior:

- Calls `CheckoutRepository.placeOrder(checkout)`.
- Returns `Result<OrderConfirmation>`.

Rules:

- No coroutine scope stored.
- No `runBlocking`.
- No broad catch unless `CancellationException` is rethrown.
- Repository remains injected through interface.

Acceptance:

- Test success delegates to repository.
- Test failure propagates repository failure.

### 5. Checkout Model Audit

Existing `Checkout` should remain pure:

- `lineItems`
- `shippingAddress`
- `subtotalPrice`
- `discountAmount`
- `totalPrice`
- `currency`
- `appliedCoupon`
- `selectedPaymentMethod`

Rules:

- `selectedPaymentMethod` may be null until payment step.
- `lineItems` cannot be empty at order submission; validation can live in `PlaceOrderUseCase` if desired.
- `shippingAddress` must be provided before order submission.

Acceptance:

- No data-layer or presentation-layer imports.

## UseCaseModule Wiring

If app DI already constructor-injects use cases directly, no module binding required. If manual use-case providers exist, add the new checkout use cases there.

Acceptance:

- Hilt graph compiles after adding checkout presentation in Iteration 3.

## Testing Plan

Add tests under:

- `domain/src/test/kotlin/shopzen/domain/cart/usecase/`
- `domain/src/test/kotlin/shopzen/domain/checkout/usecase/`

Required tests:

- `GetCartTotalUseCase`
  - no discount
  - percent-derived discount passed in
  - fixed discount passed in
  - discount larger than subtotal clamps total to zero
- `ValidateCashLimitUseCase`
  - below limit true
  - equal limit true
  - above limit false
- `GetAvailablePaymentMethodsUseCase`
  - below/equal returns COD and online
  - above returns online only
- `PlaceOrderUseCase`
  - success returns order confirmation
  - failure returns same failure message
- `ValidateCouponUseCase`
  - delegates to repository if explicit validation contract is chosen

## Verification Commands

Run:

```powershell
.\gradlew.bat :domain:test
.\gradlew.bat :app:compileDebugKotlin
```

Passing criteria:

- Domain tests pass.
- App compile still passes.
- No new Android imports in `:domain`.

## Handoff To Iteration 3

Iteration 3 may start only after:

- Checkout use cases exist and compile.
- Coupon validation contract is settled.
- COD rules are unit-tested.
- `PlaceOrderUseCase` has success/failure tests.
- Cart coupon totals are deterministic and tested.
