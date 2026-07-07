# Search Screen UI Remake Plan

## Summary
- Remake `SearchScreen` into two intentional modes: discovery before search and results after search.
- Scope is UI only inside `:presentation`.
- Keep current debounce, filters, sorting, and navigation behavior.

## Current Behavior To Preserve
- Shared `ShopzenTopAppBar` and `ShopzenBottomBar`.
- Search input debounce at 300 ms.
- Filter dialog open/apply/dismiss flow.
- Filter then sort behavior from current ViewModel.
- Result grid, empty results branch, and product navigation.

## UI Remake Goals
- Make search feel focused instead of mixed discovery and results blocks in one flat feed.
- Keep discovery mode useful before the first query.
- Make results mode clearer with stronger count, filters, and sort controls.
- Improve visual distinction between suggestions, collections, and products.

## Layout Structure
- Shared shell remains.
- Search field stays near top and visually dominant.
- Discovery mode shows suggestions first, then curated collections, then filter entry point.
- Results mode shows result count, active filters row, sort/filter actions, then product grid.
- Empty state stays in results mode with clear reset or retry affordance.

## Component Changes
- Split screen into `SearchContent`, `SearchDiscoverContent`, and `SearchResultsContent`.
- Refine suggestion chips and collection cards into a more consistent style family.
- Add stronger active-filter row presentation for results mode.
- Improve results header and empty-state composition.

## State And Navigation Constraints
- Keep `SearchState` shape for v1 remake.
- Keep `showFilterSheet`, `hasSearched`, `selectedCategory`, `selectedBrand`, and `selectedSortOption`.
- Do not move debounce logic out of the ViewModel.
- Keep navigation callbacks unchanged.

## Acceptance Criteria
- Pre-search discovery state renders clearly before any search.
- Results state renders clearly after query or suggestion selection.
- Debounce behavior still works.
- Filter dialog and apply flow still work.
- Empty results state still renders with a clear fallback path.

## Open Follow-Up Gaps
- If future design needs inline sort chips instead of dialog-only controls, that is a later UI enhancement.
- Wishlist actions are still absent from search result cards and would need separate product-card behavior review if added later.
