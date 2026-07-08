package shopzen.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────────────────────────────────────
//  SHOPZEN THEME — Design System v1.0  |  Source: DESIGN.md §1,§3,§17,§18,§20
//
//  Key rules:
//    • Primary button inverts: #000000 light ↔ #FFFFFF dark
//    • Dark primary text = #EBEBF0 — never #FFFFFF
//    • Dark bg = #111113 — never #000000 (non-OLED)
//    • surfaceTint = Transparent — M3 tonal elevation disabled; manual surface system
//    • Disabled = explicit colours — never alpha(0.38)
//    • No card borders in either theme
//    • One primary CTA per screen
// ─────────────────────────────────────────────────────────────────────────────

// ── M3 Color Scheme mappings ──────────────────────────────────────────────────

private val ShopzenLightColorScheme = lightColorScheme(
    primary              = Light_Action_Primary_Bg,   // #000000
    onPrimary            = Light_Action_Primary_Fg,   // #FFFFFF
    primaryContainer     = Light_Action_Secondary_Bg, // #F3F3F3
    onPrimaryContainer   = Light_Text_Primary,        // #111111

    secondary            = Light_Text_Secondary,      // #6E6E73
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Light_Background_Tertiary, // #F3F3F3
    onSecondaryContainer = Light_Text_Primary,

    tertiary             = Light_Text_Tertiary,       // #8E8E93
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Light_Background_Secondary,// #F8F8F8
    onTertiaryContainer  = Light_Text_Primary,

    background           = Light_Background_Primary,  // #FFFFFF
    onBackground         = Light_Text_Primary,        // #111111

    surface              = Light_Surface_Card,        // #FFFFFF
    onSurface            = Light_Text_Primary,
    surfaceVariant       = Light_Background_Secondary,// #F8F8F8
    onSurfaceVariant     = Light_Text_Secondary,
    surfaceTint          = Color.Transparent,         // tonal elevation OFF

    outline              = Light_Border_Default,      // #E7E7E7
    outlineVariant       = Light_Border_Subtle,       // #EEEEEE

    error                = Palette_Error,             // #FF3B30
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Light_Action_Danger_Bg,    // #FFF0F0
    onErrorContainer     = Palette_Error,

    scrim                = Light_Overlay,             // rgba(0,0,0,0.40)
    inverseSurface       = Light_Text_Primary,
    inverseOnSurface     = Light_Text_Inverse,
    inversePrimary       = Light_Action_Primary_Bg,
)

private val ShopzenDarkColorScheme = darkColorScheme(
    primary              = Dark_Action_Primary_Bg,    // #FFFFFF — inverted beacon
    onPrimary            = Dark_Action_Primary_Fg,    // #111113
    primaryContainer     = Dark_Action_Secondary_Bg,  // #2A2A2D
    onPrimaryContainer   = Dark_Text_Primary,         // #EBEBF0

    secondary            = Dark_Text_Secondary,       // #AEAEB2
    onSecondary          = Gray950,
    secondaryContainer   = Dark_Surface_Card,         // #232325
    onSecondaryContainer = Dark_Text_Primary,

    tertiary             = Dark_Text_Tertiary,        // #8E8E93
    onTertiary           = Gray950,
    tertiaryContainer    = Dark_Surface_Elevated,     // #2A2A2D
    onTertiaryContainer  = Dark_Text_Primary,

    background           = Dark_Background_Primary,   // #111113
    onBackground         = Dark_Text_Primary,         // #EBEBF0

    surface              = Dark_Background_Tertiary,  // #1C1C1F (base surface)
    onSurface            = Dark_Text_Primary,
    surfaceVariant       = Dark_Surface_Card,         // #232325
    onSurfaceVariant     = Dark_Text_Secondary,
    surfaceTint          = Color.Transparent,         // tonal elevation OFF

    outline              = Dark_Border_Default,       // #38383C
    outlineVariant       = Dark_Border_Subtle,        // #2A2A2D

    error                = Palette_ErrorDark,         // #FF6B6B
    onError              = Gray950,
    errorContainer       = Dark_Action_Danger_Bg,     // rgba(255,107,107,0.12)
    onErrorContainer     = Palette_ErrorDark,

    scrim                = Dark_Overlay,              // rgba(0,0,0,0.60)
    inverseSurface       = Gray100,
    inverseOnSurface     = Gray950,
    inversePrimary       = Dark_Action_Primary_Bg,
)

