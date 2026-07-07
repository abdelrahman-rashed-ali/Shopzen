# Product Detail Screen UI Remake Plan

## Summary
- Remake `ProductDetailScreen` as full requirements-first detail page.
- Scope starts in UI, but this plan intentionally calls out missing presentation contract needed for wishlist and reviews.
- Keep current add-to-cart, variant selection, stock handling, and snackbar/login effects.

## Current Behavior To Preserve
- Top app bar with back navigation.
- Product loading, error, and success states.
- Image gallery, variant selection, stock-aware add-to-cart, and description.
- Cart effects collection for snackbar and login redirect.

## UI Remake Goals
- Make product detail feel complete and premium, not thin.
- Add first-class wishlist heart in summary area.
- Add first-class reviews section with summary and preview list.
- Keep add-to-cart highly visible with sticky CTA treatment.

## Layout Structure
- Header with back and cart context.
- Large gallery first, with pager indicators and image count.
- Product summary block with vendor, title, wishlist heart, price, and review snapshot.
- Variant and size selectors next, with disabled out-of-stock options.
- Inline stock and selection feedback near CTA.
- Sticky add-to-cart CTA bar.
- Description section below summary.
- Reviews section below description.

## Component Changes
- Split content into `ProductGallerySection`, `ProductSummarySection`, `ProductOptionsSection`, `ProductDescriptionSection`, and `ProductReviewsSection`.
- Add heart action UI to summary block.
- Add review summary row and preview cards or rows.
- Rework CTA area into persistent bottom action instead of one inline button only.

## State And Navigation Constraints
- Keep current add-to-cart and variant logic in ViewModels.
- Keep login redirect and snackbar effects handling at route layer.
- Current `ProductDetailState` is not enough for final design because wishlist and reviews are missing.
- Any new wishlist or reviews behavior must stay outside composables and flow through presentation state and callbacks.

## Acceptance Criteria
- Product detail can render gallery, summary, variants, sticky CTA, description, and reviews without crowding.
- Variant and stock behavior still work.
- Add-to-cart remains obvious and reachable.
- Wishlist and reviews have designed placements, not afterthought placeholders.

## Open Follow-Up Gaps
- Follow-Up Gap: add presentation state for `isWishlisted`, wishlist toggle events, and review summary/list loading branches.
- Follow-Up Gap: if reviews require new domain/data flow, that work is separate from this UI remake.
