package shopzen.presentation.common.components.floating_island

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import shopzen.presentation.common.components.floating_island.models.AnimationConfig
import shopzen.presentation.common.components.floating_island.models.VibrationConfig
import kotlin.time.Duration

/**
 * Full configuration for [FloatingIslandDialog].
 *
 * [colors] is nullable so the composable can resolve theme-derived defaults
 * at composition time via [FloatingIslandDefaults.colors]. Pass explicit
 * colors to override the theme.
 *
 * @property title Primary text — always visible.
 * @property subtitle Optional secondary text below [title].
 * @property colors Color overrides; `null` → theme defaults.
 * @property vibration Haptic configuration.
 * @property animation Motion configuration.
 * @property shape Clip shape for the island container.
 * @property dismissAfter Auto-dismiss delay; `null` → manual dismiss only.
 * @property dismissOnOutsideTap Tap outside the island to dismiss.
 */
@Immutable
data class FloatingIslandConfig(
    val title: String,
    val subtitle: String? = null,
    val colors: FloatingIslandColors? = null,
    val vibration: VibrationConfig = VibrationConfig(),
    val animation: AnimationConfig = AnimationConfig(),
    val shape: Shape = RoundedCornerShape(999.dp),
    val dismissAfter: Duration? = null,
    val dismissOnOutsideTap: Boolean = true,
)
