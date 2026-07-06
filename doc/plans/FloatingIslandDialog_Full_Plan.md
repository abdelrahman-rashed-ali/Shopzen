# FloatingIslandDialog — Reusable Jetpack Compose Component Plan

## Goal
Create a reusable Jetpack Compose component inspired by iOS Dynamic Island interactions.

Features:
- Drops from top as glowing circle
- Expands horizontally
- Expands vertically before horizontal finishes
- Configurable vibration
- Icon/SVG support
- Title, optional subtitle, optional action
- Fully theme-driven customization
- Swipe upward to dismiss

---

# Architecture

```text
presentation/
└── components/
    └── floating_island/
        ├── FloatingIslandDialog.kt
        ├── FloatingIslandState.kt
        ├── FloatingIslandConfig.kt
        ├── FloatingIslandAnimation.kt
        ├── FloatingIslandHaptics.kt
        ├── FloatingIslandContent.kt
        ├── FloatingIslandDefaults.kt
        ├── models/
        │   ├── VibrationMode.kt
        │   ├── FloatingIslandAction.kt
        │   └── FloatingIslandIllustration.kt
        └── preview/
```

---

# Public API

```kotlin
@Composable
fun FloatingIslandDialog(
    visible: Boolean,
    config: FloatingIslandConfig,
    onDismiss: () -> Unit
)
```

---

# Config

```kotlin
@Immutable
data class FloatingIslandConfig(
    val illustration: FloatingIslandIllustration?,
    val title: String,
    val subtitle: String? = null,
    val action: FloatingIslandAction? = null,
    val colors: FloatingIslandColors = FloatingIslandDefaults.colors(),
    val vibration: VibrationConfig = VibrationConfig(),
    val animation: AnimationConfig = AnimationConfig(),
    val shape: Shape = RoundedCornerShape(999.dp),
    val dismissAfter: Duration? = null,
    val dismissOnOutsideTap: Boolean = true,
    val swipeToDismissEnabled: Boolean = true,
    val glowEnabled: Boolean = true,
)
```

# Animation System

Use:

```text
Animatable
updateTransition
graphicsLayer
spring
```

Do not use one transition for everything.

## Phase 1 — Drop
- Duration: 350ms
- Y: -250dp → 0dp
- Curve: FastOutSlowIn
- Scale: 0.3 → 1
- Glow: 0 → 100%

## Phase 2 — Horizontal Expansion
- Start: 200ms
- Width: 72dp → 320dp
- Curve: CubicBezier(0.16,1.0,0.3,1.0)
- Overshoot ~6%
- No bounce

## Phase 3 — Vertical Expansion
- Start: 320ms
- Height: 72dp → contentHeight
- spring(dampingRatio=.8f, stiffness=700f)

## Phase 4 — Content Reveal (UPDATED)
Content MUST NOT animate position or scale.

Forbidden:
- AnimatedVisibility
- scaleIn
- slideIn
- translateY
- staggered children
- animateContentSize
- Crossfade

Behavior:
- Content measured at final size
- Content rendered immediately
- Parent expansion naturally reveals content
- Content alpha only

Animation:
- Start: 180ms
- Alpha: 0 → 1
- Duration: 120–160ms
- Finish before container expansion ends

Preferred:

```kotlin
graphicsLayer {
    alpha = contentAlpha
}
```

Use SubcomposeLayout or LookaheadScope.

Goal:
Content already exists → surface reveals it.

---

# Haptics

```kotlin
enum class VibrationMode {
 NONE,
 LIGHT,
 SUCCESS,
 WARNING,
 ERROR,
 IMPACT,
 CUSTOM
}
```

Trigger after Phase 1.

---

# Swipe To Dismiss

Support upward dismissal.

Use:

```kotlin
pointerInput
Animatable
detectVerticalDragGestures
nestedScroll
```

Rules:
- Only upward drag
- No horizontal dismiss
- No downward dismiss
- Content remains fixed

Dismiss when:
- Drag ≥ 40% height
OR
- Velocity ≥ 1400 px/s upward

Else:

```kotlin
spring(
 dampingRatio=.82f,
 stiffness=650f
)
```

Drag visuals:
- translateY only
- opacity 1 → .85
- glow 100 → 60%

No:
- scale
- rotation
- content movement

Dismiss animation:
- 180–220ms
- FastOutLinearIn
- callback after completion

---

# Design System Enforcement

Component MUST use theme tokens.

Source of truth:

```text
presentation/theme/
```

Consume only:
- Color
- Typography
- Shape
- Spacing
- Motion
- Elevation
- Icon sizes

Never introduce local tokens.

Priority:
Explicit Config
→ Theme
→ Material fallback

Verify:
- Dark mode
- Dynamic color
- High contrast
- RTL
- Large font scale

---

# Performance

- Immutable configs
- derivedStateOf
- graphicsLayer
- cache painters
- avoid recomposition storms
- no nested AnimatedVisibility

---

# Success Criteria

- Feels premium at 120Hz
- <700ms animation
- API 24+
- No dropped frames
- Publishable as design-system module