// ── M3 Shapes ─────────────────────────────────────────────────────────────────

private val ShopzenM3Shapes = Shapes(
    extraSmall = ShopzenShapes.XS,
    small      = ShopzenShapes.SM,
    medium     = ShopzenShapes.MD,
    large      = ShopzenShapes.LG,
    extraLarge = ShopzenShapes.XL,
)

// ── Extended Color Token Object ───────────────────────────────────────────────
//  Full Shopzen token set beyond M3 slots.
//  Access: val c = LocalShopzenColors.current

@Immutable
data class ShopzenColors(
    val backgroundPrimary   : Color,
    val backgroundSecondary : Color,
    val backgroundTertiary  : Color,
    val backgroundOled      : Color,
    // 5-layer surface system (dark elevation via luminance, not borders)
    val surfaceCard         : Color,
    val surfaceElevated     : Color,
    val surfaceDialog       : Color,
    val surfaceTooltip      : Color,
    val surfaceInput        : Color,
    val surfaceNavigation   : Color,
    val surfaceBottomNav    : Color,
    val imageWell           : Color,
    val imagePlaceholder    : Color,
    val overlay             : Color,
    val overlayLight        : Color,
    // Text
    val textPrimary     : Color,
    val textSecondary   : Color,
    val textTertiary    : Color,
    val textDisabled    : Color,
    val textPlaceholder : Color,
    val textHint        : Color,
    val textInverse     : Color,
    val textError       : Color,
    val textSuccess     : Color,
    val textWarning     : Color,
    val textLink        : Color,
    // Icons
    val iconPrimary        : Color,
    val iconSecondary      : Color,
    val iconInactive       : Color,
    val iconDisabled       : Color,
    val iconDestructive    : Color,
    val iconNotification   : Color,
    val iconRating         : Color,
    val iconWishlistEmpty  : Color,
    val iconWishlistFilled : Color,
    // Actions
    val actionPrimaryBg       : Color,
    val actionPrimaryFg       : Color,
    val actionSecondaryBg     : Color,
    val actionSecondaryFg     : Color,
    val actionSecondaryBorder : Color,
    val actionOutlinedBorder  : Color,
    val actionOutlinedFg      : Color,
    val actionGhostFg         : Color,
    val actionDisabledBg      : Color,
    val actionDisabledFg      : Color,
    val actionDangerBg        : Color,
    val actionDangerFg        : Color,
    val actionDangerBorder    : Color,
    val actionSuccessBg       : Color,
    val actionSuccessFg       : Color,
    val actionSuccessBorder   : Color,
    // Status
    val statusSuccess : Color,
    val statusWarning : Color,
    val statusError   : Color,
    val statusInfo    : Color,
    // Borders
    val borderDefault  : Color,
    val borderSubtle   : Color,
    val borderFocus    : Color,
    val borderError    : Color,
    val borderSuccess  : Color,
    val borderSelected : Color,
    val borderNav      : Color,
    // Dividers
    val divider       : Color,
    val dividerStrong : Color,
    // Shadows
    val shadowSm : Color,
    val shadowMd : Color,
    val shadowLg : Color,
    val shadowXl : Color,
    // Interaction
    val selection : Color,
    val ripple    : Color,
    val hover     : Color,
    val pressed   : Color,
    // Skeleton
    val skeletonBase      : Color,
    val skeletonHighlight : Color,
    // Navigation
    val navIconSelected  : Color,
    val navIconInactive  : Color,
    val navBadgeBg       : Color,
    val navBadgeFg       : Color,
    val navBorderTop     : Color,
    // Carousel
    val indicatorActive   : Color,
    val indicatorInactive : Color,
    // Tab bar
    val tabSelectedBg   : Color,
    val tabSelectedFg   : Color,
    val tabUnselectedFg : Color,
    val tabContainer    : Color,
    // Swatches
    val swatchRingSelected : Color,
    // Wishlist card frosted button bg
    val wishlistCardBg     : Color,
    val isDark             : Boolean,
)

