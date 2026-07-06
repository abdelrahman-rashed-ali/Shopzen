package shopzen.presentation.common.components.floating_island.models

/**
 * Semantic vibration mode. Maps internally to platform [android.os.VibrationEffect]
 * or Compose [androidx.compose.ui.hapticfeedback.HapticFeedback] as a fallback.
 *
 * Callers pick intent; the component resolves hardware-level details.
 */
enum class VibrationMode {
    /** No vibration. */
    NONE,

    /** Subtle tick — confirmation without drawing attention. */
    LIGHT,

    /** Positive outcome — double-pulse pattern. */
    SUCCESS,

    /** Attention needed — medium sustained buzz. */
    WARNING,

    /** Negative outcome — heavy buzz. */
    ERROR,

    /** Single strong tap. */
    IMPACT,

    /** Cursor/picker-style micro-tick. */
    SELECTION,

    /** Caller-defined pattern via [VibrationConfig.customPattern]. */
    CUSTOM,
}
