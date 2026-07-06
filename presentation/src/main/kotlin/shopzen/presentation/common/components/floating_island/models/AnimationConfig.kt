package shopzen.presentation.common.components.floating_island.models

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Motion-intent configuration. Exposes personality, not raw Compose specs.
 *
 * Internal animation specs (spring, tween, easing) are resolved from this
 * config + theme motion tokens at composition time — callers never touch them.
 *
 * @property enabled Master kill-switch — `false` snaps to final state instantly.
 * @property preset Motion personality (see [MotionPreset]).
 * @property totalDuration Advisory cap; individual phase durations are derived
 *   from [preset] and scaled by [motionScale].
 * @property motionScale Multiplier applied to all derived durations. 1 f = normal.
 * @property glowEnabled Whether the radial glow effect renders.
 * @property overshootEnabled Whether horizontal expansion overshoots ~6 %.
 * @property swipeDismissEnabled Whether upward swipe dismisses the island.
 * @property contentFadeDuration Alpha-only fade for inner content (Phase 4).
 *   Must finish before container expansion ends.
 * @property reduceMotionBehavior Behavior when system reduce-motion is active.
 */
@Immutable
data class AnimationConfig(
    val enabled: Boolean = true,
    val preset: MotionPreset = MotionPreset.DEFAULT,
    val totalDuration: Duration = 650.milliseconds,
    val motionScale: Float = 1f,
    val glowEnabled: Boolean = true,
    val overshootEnabled: Boolean = true,
    val swipeDismissEnabled: Boolean = true,
    val contentFadeDuration: Duration = 140.milliseconds,
    val reduceMotionBehavior: ReduceMotionBehavior = ReduceMotionBehavior.MINIMIZE,
)