val ShopzenLightColors = ShopzenColors(
    backgroundPrimary   = Light_Background_Primary,
    backgroundSecondary = Light_Background_Secondary,
    backgroundTertiary  = Light_Background_Tertiary,
    backgroundOled      = Light_Background_Primary,
    surfaceCard         = Light_Surface_Card,
    surfaceElevated     = Light_Surface_Elevated,
    surfaceDialog       = Light_Surface_Dialog,
    surfaceTooltip      = Light_Surface_Tooltip,
    surfaceInput        = Light_Surface_Input,
    surfaceNavigation   = Light_Surface_Navigation,
    surfaceBottomNav    = Light_Surface_BottomNav,
    imageWell           = Light_Image_Well,
    imagePlaceholder    = Light_Image_Placeholder,
    overlay             = Light_Overlay,
    overlayLight        = Light_Overlay_Light,
    textPrimary         = Light_Text_Primary,
    textSecondary       = Light_Text_Secondary,
    textTertiary        = Light_Text_Tertiary,
    textDisabled        = Light_Text_Disabled,
    textPlaceholder     = Light_Text_Placeholder,
    textHint            = Light_Text_Hint,
    textInverse         = Light_Text_Inverse,
    textError           = Light_Text_Error,
    textSuccess         = Light_Text_Success,
    textWarning         = Light_Text_Warning,
    textLink            = Light_Text_Link,
    iconPrimary         = Light_Icon_Primary,
    iconSecondary       = Light_Icon_Secondary,
    iconInactive        = Light_Icon_Inactive,
    iconDisabled        = Light_Icon_Disabled,
    iconDestructive     = Light_Icon_Destructive,
    iconNotification    = Light_Icon_Notification,
    iconRating          = Light_Icon_Rating,
    iconWishlistEmpty   = Light_Icon_Wishlist_Empty,
    iconWishlistFilled  = Light_Icon_Wishlist_Filled,
    actionPrimaryBg       = Light_Action_Primary_Bg,
    actionPrimaryFg       = Light_Action_Primary_Fg,
    actionSecondaryBg     = Light_Action_Secondary_Bg,
    actionSecondaryFg     = Light_Action_Secondary_Fg,
    actionSecondaryBorder = Color.Transparent,
    actionOutlinedBorder  = Light_Action_Outlined_Border,
    actionOutlinedFg      = Light_Action_Outlined_Fg,
    actionGhostFg         = Light_Action_Ghost_Fg,
    actionDisabledBg      = Light_Action_Disabled_Bg,
    actionDisabledFg      = Light_Action_Disabled_Fg,
    actionDangerBg        = Light_Action_Danger_Bg,
    actionDangerFg        = Light_Action_Danger_Fg,
    actionDangerBorder    = Light_Action_Danger_Border,
    actionSuccessBg       = Light_Action_Success_Bg,
    actionSuccessFg       = Light_Action_Success_Fg,
    actionSuccessBorder   = Light_Action_Success_Border,
    statusSuccess = Light_Status_Success,
    statusWarning = Light_Status_Warning,
    statusError   = Light_Status_Error,
    statusInfo    = Light_Status_Info,
    borderDefault  = Light_Border_Default,
    borderSubtle   = Light_Border_Subtle,
    borderFocus    = Light_Border_Focus,
    borderError    = Light_Border_Error,
    borderSuccess  = Light_Border_Success,
    borderSelected = Light_Border_Selected,
    borderNav      = Color.Transparent,
    divider        = Light_Divider,
    dividerStrong  = Light_Divider_Strong,
    shadowSm = Light_Shadow_Sm,
    shadowMd = Light_Shadow_Md,
    shadowLg = Light_Shadow_Lg,
    shadowXl = Light_Shadow_Xl,
    selection = Light_Selection,
    ripple    = Light_Ripple,
    hover     = Light_Hover,
    pressed   = Light_Pressed,
    skeletonBase      = Light_Skeleton_Base,
    skeletonHighlight = Light_Skeleton_Highlight,
    navIconSelected   = Light_Nav_Icon_Selected,
    navIconInactive   = Light_Nav_Icon_Inactive,
    navBadgeBg        = Light_Nav_Badge_Bg,
    navBadgeFg        = Light_Nav_Badge_Fg,
    navBorderTop      = Color.Transparent,
    indicatorActive   = Light_Indicator_Active,
    indicatorInactive = Light_Indicator_Inactive,
    tabSelectedBg     = Light_Tab_Selected_Bg,
    tabSelectedFg     = Light_Tab_Selected_Fg,
    tabUnselectedFg   = Light_Tab_Unselected_Fg,
    tabContainer      = Light_Tab_Container,
    swatchRingSelected = Light_Swatch_Ring_Selected,
    wishlistCardBg    = Color(0xFFFFFFFF),
    isDark            = false,
)

