package shopzen.presentation.common.components.floating_island.models

/**
 * Behavior when the system's reduce-motion / animator-duration-scale=0 is active.
 */
enum class ReduceMotionBehavior {
    /** Instant show/hide — no animation at all. */
    DISABLE,

    /** Skip drop and expansion; keep a short overall fade. */
    MINIMIZE,

    /** Skip drop and expansion; keep only the content fade-in. */
    KEEP_FADE_ONLY,
}
