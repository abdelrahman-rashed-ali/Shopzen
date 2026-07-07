# Add/Edit Address Screen UI Remake Plan

## Summary
- Full remake for `AddEditAddressScreen` with map-assisted flow and manual fallback.
- Locked direction: use Mapbox search plus map pick, autofill fields after place selection, and still allow manual editing.
- This plan is UI-first but explicitly depends on later state and platform updates.

## Current Behavior To Preserve
- Add and edit modes in one screen.
- Back navigation.
- Save action and existing save-success return behavior.
- Inline field error handling instead of dialog-based validation.

## UI Remake Goals
- Make address entry faster with place search and map pick.
- Support both assisted flow and manual-only flow.
- Replace current generic stacked form with clear contact and address sections.
- Keep manual editing possible after autofill.

## Layout Structure
- Top mode switch between map-assisted entry and manual entry.
- Map-assisted mode includes Mapbox search/autocomplete and map picker with pin.
- After selection, form autofills and remains editable.
- Contact section includes first name, last name, and phone.
- Address section includes line 1, line 2, city, province/state, country, and postal code.
- Save CTA stays fixed near bottom or in sticky footer.

## Component Changes
- Replace current single `recipientName` emphasis with separate identity fields.
- Add place-search bar, map preview/picker area, and autofill confirmation treatment.
- Add reusable field rows with clearer required-state styling.
- Improve visual separation between contact data and address data.

## State And Navigation Constraints
- Current `AddressEditState` is not enough for final design.
- Current screen still preloads existing address values in edit mode and that behavior must remain.
- New UI must still keep manual-only path functional.
- Business logic, geocoding, and persistence must stay outside composables.

## Acceptance Criteria
- User can search or pick a location and get autofilled fields.
- User can ignore map flow and complete form manually.
- Autofilled values remain editable before save.
- Add and edit modes both remain supported.
- Inline validation remains visible and clear.

## Open Follow-Up Gaps
- Follow-Up Gap: AGENTS docs must be updated before implementation to allow Mapbox SDK and revised address contract.
- Follow-Up Gap: presentation state must expand for selected place, coordinates, autofill result, first/last name fields, and manual override tracking.
- Follow-Up Gap: if domain/data layers must support structured autofill fields, that work is separate from this UI remake.
