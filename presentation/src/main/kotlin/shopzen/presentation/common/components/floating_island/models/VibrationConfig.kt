package shopzen.presentation.common.components.floating_island.models

import androidx.compose.runtime.Immutable

/**
 * Haptic configuration — semantic controls only, never raw platform APIs.
 *
 * Defaults: [VibrationMode.SUCCESS] on [VibrationTrigger.ON_SHOW_FINISHED]
 * at [HapticIntensity.NORMAL].
 *
 * Arrays in [customPattern] / [customAmplitudes] are nullable and only
 * relevant when [mode] == [VibrationMode.CUSTOM]. Callers MUST NOT mutate
 * them after construction — the [@Immutable] contract depends on it.
 */
@Immutable
data class VibrationConfig(
    val enabled: Boolean = true,
    val mode: VibrationMode = VibrationMode.SUCCESS,
    val trigger: VibrationTrigger = VibrationTrigger.ON_SHOW_FINISHED,
    val intensity: HapticIntensity = HapticIntensity.NORMAL,
    val customPattern: LongArray? = null,
    val customAmplitudes: IntArray? = null,
    val fallbackToComposeHaptics: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VibrationConfig) return false
        return enabled == other.enabled &&
            mode == other.mode &&
            trigger == other.trigger &&
            intensity == other.intensity &&
            customPattern contentEquals other.customPattern &&
            customAmplitudes contentEquals other.customAmplitudes &&
            fallbackToComposeHaptics == other.fallbackToComposeHaptics
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + mode.hashCode()
        result = 31 * result + trigger.hashCode()
        result = 31 * result + intensity.hashCode()
        result = 31 * result + customPattern.contentHashCode()
        result = 31 * result + customAmplitudes.contentHashCode()
        result = 31 * result + fallbackToComposeHaptics.hashCode()
        return result
    }
}
