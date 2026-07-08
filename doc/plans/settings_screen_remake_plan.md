# Settings Screen UI Remake Plan

## Summary
- Remake `SettingsScreen` into preferences plus account-tools hub.
- Scope is UI only inside `:presentation`.
- This screen now owns saved-address navigation entry.

## Current Behavior To Preserve
- Shared `ShopzenTopAppBar` and `ShopzenBottomBar`.
- Guest access to settings.
- Local preference changes for currency, language, and theme.
- Auth-required flow for Firebase sync.
- Logout confirmation flow for signed-in users.

## UI Remake Goals
- Make settings feel structured instead of two plain cards.
- Separate preferences, account actions, and account tools.
- Add clear entry point for saved addresses.
- Keep guest and signed-in differences obvious.

## Layout Structure
- Shared shell remains.
- Section 1: preferences for currency, language, and theme.
- Section 2: account actions for login, logout, and sync.
- Section 3: tools/navigation, including saved addresses.
- Error messaging remains visible but secondary to main content.

## Component Changes
- Rework current card layout into stronger grouped sections.
- Keep chip controls for preferences but improve spacing and hierarchy.
- Add saved-address row or card with clear affordance.
- Keep login/logout/sync actions visually distinct.

## State And Navigation Constraints
- Keep `SettingsState` shape for v1 remake.
- Keep guest-vs-auth behavior in ViewModel.
- Preserve auth gating outside UI where possible.
- Screen needs one new navigation callback: `onNavigateToAddresses`.

## Acceptance Criteria
- Preferences remain editable for guests.
- Signed-in users can reach saved addresses from settings.
- Sync still requires authentication.
- Logout still requires confirmation.

## Open Follow-Up Gaps
- Follow-Up Gap: app navigation wiring must add `onNavigateToAddresses`.
- Follow-Up Gap: guest tap behavior for saved addresses must follow existing auth-guard conventions in app navigation.
