package shopzen.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  DIMENS & SHAPES — Shopzen Design System v1.0
//  Source of truth: DESIGN.md §8, §9, §4, §15
//  Theme-independent — identical in light and dark.
// ─────────────────────────────────────────────────────────────────────────────

// ── Border Radius ─────────────────────────────────────────────────────────────
// DESIGN.md §8 — shape language never changes between themes

object ShopzenRadius {
    val XS = 8.dp
    val SM = 12.dp
    val MD = 16.dp
    val LG = 20.dp
    val XL = 24.dp
    val XXL = 28.dp
    val Full = 9999.dp
}

object ShopzenShapes {
    val XS = RoundedCornerShape(ShopzenRadius.XS)
    val SM = RoundedCornerShape(ShopzenRadius.SM)
    val MD = RoundedCornerShape(ShopzenRadius.MD)
    val LG = RoundedCornerShape(ShopzenRadius.LG)
    val XL = RoundedCornerShape(ShopzenRadius.XL)
    val XXL = RoundedCornerShape(ShopzenRadius.XXL)

    val Full = RoundedCornerShape(percent = 50)

    val BottomSheet = RoundedCornerShape(
        topStart = ShopzenRadius.XXL,
        topEnd = ShopzenRadius.XXL,
    )
}

// ── Spacing — 8pt grid ────────────────────────────────────────────────────────
// DESIGN.md §9

object ShopzenSpacing {
    val XS = 4.dp
    val SM = 8.dp
    val MD = 12.dp
    val LG = 16.dp
    val XL = 20.dp
    val XXL = 24.dp
    val XXXL = 32.dp
    val XXXXL = 40.dp
    val XXXXXL = 48.dp
}

// ── Component Sizes ────────────────────────────────────────────────────────────
// DESIGN.md §10, §11, §12, §13, §14

object ShopzenSize {

    // Buttons
    val ButtonHeight = 52.dp
    val ButtonMinWidth = 120.dp

    // Quantity stepper
    val QuantityButtonSize = 40.dp

    // FAB
    val FabDiameter = 60.dp

    // Inputs
    val SearchBarHeight = 48.dp
    val TextFieldHeight = 52.dp

    // Top App Bar
    val TopBarHeight = 56.dp

    // Bottom Navigation
    val BottomNavHeight = 72.dp

    // Drawer
    val DrawerWidth = 280.dp

    // Icons
    val IconNav = 24.dp
    val IconCommon = 22.dp
    val IconAction = 20.dp

    // Category
    val CategoryCircle = 56.dp

    // Product Card
    val WishlistButton = 32.dp

    // Ratings / Badge
    val StarSize = 16.dp
    val BadgeSize = 16.dp

    // Accessibility
    val TouchTargetMin = 48.dp

    // Bottom Sheet
    val SheetHandleWidth = 32.dp
    val SheetHandleHeight = 4.dp
}

// ── Elevation / Shadow ────────────────────────────────────────────────────────
// DESIGN.md §4, §6

object ShopzenElevation {
    val None = 0.dp
    val XS = 2.dp
    val SM = 4.dp
    val MD = 8.dp
    val LG = 16.dp
    val XL = 20.dp
}

// ── Motion ─────────────────────────────────────────────────────────────────────
// DESIGN.md §15

object ShopzenMotion {
    const val DurationFast = 150
    const val DurationNormal = 200
    const val DurationSlow = 300
    const val DurationSpring = 250

    const val ScaleButton = 0.96f
    const val ScaleCard = 0.98f
    const val ScaleWishlistPeak = 1.10f
}

// ── Border Widths ─────────────────────────────────────────────────────────────
// DESIGN.md §7

object ShopzenBorderWidth {
    val Default = 1.dp
    val Focused = 1.5.dp
    val Selected = 2.dp
    val Focus = 2.dp
    val NavTop = 0.5.dp
}
