package shopzen.presentation.common.components.floating_island

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import shopzen.presentation.common.components.floating_island.models.AnimationConfig
import shopzen.presentation.common.components.floating_island.models.MotionPreset
import shopzen.presentation.common.components.floating_island.models.ReduceMotionBehavior
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
//  Animation State Holder
//
//  Holds all Animatable instances for the island lifecycle.
//  Values are read exclusively in graphicsLayer / Modifier.layout blocks
//  (deferred-reads skill) — never via `by` delegate in composition.
// ─────────────────────────────────────────────────────────────────────────────

@Stable
internal class FloatingIslandAnimationState(
    /** Vertical offset for drop + dismiss. */
    val offsetY: Animatable<Float, AnimationVector1D>,
    /** Uniform scale during drop entrance. */
    val scale: Animatable<Float, AnimationVector1D>,
    /** Radial glow opacity. */
    val glowAlpha: Animatable<Float, AnimationVector1D>,
    /** Animated container width in px. */
    val containerWidthPx: Animatable<Float, AnimationVector1D>,
    /** Animated container height in px. */
    val containerHeightPx: Animatable<Float, AnimationVector1D>,
    /** Content-only alpha for Phase 4 reveal. */
    val contentAlpha: Animatable<Float, AnimationVector1D>,
    /** Drag offset during swipe-to-dismiss gesture. */
    val dragOffsetY: Animatable<Float, AnimationVector1D>,
) {
    /** Snap every value to its initial (hidden) position. */
    suspend fun resetToInitial(initialOffsetPx: Float, initialSizePx: Float) {
        offsetY.snapTo(initialOffsetPx)
        scale.snapTo(0.3f)
        glowAlpha.snapTo(0f)
        containerWidthPx.snapTo(initialSizePx)
        containerHeightPx.snapTo(initialSizePx)
        contentAlpha.snapTo(0f)
        dragOffsetY.snapTo(0f)
    }

    /** Snap every value to the fully-expanded shown position. */
    suspend fun snapToShown(expandedWidthPx: Float, contentHeightPx: Float) {
        offsetY.snapTo(0f)
        scale.snapTo(1f)
        glowAlpha.snapTo(1f)
        containerWidthPx.snapTo(expandedWidthPx)
        containerHeightPx.snapTo(contentHeightPx)
        contentAlpha.snapTo(1f)
        dragOffsetY.snapTo(0f)
    }
}