val ShopzenDarkColors = ShopzenColors(
    backgroundPrimary   = Dark_Background_Primary,
    backgroundSecondary = Dark_Background_Secondary,
    backgroundTertiary  = Dark_Background_Tertiary,
    backgroundOled      = Dark_Background_Oled,
    surfaceCard         = Dark_Surface_Card,
    surfaceElevated     = Dark_Surface_Elevated,
    surfaceDialog       = Dark_Surface_Dialog,
    surfaceTooltip      = Dark_Surface_Tooltip,
    surfaceInput        = Dark_Surface_Input,
    surfaceNavigation   = Dark_Surface_Navigation,
    surfaceBottomNav    = Dark_Surface_BottomNav,
    imageWell           = Dark_Image_Well,
    imagePlaceholder    = Dark_Image_Placeholder,
    overlay             = Dark_Overlay,
    overlayLight        = Dark_Overlay_Light,
    textPrimary         = Dark_Text_Primary,
    textSecondary       = Dark_Text_Secondary,
    textTertiary        = Dark_Text_Tertiary,
    textDisabled        = Dark_Text_Disabled,
    textPlaceholder     = Dark_Text_Placeholder,
    textHint            = Dark_Text_Hint,
    textInverse         = Dark_Text_Inverse,
    textError           = Dark_Text_Error,
    textSuccess         = Dark_Text_Success,
    textWarning         = Dark_Text_Warning,
    textLink            = Dark_Text_Link,
    iconPrimary         = Dark_Icon_Primary,
    iconSecondary       = Dark_Icon_Secondary,
    iconInactive        = Dark_Icon_Inactive,
    iconDisabled        = Dark_Icon_Disabled,
    iconDestructive     = Dark_Icon_Destructive,
    iconNotification    = Dark_Icon_Notification,
    iconRating          = Dark_Icon_Rating,
    iconWishlistEmpty   = Dark_Icon_Wishlist_Empty,
    iconWishlistFilled  = Dark_Icon_Wishlist_Filled,
    actionPrimaryBg       = Dark_Action_Primary_Bg,
    actionPrimaryFg       = Dark_Action_Primary_Fg,
    actionSecondaryBg     = Dark_Action_Secondary_Bg,
    actionSecondaryFg     = Dark_Action_Secondary_Fg,
    actionSecondaryBorder = Dark_Action_Secondary_Border,
    actionOutlinedBorder  = Dark_Action_Outlined_Border,
    actionOutlinedFg      = Dark_Action_Outlined_Fg,
    actionGhostFg         = Dark_Action_Ghost_Fg,
    actionDisabledBg      = Dark_Action_Disabled_Bg,
    actionDisabledFg      = Dark_Action_Disabled_Fg,
    actionDangerBg        = Dark_Action_Danger_Bg,
    actionDangerFg        = Dark_Action_Danger_Fg,
    actionDangerBorder    = Dark_Action_Danger_Border,
    actionSuccessBg       = Dark_Action_Success_Bg,
    actionSuccessFg       = Dark_Action_Success_Fg,
    actionSuccessBorder   = Dark_Action_Success_Border,
    statusSuccess = Dark_Status_Success,
    statusWarning = Dark_Status_Warning,
    statusError   = Dark_Status_Error,
    statusInfo    = Dark_Status_Info,
    borderDefault  = Dark_Border_Default,
    borderSubtle   = Dark_Border_Subtle,
    borderFocus    = Dark_Border_Focus,
    borderError    = Dark_Border_Error,
    borderSuccess  = Dark_Border_Success,
    borderSelected = Dark_Border_Selected,
    borderNav      = Dark_Border_Nav,
    divider        = Dark_Divider,
    dividerStrong  = Dark_Divider_Strong,
    shadowSm = Dark_Shadow_Sm,
    shadowMd = Dark_Shadow_Md,
    shadowLg = Dark_Shadow_Lg,
    shadowXl = Dark_Shadow_Xl,
    selection = Dark_Selection,
    ripple    = Dark_Ripple,
    hover     = Dark_Hover,
    pressed   = Dark_Pressed,
    skeletonBase      = Dark_Skeleton_Base,
    skeletonHighlight = Dark_Skeleton_Highlight,
    navIconSelected   = Dark_Nav_Icon_Selected,
    navIconInactive   = Dark_Nav_Icon_Inactive,
    navBadgeBg        = Dark_Nav_Badge_Bg,
    navBadgeFg        = Dark_Nav_Badge_Fg,
    navBorderTop      = Dark_Border_Nav,
    indicatorActive   = Dark_Indicator_Active,
    indicatorInactive = Dark_Indicator_Inactive,
    tabSelectedBg     = Dark_Tab_Selected_Bg,
    tabSelectedFg     = Dark_Tab_Selected_Fg,
    tabUnselectedFg   = Dark_Tab_Unselected_Fg,
    tabContainer      = Dark_Tab_Container,
    swatchRingSelected = Dark_Swatch_Ring_Selected,
    wishlistCardBg    = Color(0xCC232325), // rgba(35,35,37,0.80) frosted glass
    isDark            = true,
)

// ── CompositionLocal ──────────────────────────────────────────────────────────

val LocalShopzenColors = staticCompositionLocalOf { ShopzenLightColors }

// ── ShopzenTheme ──────────────────────────────────────────────────────────────

/**
 * Shopzen Material 3 theme.
 *
 * Usage:
 *   val c = LocalShopzenColors.current   // full Shopzen extended tokens
 *   MaterialTheme.colorScheme            // M3 standard slots
 *   MaterialTheme.typography             // Plus Jakarta Sans scale
 *   MaterialTheme.shapes                 // Shopzen rounded shapes
 */
@Composable
fun ShopzenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme   = if (darkTheme) ShopzenDarkColorScheme  else ShopzenLightColorScheme
    val shopzenColors = if (darkTheme) ShopzenDarkColors       else ShopzenLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalShopzenColors provides shopzenColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            shapes      = ShopzenM3Shapes,
            content     = content,
        )
    }
}

// Back-compat alias — existing call-sites compile without migration sweep
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = ShopzenTheme(darkTheme = darkTheme, content = content)