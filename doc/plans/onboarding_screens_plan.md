# Onboarding Flow Implementation Plan

This document outlines the approach for designing and implementing the premium onboarding flow for Shopzen.

## Understanding of the Current Design Language
Based on `DESIGN.md`, Shopzen's design language is "Minimal Premium / Modern Soft UI". We will STRICTLY adhere to the existing tokens and styling:
- No new colors: We will use the existing `color.action.primary.background` (which maps to White in dark mode, Black in light mode) for the primary button, ignoring the gold accent from the reference images to stay true to the Shopzen brand.
- No new fonts: We will stick exclusively to `Plus Jakarta Sans` without introducing serif accents.
- Soft rounded surfaces (radius-full for buttons) and spacious layouts.

## Proposed Onboarding Structure
The onboarding will consist of 3 screens displayed in a swipeable pager, along with persistent top navigation (Skip) and bottom controls (Progress + CTA).
1. **Screen 1: Discover the Extraordinary**
   - **Content:** Hero image. Title focusing on discovery. Description of curated collections.
   - **CTA:** "EXPLORE COLLECTION"
2. **Screen 2: Authenticity Guaranteed**
   - **Content:** Hero image. Title focusing on authenticity and certification.
   - **CTA:** "NEXT DESTINATION"
3. **Screen 3: Delivered with Care**
   - **Content:** Hero image. Title focusing on premium white-glove service.
   - **CTA:** "GET STARTED"

## Final Screen Wireframe Structure
```
[ Top Bar:  Optional Logo                   Skip Button ]

[              Hero Image (Radius 18dp)                 ]
[                                                       ]
[                                                       ]

[         Title (Heading 1 or Display, Centered)        ]
[   Description (Body or Section Title, Centered)       ]

[             Progress Dots (Animated pill)             ]
[             [ Primary Full-Width CTA ]                ]
```

## Exact Routes to Add
- `onboarding` -> The root graph for onboarding features.
- `onboarding/main` -> The single screen housing the `HorizontalPager`.

## Content Strategy & Assets
- We will not use placeholder generation. The image placeholders will just use standard colored boxes or `AsyncImage` with `null` model, ready to accept remote URLs when available.

## Architecture and File Structure
We will treat onboarding as its own feature area to decouple it from authentication.

**List of new files only:**
- **Domain:**
  - `domain/onboarding/repository/OnboardingRepository.kt`
  - `domain/onboarding/usecase/CompleteOnboardingUseCase.kt`
  - `domain/onboarding/usecase/HasCompletedOnboardingUseCase.kt`
- **Data:**
  - `data/onboarding/repository/OnboardingRepositoryImpl.kt`
  - `data/onboarding/local/LocalOnboardingDataSource.kt`
- **Presentation:**
  - `presentation/onboarding/screen/OnboardingScreen.kt`
  - `presentation/onboarding/components/OnboardingPage.kt`
  - `presentation/onboarding/components/OnboardingPagerIndicator.kt`
  - `presentation/onboarding/viewmodel/OnboardingViewModel.kt`
  - `presentation/onboarding/intent/OnboardingIntent.kt`
  - `presentation/onboarding/state/OnboardingState.kt`

## Data Persistence Approach
We will use **SharedPreferences** wrapped in a `LocalOnboardingDataSource` to persist the `hasCompletedOnboarding` flag, avoiding the introduction of new `DataStore` dependencies to keep the project slim and use the existing local layer patterns.

## State Management Approach
- **ViewModel (`OnboardingViewModel`):** Will ONLY handle the completion state and persistence (calling the use case). It will emit a one-time event (`OnboardingEvent.NavigateNext`) when complete.
- **Pager State:** Will be managed entirely within the Composable UI (`rememberPagerState()`), keeping the ViewModel minimal.
- **Navigation:** Handled externally by the NavGraph observing the ViewModel's completion event.

## Animation Approach
- Standard smooth horizontal swipe provided by `HorizontalPager`.
- `animateDpAsState` and `animateColorAsState` for the width and color of the active dot.
