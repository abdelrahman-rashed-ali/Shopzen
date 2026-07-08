package shopzen.presentation.common.components.floating_island

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import shopzen.presentation.common.theme.LocalShopzenColors

/**
 * Defaults and constants for [FloatingIslandDialog].
 *
 * Follows Material 3 `XxxDefaults` convention (slot-api-pattern skill §4).
 */
object FloatingIslandDefaults {

    // ── Sizing ────────────────────────────────────────────────────────────

    /** Initial diameter of the glowing circle before expansion. */
    val InitialDiameter = 72.dp

    /** Target width after horizontal expansion. */
    val ExpandedWidth = 320.dp

    /** Vertical drop distance above the top edge. */
    val DropDistance = 250.dp

    // ── Swipe thresholds ──────────────────────────────────────────────────

    /** Fraction of container height that triggers swipe dismiss. */
    const val SwipeDismissThresholdFraction = 0.4f

    /** Upward velocity (px/s) that triggers swipe dismiss. */
    const val SwipeDismissVelocityThreshold = 1400f

    // ── Colors ────────────────────────────────────────────────────────────

    /**
     * Theme-aware color defaults.
     *
     * Reads [LocalShopzenColors]; call at composition time only.
     */
    @Composable
    fun colors(
        containerColor: Color = LocalShopzenColors.current.surfaceElevated,
        contentColor: Color = LocalShopzenColors.current.textPrimary,
        titleColor: Color = LocalShopzenColors.current.textPrimary,
        subtitleColor: Color = LocalShopzenColors.current.textSecondary,
        glowColor: Color = if (LocalShopzenColors.current.isDark) {
            Color.White
        } else {
            Color.Black
        },
    ): FloatingIslandColors = FloatingIslandColors(
        containerColor = containerColor,
        contentColor = contentColor,
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        glowColor = glowColor,
    )
}
