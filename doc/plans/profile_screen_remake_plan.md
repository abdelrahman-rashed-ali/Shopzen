# Profile Screen UI Remake Plan

## Summary
- Remake `ProfileScreen` around identity summary and account editing.
- Scope is UI only inside `:presentation`.
- Locked direction: avatar initials, full name, email, masked password row, edit CTA, and order-history navigation.

## Current Behavior To Preserve
- Guest and signed-in branches.
- Current auth-required dialog behavior for gated actions.
- Sign-out confirmation dialog.
- Existing `onNavigateToPersonalDetails` and `onNavigateToOrderHistory` callback paths.

## UI Remake Goals
- Replace shortcut-card feel with structured profile summary.
- Show circular avatar with first letters of first and last names.
- Make full name, email, and password row easy to scan.
- Keep profile focused on identity and order history only.

## Layout Structure
- Authenticated state starts with identity hero card.
- Identity card shows avatar, full name, and email.
- Account details list below it: full name row, email row, masked password row.
- Single edit CTA routes to personal-details update flow.
- Order history row or card appears as primary secondary action.
- Sign-out action stays near bottom.
- Guest state becomes compact login-forward branch without empty account cards.

## Component Changes
- Replace current multi-card action layout with profile summary card plus detail rows.
- Add initials avatar component.
- Add reusable detail-row pattern for name, email, and password.
- Remove saved-address card from this screen.

## State And Navigation Constraints
- Keep current `ProfileState` for v1 UI where possible.
- Use existing `onNavigateToPersonalDetails` for edit action.
- Keep order-history navigation.
- Do not keep addresses entry on profile; it moves to settings.

## Acceptance Criteria
- Signed-in profile shows avatar, full name, email, masked password row, edit CTA, and order history.
- Guest profile shows a clean login-first presentation.
- Sign-out confirmation still works.
- No saved-address entry remains on profile.

## Open Follow-Up Gaps
- Follow-Up Gap: if password editing needs dedicated route or copy beyond personal details, that is later behavior/design work.
- Follow-Up Gap: initials formatting rules for missing last name should be finalized during implementation.
