# Home Screen UI Remake Plan

## Summary
- Remake `HomeScreen` into clear commerce landing page with stronger hierarchy and cleaner section rhythm.
- Scope is UI only inside `:presentation`.
- Keep current data sources, navigation callbacks, and wishlist dialog behavior.

## Current Behavior To Preserve
- Shared `ShopzenTopAppBar` and `ShopzenBottomBar`.
- Loading, error, and success branches from `HomeState`.
- Product tap, category tap, search, profile, wishlist, cart, and settings navigation.
- Wishlist add/remove confirmation dialogs triggered from home product cards.

## UI Remake Goals
- Make home feel editorial instead of one long plain white column.
- Reintroduce brands as visible content because `brands` already exists in state.
- Give hero, categories, and new arrivals distinct visual weight.
- Improve spacing, section headers, and CTA clarity without changing business behavior.

## Layout Structure
- Top app bar stays fixed in shared shell.
- Hero banner first, edge-to-edge, with short headline, subheadline, and one primary CTA.
- Brand section second, using horizontal rail or compact grid fed by `state.brands`.
- Category quick-access section third, using current category data.
- New arrivals section fourth, with stronger header and product grid.
- Bottom navigation stays unchanged.

## Component Changes
- Split screen into `HomeContent`, `HomeHeroSection`, `HomeBrandSection`, `HomeCategorySection`, and `HomeNewArrivalsSection`.
- Rework `FeaturedBanner` styling and spacing.
- Add dedicated brand UI instead of leaving `brands` unused.
- Tighten product-grid density and section header presentation.

## State And Navigation Constraints
- Keep `HomeState` shape as-is for this remake.
- Do not move wishlist logic into composables beyond current callback wiring.
- Keep route-level auth gating in navigation layer, not in screen UI.
- Keep current confirmation dialog flows for wishlist actions.

## Acceptance Criteria
- Home visibly renders hero, brands, categories, and new arrivals.
- `state.brands` is used in UI.
- Loading and error states still work.
- Product and category navigation still work.
- Wishlist add/remove flows still show confirmation dialogs and dispatch current intents.

## Open Follow-Up Gaps
- Brand destination routing is still a navigation follow-up because current app host leaves brand navigation TODO.
- Product list "View all" destination remains a navigation follow-up if route stays unfinished.
