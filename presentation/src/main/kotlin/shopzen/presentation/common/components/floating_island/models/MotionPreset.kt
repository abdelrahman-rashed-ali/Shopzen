package shopzen.presentation.common.components.floating_island.models

/**
 * Motion personality preset. The component resolves internal spring/tween specs
 * from this value — callers never touch raw easing or stiffness.
 */
enum class MotionPreset {
    /** Shorter durations, less overshoot, quieter glow. */
    SUBTLE,

    /** Plan-spec defaults (350 ms drop, 0.8 damping, ~6 % overshoot). */
    DEFAULT,

    /** Longer arcs, bouncier springs, larger glow radius. */
    EXPRESSIVE,

    /** Quick springs, high stiffness, minimal overshoot. */
    SNAPPY,

    /** Same as [DEFAULT] but durations scaled by the system animator duration. */
    SYSTEM,
}
