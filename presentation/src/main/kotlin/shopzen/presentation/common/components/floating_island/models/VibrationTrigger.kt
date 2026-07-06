package shopzen.presentation.common.components.floating_island.models

/**
 * Lifecycle point at which haptic feedback fires.
 */
enum class VibrationTrigger {
    /** No automatic haptic trigger. */
    NONE,

    /** Fire when show animation begins (Phase 1 start). */
    ON_SHOW,

    /** Fire when show animation completes (all phases done). */
    ON_SHOW_FINISHED,

    /** Fire when programmatic dismiss starts. */
    ON_DISMISS,

    /** Fire when the action slot is tapped (caller-driven). */
    ON_ACTION,

    /** Fire when swipe-to-dismiss threshold is reached. */
    ON_SWIPE_DISMISS,
}