@Composable
internal fun rememberFloatingIslandAnimationState(
    initialOffsetPx: Float,
    initialSizePx: Float,
): FloatingIslandAnimationState = remember {
    FloatingIslandAnimationState(
        offsetY = Animatable(initialOffsetPx),
        scale = Animatable(0.3f),
        glowAlpha = Animatable(0f),
        containerWidthPx = Animatable(initialSizePx),
        containerHeightPx = Animatable(initialSizePx),
        contentAlpha = Animatable(0f),
        dragOffsetY = Animatable(0f),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Resolved Animation Specs
//
//  Generated from AnimationConfig + MotionPreset.
//  Internal only — callers never see spring/tween/easing.
// ─────────────────────────────────────────────────────────────────────────────

internal data class ResolvedAnimationSpecs(
    val enabled: Boolean,
    // Phase 1 — Drop
    val dropDurationMs: Int,
    val dropEasing: Easing,
    // Phase 2 — Horizontal expansion
    val horizontalExpandDelayMs: Int,
    val horizontalExpandDurationMs: Int,
    val horizontalExpandEasing: Easing,
    val overshootFraction: Float,
    // Phase 3 — Vertical expansion
    val verticalExpandDelayMs: Int,
    val verticalExpandSpring: SpringSpec<Float>,
    // Phase 4 — Content fade
    val contentFadeDelayMs: Int,
    val contentFadeDurationMs: Int,
    // Dismiss
    val dismissDurationMs: Int,
    val dismissEasing: Easing,
    // Swipe
    val snapBackSpring: SpringSpec<Float>,
    val swipeDismissEnabled: Boolean,
    // Glow
    val glowEnabled: Boolean,
    // Reduce motion
    val reduceMotionBehavior: ReduceMotionBehavior,
    val isReduceMotionActive: Boolean,
)

/**
 * Resolve [AnimationConfig] to concrete animation specs.
 *
 * @param systemAnimatorScale System's `ANIMATOR_DURATION_SCALE` (default 1 f).
 * @param isReduceMotionEnabled True when the system signals reduced motion.
 */
internal fun AnimationConfig.resolve(
    systemAnimatorScale: Float = 1f,
    isReduceMotionEnabled: Boolean = false,
): ResolvedAnimationSpecs {
    if (!enabled) {
        return disabledSpecs(swipeDismissEnabled, glowEnabled, reduceMotionBehavior)
    }

    // ── Base timing per preset ────────────────────────────────────────────
    val baseDropMs: Int
    val baseHExpandMs: Int
    val baseHExpandDelayMs: Int
    val baseVExpandDelayMs: Int
    val baseFadeDelayMs: Int
    val baseDismissMs: Int
    val baseVDamping: Float
    val baseVStiffness: Float
    val baseOvershoot: Float

    when (preset) {
        MotionPreset.SUBTLE -> {
            baseDropMs = 280; baseHExpandMs = 250; baseHExpandDelayMs = 160
            baseVExpandDelayMs = 260; baseFadeDelayMs = 140; baseDismissMs = 160
            baseVDamping = 0.9f; baseVStiffness = 800f; baseOvershoot = 0.03f
        }
        MotionPreset.DEFAULT -> {
            baseDropMs = 350; baseHExpandMs = 300; baseHExpandDelayMs = 200
            baseVExpandDelayMs = 320; baseFadeDelayMs = 180; baseDismissMs = 200
            baseVDamping = 0.8f; baseVStiffness = 700f; baseOvershoot = 0.06f
        }
        MotionPreset.EXPRESSIVE -> {
            baseDropMs = 420; baseHExpandMs = 380; baseHExpandDelayMs = 240
            baseVExpandDelayMs = 380; baseFadeDelayMs = 220; baseDismissMs = 240
            baseVDamping = 0.65f; baseVStiffness = 500f; baseOvershoot = 0.10f
        }
        MotionPreset.SNAPPY -> {
            baseDropMs = 200; baseHExpandMs = 180; baseHExpandDelayMs = 100
            baseVExpandDelayMs = 180; baseFadeDelayMs = 100; baseDismissMs = 120
            baseVDamping = 0.95f; baseVStiffness = 1200f; baseOvershoot = 0.02f
        }
        MotionPreset.SYSTEM -> {
            baseDropMs = 350; baseHExpandMs = 300; baseHExpandDelayMs = 200
            baseVExpandDelayMs = 320; baseFadeDelayMs = 180; baseDismissMs = 200
            baseVDamping = 0.8f; baseVStiffness = 700f; baseOvershoot = 0.06f
        }
    }

    // ── Scale durations ───────────────────────────────────────────────────
    val durationScale = motionScale * if (preset == MotionPreset.SYSTEM) {
        systemAnimatorScale
    } else {
        1f
    }
    val fadeDurationMs = contentFadeDuration.inWholeMilliseconds.toInt()

    return ResolvedAnimationSpecs(
        enabled = true,
        dropDurationMs = (baseDropMs * durationScale).roundToInt(),
        dropEasing = FastOutSlowInEasing,
        horizontalExpandDelayMs = (baseHExpandDelayMs * durationScale).roundToInt(),
        horizontalExpandDurationMs = (baseHExpandMs * durationScale).roundToInt(),
        horizontalExpandEasing = LinearOutSlowInEasing,
        overshootFraction = if (overshootEnabled) baseOvershoot else 0f,
        verticalExpandDelayMs = (baseVExpandDelayMs * durationScale).roundToInt(),
        verticalExpandSpring = spring(
            dampingRatio = baseVDamping,
            stiffness = baseVStiffness,
        ),
        contentFadeDelayMs = (baseFadeDelayMs * durationScale).roundToInt(),
        contentFadeDurationMs = (fadeDurationMs * durationScale).roundToInt(),
        dismissDurationMs = (baseDismissMs * durationScale).roundToInt(),
        dismissEasing = FastOutLinearInEasing,
        snapBackSpring = spring(dampingRatio = 0.82f, stiffness = 650f),
        swipeDismissEnabled = swipeDismissEnabled,
        glowEnabled = glowEnabled,
        reduceMotionBehavior = reduceMotionBehavior,
        isReduceMotionActive = isReduceMotionEnabled,
    )
}

private fun disabledSpecs(
    swipe: Boolean,
    glow: Boolean,
    reduceBehavior: ReduceMotionBehavior,
) = ResolvedAnimationSpecs(
    enabled = false,
    dropDurationMs = 0, dropEasing = FastOutSlowInEasing,
    horizontalExpandDelayMs = 0, horizontalExpandDurationMs = 0,
    horizontalExpandEasing = LinearOutSlowInEasing, overshootFraction = 0f,
    verticalExpandDelayMs = 0,
    verticalExpandSpring = spring(dampingRatio = 1f, stiffness = 1000f),
    contentFadeDelayMs = 0, contentFadeDurationMs = 0,
    dismissDurationMs = 0, dismissEasing = FastOutLinearInEasing,
    snapBackSpring = spring(dampingRatio = 1f, stiffness = 1000f),
    swipeDismissEnabled = swipe, glowEnabled = glow,
    reduceMotionBehavior = reduceBehavior, isReduceMotionActive = false,
)

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers used by the Dialog's LaunchedEffect orchestrator
// ─────────────────────────────────────────────────────────────────────────────

/** Build a [tween] spec from resolved drop parameters. */
internal fun ResolvedAnimationSpecs.dropTween() =
    tween<Float>(durationMillis = dropDurationMs, easing = dropEasing)

/** Build a [tween] for horizontal expansion (pre-overshoot segment). */
internal fun ResolvedAnimationSpecs.horizontalExpandTween() =
    tween<Float>(durationMillis = horizontalExpandDurationMs, easing = horizontalExpandEasing)

/** Build a [tween] for the overshoot settle-back. */
internal fun ResolvedAnimationSpecs.overshootSettleTween() =
    tween<Float>(
        durationMillis = (horizontalExpandDurationMs * 0.3f).roundToInt(),
        easing = LinearOutSlowInEasing,
    )

/** Build a [tween] for Phase 4 content fade. */
internal fun ResolvedAnimationSpecs.contentFadeTween() =
    tween<Float>(durationMillis = contentFadeDurationMs)

/** Build a [tween] for dismiss exit. */
internal fun ResolvedAnimationSpecs.dismissTween() =
    tween<Float>(durationMillis = dismissDurationMs, easing = dismissEasing)
