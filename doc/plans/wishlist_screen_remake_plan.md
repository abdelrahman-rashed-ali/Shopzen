# Wishlist Screen UI Remake Plan

## Summary
- Remake `WishlistScreen` into saved-collection experience with stronger product hierarchy and better empty state.
- Scope is UI only inside `:presentation`.
- Keep current remove confirmation, product open, and add-to-cart intent dispatch behavior.

## Current Behavior To Preserve
- Shared `ShopzenTopAppBar` and `ShopzenBottomBar`.
- Loading, error, empty, and populated branches.
- Product navigation from wishlist cards.
- Remove item confirmation dialog.
- Existing `AddToCart` intent dispatch from wishlist cards.

## UI Remake Goals
- Make wishlist feel curated, not like generic grid dump.
- Improve image-to-text hierarchy and action clarity.
- Make item count and collection context easier to scan.
- Upgrade empty state so it feels intentional and commerce-focused.

## Layout Structure
- Shared shell remains.
- Header block with title, item count, and short helper text.
- Populated state uses cleaner adaptive grid with stronger card spacing.
- Empty state centers message and clear "Shop now" action.
- Error and loading states remain simple and readable.

## Component Changes
- Refresh `WishlistItemCard` layout for clearer image, vendor, title, price, remove, and add-to-cart actions.
- Tighten section spacing and card rhythm.
- Keep confirmation dialog outside card content, as it is today.

## State And Navigation Constraints
- Keep `WishlistState` shape as-is.
- Keep `showRemoveItemDialog` and `pendingRemovalItemId`.
- Do not add business logic to card composables.
- Keep auth and navigation gating outside this screen.

## Acceptance Criteria
- Empty and populated wishlist states both look intentional.
- Item count remains visible and reactive.
- Remove action still requires confirmation.
- Product open and add-to-cart actions still dispatch existing flows.

## Open Follow-Up Gaps
- Real add-to-cart completion feedback is still a later behavior gap because current ViewModel leaves cart integration minimal.
