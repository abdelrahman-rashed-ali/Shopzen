package shopzen.presentation.common.components.floating_island

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color tokens consumed by [FloatingIslandDialog].
 *
 * Obtain theme-derived defaults via [FloatingIslandDefaults.colors].
 */
@Immutable
data class FloatingIslandColors(
    val containerColor: Color,
    val contentColor: Color,
    val titleColor: Color,
    val subtitleColor: Color,
    val glowColor: Color,
)
