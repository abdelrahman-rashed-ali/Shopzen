# Order History Screen Plan

## Summary
Build `OrderHistoryScreen` for authenticated profile users. Requirements from `AGENTS.md`: account/profile feature, route `main/profile/orders`, MVI, `GetOrderHistoryUseCase`, Shopify REST `GET /customers/{id}/orders.json`, localized UI/errors, no hardcoded strings/colors, and Shopzen design system components/tokens.

## Key Changes
- Add domain order pieces under existing `profile` package reality: `Order`, `OrderLineItem`, `OrderRepository`, `GetOrderHistoryUseCase`.
- Add Ktor REST data implementation: `RemoteOrderDataSource`, order DTOs, mapper, `OrderRepositoryImpl`.
- Fetch `customers/{customerId}/orders.json` with `status=any` and `limit=50`; map DTOs to domain only.
- Bind `OrderRepositoryImpl` in `RepositoryModule`.
- Add presentation MVI: `OrderHistoryState(error: UiText?)`, `OrderHistoryIntent`, `OrderHistoryViewModel`.
- Add `OrderHistoryScreen` and `OrderHistoryCard` using `LocalShopzenColors`, `ShopzenSpacing`, `ShopzenShapes`, `ShopzenBody`, `ShopzenHeading3`, `ShopzenSmall`, and `LoadingIndicator`.
- Add localized strings in `presentation/src/main/res/values/strings.xml`.
- Update profile navigation with `OrderHistoryClicked`, `ORDER_HISTORY`, typed `OrderHistoryScreen`, and `AppNavHost` registration.

## UI Rules
- Screen states: loading, empty, error with retry, list content.
- Cards show localized order number, localized date, localized statuses, item count, and formatted total.
- Currency uses `NumberFormat` and `Currency`, not hardcoded symbols.
- Dates use `DateTimeFormatter` with current locale; fallback to string resource.
- Existing `ErrorScreen` / `EmptyStateView` are not used for this screen because they contain hardcoded text/colors.

## Tests
- Domain: `GetOrderHistoryUseCaseTest` delegates and propagates success/failure.
- Data: mapper maps price, currency, statuses, empty/null line items; remote data source calls `customers/{id}/orders.json` with Ktor MockEngine.
- Presentation: ViewModel emits loading then orders, maps auth failure to localized `UiText`, maps repository failure to `order_history_error_load_failed`, retry reloads.
- Build: `.\gradlew.bat :domain:test :data:test :presentation:test :app:assembleDebug`.

## Assumptions
- Follow existing `shopzen.domain.profile` / `shopzen.presentation.profile` packages, despite root `AGENTS.md` saying `account`.
- Order detail screen stays separate; history cards expose `onOrderClick(orderId)` but full `OrderDetailScreen` is not implemented here.
- No new dependencies needed.
