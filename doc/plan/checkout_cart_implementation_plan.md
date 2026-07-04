# Checkout and Cart Coupon Features Implementation Plan

This plan details the implementation of the Cart Coupon feature and the Complete Order (Checkout) process. As requested, no code is included, only detailed requirements. The implementation is divided into iterations targeting specific architectural layers: Data, Domain, and Presentation. 

> [!IMPORTANT]
> As requested, all coupon-related logic (apply, remove, validate) has been shifted to the Cart feature. This must be fully functional and tested on the Cart screen before the checkout process begins.
> All errors (network, validation) must be properly handled and mapped to localized strings to provide a smooth user experience.

## Iteration 1: Data Layer

This iteration focuses on the network and local data sources, DTOs, entities, and repository implementations. We will implement these elements first without any UI or domain logic.

### Cart Data Layer Updates
*   **API & Networking:** Implement the REST API call to validate discount codes (`GET /price_rules/{id}/discount_codes.json`).
*   **DTOs:** Create DTOs to parse the discount code validation response from Shopify.
*   **Local Database (Room):** Ensure `CartEntity` or a supplementary local store can persist applied coupons temporarily during a session.
*   **Repository Implementation:** 
    *   Update `CartRepositoryImpl` to include coupon validation using the remote data source.
    *   Implement mapping from discount DTOs to domain models.
    *   Implement specific, localized error mapping for invalid coupons (e.g., "Coupon expired", "Invalid code") utilizing safe API call wrappers.

### Checkout Data Layer
*   **API & Networking:** Implement the GraphQL `orderCreate` mutation for order placement.
*   **DTOs:** Create GraphQL request/response models for order creation.
*   **Repository Implementation:**
    *   Implement `CheckoutRepositoryImpl` to handle order submission.
    *   Map domain errors to localized strings for order submission failures (e.g., "Network error", "Payment failed", "Out of stock").

---

## Iteration 2: Domain Layer

This iteration defines the pure Kotlin models, repository interfaces, and use cases. It contains zero Android framework dependencies.

### Cart Domain Layer Updates
*   **Domain Models:** 
    *   Update `Cart` to include `discountAmount`, `totalPrice`, and `appliedCoupon`.
    *   Create `DiscountCode` and `DiscountType` models.
*   **Use Cases:**
    *   Create `ValidateCouponUseCase` to interact with the repository and return a strongly-typed `Result` with localized error messages.
    *   Create `ApplyCouponUseCase` to execute the discount logic and update the `Cart` state.
    *   Create `RemoveCouponUseCase` to clear the applied coupon and recalculate the original subtotal.
    *   Update `GetCartTotalUseCase` to account for the applied discount dynamically.

### Checkout Domain Layer
*   **Domain Models:**
    *   Create `Checkout` (tracking line items, address, subtotal, discount, total, currency, payment method).
    *   Create `PaymentMethod` enum (`CASH_ON_DELIVERY`, `ONLINE_PAYMENT`).
    *   Create `OrderConfirmation` model.
*   **Use Cases:**
    *   Create `GetAvailablePaymentMethodsUseCase` to dynamically filter COD based on `Constants.MAX_COD_AMOUNT`.
    *   Create `ValidateCashLimitUseCase` to return `false` if `totalPrice > Constants.MAX_COD_AMOUNT`.
    *   Create `PlaceOrderUseCase` to submit the order and return the result.
    *   Create `ClearCartUseCase` to trigger after a successful order placement.

---

## Iteration 3: Presentation Layer

This final iteration focuses on the MVI architecture (Intent, State, ViewModel) and the Compose UI components.

### Cart Presentation Updates
*   **MVI State & Intents:**
    *   Update `CartState` to track the applied coupon, discount amount, and coupon-specific inline error messages.
    *   Add intents: `ApplyCoupon(code)`, `RemoveCoupon`, `DismissCouponError`.
*   **ViewModel:**
    *   Handle coupon validation. On error, update the inline field error state (no dialogs).
    *   On success, update the cart state with the new totals and display the applied coupon.
*   **UI Components:**
    *   Add a text field for coupon entry on the `CartScreen`.
    *   Implement an inline error text view below the coupon field that only appears when a validation error occurs.
    *   Add a "Remove" or "X" button next to applied coupons.
    *   Update the Cart Summary section to reflect the subtotal, discount deduction, and final total.

### Checkout Presentation
*   **MVI State & Intents:**
    *   Create `CheckoutState` covering loading, errors, selected payment, and order submission dialog flags.
    *   Add intents: `SelectPaymentMethod`, `RequestPlaceOrder`, `ConfirmPlaceOrder`, `DismissDialog`.
*   **ViewModel:**
    *   Enforce payment method rules (hide COD if limit exceeded).
    *   Manage the `ConfirmationDialog` state before placing the order.
    *   On successful order placement, trigger cart clearing and navigate to `OrderConfirmationScreen`.
*   **UI Components:**
    *   **CheckoutSummaryScreen:** Display line items, shipping address, and the final price breakdown.
    *   **PaymentScreen:** Radio button selection for payment methods, dynamically disabling/hiding COD if necessary.
    *   **OrderConfirmationScreen:** Success state, order number, and a button to return Home.
    *   **Dialogs:** Implement the mandatory "Confirm and place your order?" confirmation dialog.
