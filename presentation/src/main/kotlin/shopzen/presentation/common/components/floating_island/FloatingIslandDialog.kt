package shopzen.presentation.common.components.floating_island

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import shopzen.presentation.common.components.floating_island.models.ReduceMotionBehavior
import shopzen.presentation.common.components.floating_island.models.VibrationTrigger
import shopzen.presentation.theme.ShopzenSpacing
import kotlin.math.abs
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
//  FloatingIslandDialog — Public API
//
//  iOS Dynamic Island–style notification component.
//  Drops from top as glowing circle → expands → reveals content → dismissable.
//
//  Skill compliance:
//  ┌────────────────────────────────────┬────────────────────────────────────┐
//  │ compose-animations                │ Animatable for cancelable motion   │
//  │ compose-modifier-and-layout-style │ modifier param on root, no hardcode│
//  │ compose-recomposition-performance │ All .value reads in graphicsLayer  │
//  │ compose-side-effects              │ LaunchedEffect for orchestration   │
//  │ compose-stability-diagnostics     │ @Immutable on config / colors      │
//  │ compose-state-deferred-reads      │ Zero composition-phase reads       │
//  │ compose-state-authoring           │ remember { Animatable }, no bare var│
//  │ compose-slot-api-pattern          │ Nullable slots, Defaults object    │
//  └────────────────────────────────────┴────────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Floating island dialog inspired by iOS Dynamic Island.
 *
 * @param visible Controls show / hide. Transition animations play automatically.
 * @param config Full island configuration — title, colors, haptics, motion.
 * @param onDismiss Called after dismiss animation completes.
 * @param modifier Applied to the outermost layout (the full-screen overlay).
 * @param illustrationContent Optional leading illustration slot (Icon, Image, Lottie…).
 * @param actionContent Optional trailing action slot (Button, IconButton…).
 */
