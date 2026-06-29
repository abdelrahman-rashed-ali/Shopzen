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
    val Xs   = 8.dp    // small badges, tags
    val Sm   = 12.dp   // chips, tags
    val Md   = 16.dp   // inputs, smaller cards
    val Lg   = 20.dp   // standard cards
    val Xl   = 24.dp   // product cards, major cards
    val Xxl  = 28.dp   // bottom sheets, dialogs
    val Full = 9999.dp // buttons, pills, circles
}

object ShopzenShapes {
    val Xs         = RoundedCornerShape(ShopzenRadius.Xs)
    val Sm         = RoundedCornerShape(ShopzenRadius.Sm)
    val Md         = RoundedCornerShape(ShopzenRadius.Md)
    val Lg         = RoundedCornerShape(ShopzenRadius.Lg)
    val Xl         = RoundedCornerShape(ShopzenRadius.Xl)
    val Xxl        = RoundedCornerShape(ShopzenRadius.Xxl)
    val Full       = RoundedCornerShape(percent = 50)
    val BottomSheet = RoundedCornerShape(topStart = ShopzenRadius.Xxl, topEnd = ShopzenRadius.Xxl)
}

// ── Spacing — 8pt grid ────────────────────────────────────────────────────────
// DESIGN.md §9

object ShopzenSpacing {
    val Xs  = 4.dp
    val Sm  = 8.dp
    val Md  = 12.dp
    val Lg  = 16.dp   // card padding
    val Xl  = 20.dp   // screen horizontal padding
    val Xxl = 24.dp   // section vertical rhythm (start)
    val X3l = 32.dp   // section vertical rhythm (end)
    val X4l = 40.dp
    val X5l = 48.dp
}

// ── Component Sizes ────────────────────────────────────────────────────────────
// DESIGN.md §10, §11, §12, §13, §14

object ShopzenSize {
    // Buttons (all variants)
    val ButtonHeight        = 52.dp
    val ButtonMinWidth      = 120.dp

    // Quantity stepper button
    val QuantityButtonSize  = 40.dp

    // Floating Action Button
    val FabDiameter         = 60.dp

    // Inputs
    val SearchBarHeight     = 48.dp
    val TextFieldHeight     = 52.dp

    // Top App Bar
    val TopBarHeight        = 56.dp

    // Bottom Navigation
    val BottomNavHeight     = 72.dp

    // Navigation Drawer
    val DrawerWidth         = 280.dp

    // Icons
    val IconNav             = 24.dp  // navigation bar icons
    val IconCommon          = 22.dp  // standard icons
    val IconAction          = 20.dp  // action / inline icons

    // Category chip image circle
    val CategoryCircle      = 56.dp

    // Wishlist heart button (floating on product card)
    val WishlistButton      = 32.dp

    // Rating badge
    val StarSize            = 16.dp

    // Notification badge
    val BadgeSize           = 16.dp

    // Accessibility minimum
    val TouchTargetMin = 48.dp

    // Bottom sheet handle
    val SheetHandleWidth    = 32.dp
    val SheetHandleHeight   = 4.dp
}

// ── Elevation / Shadow ────────────────────────────────────────────────────────
// DESIGN.md §4, §6
// Use with Modifier.shadow() — values mirror DESIGN.md elevation table.
// M3 tonal elevation is disabled in ShopzenTheme; use these manually.

object ShopzenElevation {
    val None    = 0.dp
    val Xs      = 2.dp
    val Sm      = 4.dp   // standard cards
    val Md      = 8.dp   // elevated panels
    val Lg      = 16.dp  // dialogs
    val Xl      = 20.dp  // FAB / modals
}

// ── Motion ─────────────────────────────────────────────────────────────────────
// DESIGN.md §15 — timing and scale constants for animations

object ShopzenMotion {
    const val DurationFast   = 150
    const val DurationNormal = 200
    const val DurationSlow   = 300
    const val DurationSpring = 250

    // Press scale factors
    const val ScaleButton = 0.96f
    const val ScaleCard   = 0.98f
    const val ScaleWishlistPeak = 1.10f
}

// ── Border Widths ─────────────────────────────────────────────────────────────
// DESIGN.md §7

object ShopzenBorderWidth {
    val Default  = 1.dp
    val Focused  = 1.5.dp
    val Selected = 2.dp
    val Focus    = 2.dp    // focus ring
    val NavTop   = 0.5.dp  // dark bottom nav hairline
}
