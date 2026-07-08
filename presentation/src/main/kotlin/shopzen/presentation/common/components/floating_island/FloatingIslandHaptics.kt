package shopzen.presentation.common.components.floating_island

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import shopzen.presentation.common.components.floating_island.models.HapticIntensity
import shopzen.presentation.common.components.floating_island.models.VibrationConfig
import shopzen.presentation.common.components.floating_island.models.VibrationMode

// ─────────────────────────────────────────────────────────────────────────────
//  Haptics — internal helper
//
//  Maps semantic VibrationMode + HapticIntensity to platform Vibrator API.
//  Falls back to Compose HapticFeedback when the Vibrator isn't available or
//  when the caller opts in via fallbackToComposeHaptics.
//
//  Called from LaunchedEffect keyed on animation phase — never from the
//  composable body (side-effects skill).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trigger haptic feedback per [config].
 *
 * Call from an effect scope only — not from the composable body.
 */
internal fun triggerHaptic(
    context: Context,
    config: VibrationConfig,
    composeFallback: HapticFeedback? = null,
) {
    if (!config.enabled || config.mode == VibrationMode.NONE) return

    val vibrator = obtainVibrator(context)

    if (vibrator != null && vibrator.hasVibrator()) {
        vibrateWithEffect(vibrator, config)
    } else if (config.fallbackToComposeHaptics && composeFallback != null) {
        composeFallback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

// ── Platform vibration ───────────────────────────────────────────────────────

@Suppress("DEPRECATION")
private fun obtainVibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

private fun vibrateWithEffect(vibrator: Vibrator, config: VibrationConfig) {
    val amplitude = config.intensity.toAmplitude()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = when (config.mode) {
            VibrationMode.NONE -> return
            VibrationMode.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            VibrationMode.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            VibrationMode.WARNING -> VibrationEffect.createOneShot(80L, amplitude)
            VibrationMode.ERROR -> VibrationEffect.createOneShot(120L, amplitude)
            VibrationMode.IMPACT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            VibrationMode.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            VibrationMode.CUSTOM -> createCustomEffect(config)
        }
        if (effect != null) {
            vibrator.vibrate(effect)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // API 26–28: predefined effects unavailable; use one-shot fallback
        val durationMs = when (config.mode) {
            VibrationMode.NONE -> return
            VibrationMode.LIGHT, VibrationMode.SELECTION -> 20L
            VibrationMode.SUCCESS -> 40L
            VibrationMode.WARNING -> 80L
            VibrationMode.ERROR -> 120L
            VibrationMode.IMPACT -> 30L
            VibrationMode.CUSTOM -> config.customPattern?.sum() ?: 40L
        }
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(40L)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createCustomEffect(config: VibrationConfig): VibrationEffect? {
    val pattern = config.customPattern ?: return null
    val amplitudes = config.customAmplitudes
    return if (amplitudes != null && amplitudes.size == pattern.size) {
        VibrationEffect.createWaveform(pattern, amplitudes, -1)
    } else {
        VibrationEffect.createWaveform(pattern, -1)
    }
}

private fun HapticIntensity.toAmplitude(): Int = when (this) {
    HapticIntensity.VERY_LIGHT -> 30
    HapticIntensity.LIGHT -> 80
    HapticIntensity.NORMAL -> 150
    HapticIntensity.STRONG -> 255
}