@Composable
fun FloatingIslandDialog(
    visible: Boolean,
    config: FloatingIslandConfig,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    illustrationContent: (@Composable () -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null,
) {
    // ── Resolve theme colors ─────────────────────────────────────────────
    val resolvedColors = config.colors ?: FloatingIslandDefaults.colors()

    // ── Resolve animation specs ──────────────────────────────────────────
    val context = LocalContext.current
    val systemAnimatorScale = remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
        } catch (_: Exception) {
            1f
        }
    }
    val isReduceMotion = systemAnimatorScale == 0f

    val specs = remember(config.animation, systemAnimatorScale, isReduceMotion) {
        config.animation.resolve(
            systemAnimatorScale = systemAnimatorScale,
            isReduceMotionEnabled = isReduceMotion,
        )
    }

    // ── Pixel conversions ────────────────────────────────────────────────
    val density = LocalDensity.current
    val expandedWidthPx = with(density) { FloatingIslandDefaults.ExpandedWidth.toPx() }
    val initialSizePx = with(density) { FloatingIslandDefaults.InitialDiameter.toPx() }
    val initialOffsetPx = with(density) { FloatingIslandDefaults.DropDistance.toPx() }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // ── Animation state ──────────────────────────────────────────────────
    val animState = rememberFloatingIslandAnimationState(
        initialOffsetPx = -initialOffsetPx,
        initialSizePx = initialSizePx,
    )

    // One-shot measured content height (layout → composition back-write; fires
    // once during initial measurement, not per-frame — structurally unavoidable
    // to provide the vertical-expansion animation target).
    var targetContentHeightPx by remember { mutableIntStateOf(0) }

    // Keep dialog in composition during exit animation.
    var showDialog by remember { mutableStateOf(false) }

    // ── Haptics ──────────────────────────────────────────────────────────
    val hapticFeedback = LocalHapticFeedback.current

    // ── Show / hide orchestration ────────────────────────────────────────
    LaunchedEffect(visible) {
        if (visible) {
            showDialog = true
            animState.resetToInitial(-initialOffsetPx, initialSizePx)

            // ── Reduce motion fast-paths ─────────────────────────────────
            if (!specs.enabled || (specs.isReduceMotionActive && specs.reduceMotionBehavior == ReduceMotionBehavior.DISABLE)) {
                val contentH = snapshotFlow { targetContentHeightPx }
                    .first { it > 0 }
                animState.snapToShown(expandedWidthPx, contentH.toFloat())
                fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SHOW_FINISHED)
                return@LaunchedEffect
            }

            if (specs.isReduceMotionActive && specs.reduceMotionBehavior == ReduceMotionBehavior.MINIMIZE) {
                val contentH = snapshotFlow { targetContentHeightPx }
                    .first { it > 0 }
                animState.offsetY.snapTo(0f)
                animState.scale.snapTo(1f)
                animState.glowAlpha.snapTo(if (specs.glowEnabled) 1f else 0f)
                animState.containerWidthPx.snapTo(expandedWidthPx)
                animState.containerHeightPx.snapTo(contentH.toFloat())
                animState.contentAlpha.animateTo(1f, specs.contentFadeTween())
                fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SHOW_FINISHED)
                return@LaunchedEffect
            }

            if (specs.isReduceMotionActive && specs.reduceMotionBehavior == ReduceMotionBehavior.KEEP_FADE_ONLY) {
                val contentH = snapshotFlow { targetContentHeightPx }
                    .first { it > 0 }
                animState.offsetY.snapTo(0f)
                animState.scale.snapTo(1f)
                animState.containerWidthPx.snapTo(expandedWidthPx)
                animState.containerHeightPx.snapTo(contentH.toFloat())
                animState.glowAlpha.snapTo(if (specs.glowEnabled) 1f else 0f)
                animState.contentAlpha.animateTo(1f, specs.contentFadeTween())
                fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SHOW_FINISHED)
                return@LaunchedEffect
            }

            // ── Full animation ───────────────────────────────────────────
            // Fire ON_SHOW haptic
            fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SHOW)

            // Wait for content measurement
            val contentH = snapshotFlow { targetContentHeightPx }
                .first { it > 0 }

            // Phase 1 — Drop (starts immediately, concurrent launches)
            val dropJob = launch {
                launch { animState.offsetY.animateTo(0f, specs.dropTween()) }
                launch { animState.scale.animateTo(1f, specs.dropTween()) }
                launch {
                    animState.glowAlpha.animateTo(
                        if (specs.glowEnabled) 1f else 0f,
                        specs.dropTween(),
                    )
                }
            }

            // Phase 2 — Horizontal expansion (delayed from start)
            launch {
                delay(specs.horizontalExpandDelayMs.toLong())
                if (specs.overshootFraction > 0f) {
                    val overshootTarget = expandedWidthPx * (1f + specs.overshootFraction)
                    animState.containerWidthPx.animateTo(
                        overshootTarget,
                        specs.horizontalExpandTween(),
                    )
                    animState.containerWidthPx.animateTo(
                        expandedWidthPx,
                        specs.overshootSettleTween(),
                    )
                } else {
                    animState.containerWidthPx.animateTo(
                        expandedWidthPx,
                        specs.horizontalExpandTween(),
                    )
                }
            }

            // Phase 3 — Vertical expansion (delayed from start)
            launch {
                delay(specs.verticalExpandDelayMs.toLong())
                animState.containerHeightPx.animateTo(
                    contentH.toFloat(),
                    specs.verticalExpandSpring,
                )
            }

            // Phase 4 — Content alpha (delayed from start)
            launch {
                delay(specs.contentFadeDelayMs.toLong())
                animState.contentAlpha.animateTo(1f, specs.contentFadeTween())
            }

            // Fire ON_SHOW_FINISHED haptic after drop completes
            dropJob.join()
            fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SHOW_FINISHED)
        } else if (showDialog) {
            // ── Exit animation ───────────────────────────────────────────
            fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_DISMISS)

            if (!specs.enabled || (specs.isReduceMotionActive && specs.reduceMotionBehavior == ReduceMotionBehavior.DISABLE)) {
                showDialog = false
                return@LaunchedEffect
            }

            launch { animState.contentAlpha.animateTo(0f, specs.dismissTween()) }
            launch { animState.glowAlpha.animateTo(0f, specs.dismissTween()) }
            animState.offsetY.animateTo(
                -initialOffsetPx,
                specs.dismissTween(),
            )
            showDialog = false
        }
    }

    // ── Auto-dismiss timer ───────────────────────────────────────────────
    if (config.dismissAfter != null) {
        LaunchedEffect(visible, config.dismissAfter) {
            if (visible) {
                delay(config.dismissAfter.inWholeMilliseconds)
                onDismiss()
            }
        }
    }

    // ── Guard: nothing to render ─────────────────────────────────────────
    if (!showDialog) return

    // ── Render ────────────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = config.dismissOnOutsideTap,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Outside-tap catcher (transparent overlay)
            if (config.dismissOnOutsideTap) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                )
            }

            // ── Island container ─────────────────────────────────────────
            IslandContainer(
                config = config,
                resolvedColors = resolvedColors,
                animState = animState,
                specs = specs,
                expandedWidthPx = expandedWidthPx,
                onContentMeasured = { heightPx ->
                    if (heightPx > 0 && heightPx != targetContentHeightPx) {
                        targetContentHeightPx = heightPx
                    }
                },
                onSwipeDismiss = {
                    fireHaptic(context, config, hapticFeedback, VibrationTrigger.ON_SWIPE_DISMISS)
                    onDismiss()
                },
                modifier = Modifier.padding(top = statusBarTop + ShopzenSpacing.SM),
                illustrationContent = illustrationContent,
                actionContent = actionContent,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Private: Island container with transforms, layout, glow, swipe
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IslandContainer(
    config: FloatingIslandConfig,
    resolvedColors: FloatingIslandColors,
    animState: FloatingIslandAnimationState,
    specs: ResolvedAnimationSpecs,
    expandedWidthPx: Float,
    onContentMeasured: (Int) -> Unit,
    onSwipeDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    illustrationContent: (@Composable () -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null,
) {
    val swipeScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            // ── 1. Animated transforms (draw phase reads) ────────────────
            .graphicsLayer {
                translationY = animState.offsetY.value + animState.dragOffsetY.value
                scaleX = animState.scale.value
                scaleY = animState.scale.value

                // Drag feedback: alpha dims proportionally
                val dragRatio = if (size.height > 0f) {
                    (abs(animState.dragOffsetY.value) /
                        (size.height * FloatingIslandDefaults.SwipeDismissThresholdFraction))
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
                alpha = lerp(1f, 0.85f, dragRatio)
            }
            // ── 2. Animated size (layout phase reads) ────────────────────
            //    Reports animated w × h to parent; content measured at full
            //    expanded width then clipped below.
            .layout { measurable, _ ->
                val expandedW = expandedWidthPx.roundToInt()
                val placeable = measurable.measure(
                    androidx.compose.ui.unit.Constraints(
                        minWidth = expandedW,
                        maxWidth = expandedW,
                        maxHeight = androidx.compose.ui.unit.Constraints.Infinity,
                    ),
                )
                // One-shot content height capture (layout → composition write,
                // fires once — not frame-rate back-writing).
                onContentMeasured(placeable.height)

                val w = animState.containerWidthPx.value
                    .roundToInt()
                    .coerceIn(1, expandedW)
                val h = animState.containerHeightPx.value
                    .roundToInt()
                    .coerceIn(1, placeable.height)

                layout(w, h) {
                    placeable.placeRelative(
                        x = (w - placeable.width) / 2,
                        y = (h - placeable.height) / 2,
                    )
                }
            }
            // ── 3. Glow shadow (draw phase reads, AFTER layout) ──────────
            //    Now sees animated w × h. Shape gives rounded shadow outline.
            .graphicsLayer {
                shape = config.shape
                clip = false

                val glowAlphaValue = animState.glowAlpha.value

                // Drag reduces glow proportionally
                val dragRatio = if (size.height > 0f) {
                    (abs(animState.dragOffsetY.value) /
                        (size.height * FloatingIslandDefaults.SwipeDismissThresholdFraction))
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
                val effectiveGlowAlpha = lerp(
                    glowAlphaValue,
                    glowAlphaValue * 0.6f,
                    dragRatio,
                )

                if (effectiveGlowAlpha > 0f && specs.glowEnabled) {
                    shadowElevation = 12f * effectiveGlowAlpha
                    ambientShadowColor = resolvedColors.glowColor.copy(
                        alpha = 0.2f * effectiveGlowAlpha,
                    )
                    spotShadowColor = resolvedColors.glowColor.copy(
                        alpha = 0.3f * effectiveGlowAlpha,
                    )
                }
            }
            // ── 4. Clip + background ─────────────────────────────────────
            .clip(config.shape)
            .background(resolvedColors.containerColor)
            // ── 5. Swipe to dismiss ──────────────────────────────────────
            .then(
                if (specs.swipeDismissEnabled) {
                    Modifier.swipeToDismiss(animState, specs, onSwipeDismiss, swipeScope)
                } else {
                    Modifier
                },
            ),
    ) {
        FloatingIslandContentLayout(
            title = config.title,
            subtitle = config.subtitle,
            colors = resolvedColors,
            illustrationContent = illustrationContent,
            actionContent = actionContent,
            modifier = Modifier.graphicsLayer {
                alpha = animState.contentAlpha.value
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Private: Swipe-to-dismiss modifier
// ─────────────────────────────────────────────────────────────────────────────

private fun Modifier.swipeToDismiss(
    animState: FloatingIslandAnimationState,
    specs: ResolvedAnimationSpecs,
    onDismiss: () -> Unit,
    scope: CoroutineScope,
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        val velocityTracker = VelocityTracker()

        detectVerticalDragGestures(
            onDragStart = {
                velocityTracker.resetTracking()
            },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity()
                val currentDrag = animState.dragOffsetY.value
                val containerHeight = animState.containerHeightPx.value

                scope.launch {
                    val shouldDismiss =
                        abs(currentDrag) >= containerHeight * FloatingIslandDefaults.SwipeDismissThresholdFraction ||
                            velocity.y <= -FloatingIslandDefaults.SwipeDismissVelocityThreshold

                    if (shouldDismiss) {
                        animState.dragOffsetY.animateTo(
                            targetValue = -containerHeight * 2,
                            animationSpec = specs.dismissTween(),
                        )
                        onDismiss()
                    } else {
                        animState.dragOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = specs.snapBackSpring,
                        )
                    }
                }
            },
            onDragCancel = {
                scope.launch {
                    animState.dragOffsetY.animateTo(0f, specs.snapBackSpring)
                }
            },
            onVerticalDrag = { change, dragAmount ->
                // Only upward drag (negative Y). Clamp to 0 to prevent downward drag.
                if (dragAmount < 0f || animState.dragOffsetY.value < 0f) {
                    change.consume()
                    velocityTracker.addPosition(
                        change.uptimeMillis,
                        Offset(change.position.x, change.position.y),
                    )
                    scope.launch {
                        val newOffset = (animState.dragOffsetY.value + dragAmount).coerceAtMost(0f)
                        animState.dragOffsetY.snapTo(newOffset)
                    }
                }
            },
        )
    },
)

// ─────────────────────────────────────────────────────────────────────────────
//  Private: Haptic fire helper
// ─────────────────────────────────────────────────────────────────────────────

private fun fireHaptic(
    context: android.content.Context,
    config: FloatingIslandConfig,
    composeFallback: HapticFeedback,
    currentTrigger: VibrationTrigger,
) {
    if (config.vibration.trigger == currentTrigger) {
        triggerHaptic(context, config.vibration, composeFallback)
    }
}
