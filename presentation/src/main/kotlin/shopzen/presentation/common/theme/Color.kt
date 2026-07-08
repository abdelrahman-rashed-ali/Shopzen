package shopzen.presentation.common.theme

import androidx.compose.ui.graphics.Color

// ─── Raw Palette — DESIGN.md §2 ─────────────────────────────────────────────
// Dark grayscale: warm-tinted near-blacks, ~6-8% luminance steps
val Gray1000 = Color(0xFF0A0A0B) // OLED background
val Gray950  = Color(0xFF111113) // dark primary bg
val Gray900  = Color(0xFF161618) // dark secondary bg / nav
val Gray850  = Color(0xFF1C1C1F) // dark tertiary bg / section
val Gray800  = Color(0xFF232325) // dark card surface
val Gray750  = Color(0xFF2A2A2D) // dark elevated card / dialog
val Gray700  = Color(0xFF313134) // dark input bg
val Gray650  = Color(0xFF38383C) // dark border / hover
val Gray600  = Color(0xFF48484C) // dark disabled icon
val Gray500  = Color(0xFF636366) // dark disabled text / placeholder
val Gray400  = Color(0xFF8E8E93) // dark tertiary text / secondary icon
val Gray300  = Color(0xFFAEAEB2) // dark secondary text
val Gray100  = Color(0xFFEBEBF0) // dark primary text — NOT #FFFFFF (AAA ~15:1)
val Gray50   = Color(0xFFF2F2F7) // inverse text

// Semantic accent
val Palette_Success     = Color(0xFF34C759)
val Palette_SuccessDark = Color(0xFF34D058)
val Palette_Error       = Color(0xFFFF3B30)
val Palette_ErrorDark   = Color(0xFFFF6B6B)
val Palette_Warning     = Color(0xFFFF9500)
val Palette_WarningDark = Color(0xFFFFB347)
val Palette_Info        = Color(0xFF007AFF)
val Palette_InfoDark    = Color(0xFF6B9FFF)

// ─── Light Semantic Tokens — DESIGN.md §3 ────────────────────────────────────
val Light_Background_Primary   = Color(0xFFFFFFFF)
val Light_Background_Secondary = Color(0xFFF8F8F8)
val Light_Background_Tertiary  = Color(0xFFF3F3F3)

val Light_Surface_Card        = Color(0xFFFFFFFF)
val Light_Surface_Elevated    = Color(0xFFFFFFFF)
val Light_Surface_Dialog      = Color(0xFFFFFFFF)
val Light_Surface_Tooltip     = Color(0xFFFFFFFF)
val Light_Surface_Input       = Color(0xFFF8F8F8)
val Light_Surface_Navigation  = Color(0xFFFFFFFF)
val Light_Surface_BottomNav   = Color(0xFFFFFFFF)
val Light_Image_Well          = Color(0xFFF8F8F8)
val Light_Image_Placeholder   = Color(0xFFF3F3F3)

val Light_Overlay      = Color(0x66000000) // rgba(0,0,0,0.40)
val Light_Overlay_Light = Color(0x33000000) // rgba(0,0,0,0.20)

val Light_Text_Primary     = Color(0xFF111111)
val Light_Text_Secondary   = Color(0xFF6E6E73)
val Light_Text_Tertiary    = Color(0xFF8E8E93)
val Light_Text_Disabled    = Color(0xFFB4B4B8)
val Light_Text_Placeholder = Color(0xFFB7B7B7)
val Light_Text_Hint        = Color(0xFFB7B7B7)
val Light_Text_Inverse     = Color(0xFFFFFFFF)
val Light_Text_Error       = Palette_Error
val Light_Text_Success     = Palette_Success
val Light_Text_Warning     = Palette_Warning
val Light_Text_Link        = Palette_Info

val Light_Icon_Primary         = Color(0xFF222222)
val Light_Icon_Secondary       = Color(0xFF6E6E73)
val Light_Icon_Inactive        = Color(0xFFB4B4B8)
val Light_Icon_Disabled        = Color(0xFFD1D1D6)
val Light_Icon_Destructive     = Palette_Error
val Light_Icon_Notification    = Palette_Error
val Light_Icon_Rating          = Palette_Warning
val Light_Icon_Wishlist_Empty  = Color(0xFFB4B4B8)
val Light_Icon_Wishlist_Filled = Palette_Error

val Light_Action_Primary_Bg      = Color(0xFF000000)
val Light_Action_Primary_Fg      = Color(0xFFFFFFFF)
val Light_Action_Secondary_Bg    = Color(0xFFF3F3F3)
val Light_Action_Secondary_Fg    = Color(0xFF111111)
val Light_Action_Outlined_Border = Color(0xFF000000)
val Light_Action_Outlined_Fg     = Color(0xFF111111)
val Light_Action_Ghost_Fg        = Color(0xFF111111)
val Light_Action_Disabled_Bg     = Color(0xFFF3F3F3)
val Light_Action_Disabled_Fg     = Color(0xFFB4B4B8)
val Light_Action_Danger_Bg       = Color(0xFFFFF0F0)
val Light_Action_Danger_Fg       = Palette_Error
val Light_Action_Danger_Border   = Color(0x40FF3B30)  // rgba(255,59,48,0.25)
val Light_Action_Success_Bg      = Color(0xFFF0FFF4)
val Light_Action_Success_Fg      = Palette_Success
val Light_Action_Success_Border  = Color(0x4034C759)  // rgba(52,199,89,0.25)

val Light_Status_Success = Palette_Success
val Light_Status_Warning = Palette_Warning
val Light_Status_Error   = Palette_Error
val Light_Status_Info    = Palette_Info

val Light_Border_Default  = Color(0xFFE7E7E7)
val Light_Border_Subtle   = Color(0xFFEEEEEE)
val Light_Border_Focus    = Color(0xFF000000)
val Light_Border_Error    = Palette_Error
val Light_Border_Success  = Palette_Success
val Light_Border_Selected = Color(0xFF000000)

val Light_Divider        = Color(0xFFEEEEEE)
val Light_Divider_Strong = Color(0xFFE7E7E7)

val Light_Shadow_Sm = Color(0x14000000) // rgba(0,0,0,0.08) — card shadow
val Light_Shadow_Md = Color(0x1A000000) // rgba(0,0,0,0.10)
val Light_Shadow_Lg = Color(0x1F000000) // rgba(0,0,0,0.12)
val Light_Shadow_Xl = Color(0x29000000) // rgba(0,0,0,0.16)

val Light_Selection = Color(0x14000000) // rgba(0,0,0,0.08)
val Light_Focus     = Color(0x1F000000) // rgba(0,0,0,0.12)
val Light_Ripple    = Color(0x0F000000) // rgba(0,0,0,0.06)
val Light_Hover     = Color(0x0A000000) // rgba(0,0,0,0.04)
val Light_Pressed   = Color(0x14000000) // rgba(0,0,0,0.08)

val Light_Skeleton_Base      = Color(0xFFF3F3F3)
val Light_Skeleton_Highlight = Color(0xFFEBEBEB)

val Light_Nav_Icon_Selected  = Color(0xFF000000)
val Light_Nav_Icon_Inactive  = Color(0x4D000000) // rgba(0,0,0,0.30)
val Light_Nav_Badge_Bg       = Palette_Error
val Light_Nav_Badge_Fg       = Color(0xFFFFFFFF)

val Light_Indicator_Active   = Color(0xFF000000)
val Light_Indicator_Inactive = Color(0xFFD1D1D6)

val Light_Tab_Selected_Bg   = Color(0xFF000000)
val Light_Tab_Selected_Fg   = Color(0xFFFFFFFF)
val Light_Tab_Unselected_Fg = Color(0xFF6E6E73)
val Light_Tab_Container     = Color(0xFFF3F3F3)

val Light_Swatch_Ring_Selected = Color(0xFF000000)

// ─── Dark Semantic Tokens — DESIGN.md §3 ─────────────────────────────────────
val Dark_Background_Primary   = Gray950   // #111113 — never #000000 (non-OLED)
val Dark_Background_Secondary = Gray900   // #161618
val Dark_Background_Tertiary  = Gray850   // #1C1C1F
val Dark_Background_Oled      = Gray1000  // #0A0A0B

// 5-layer elevation system via luminance (no borders on cards)
val Dark_Surface_Card        = Gray800    // #232325
val Dark_Surface_Elevated    = Gray750    // #2A2A2D
val Dark_Surface_Dialog      = Gray750    // #2A2A2D
val Dark_Surface_Tooltip     = Gray650    // #38383C
val Dark_Surface_Input       = Gray700    // #313134
val Dark_Surface_Navigation  = Gray900    // #161618
val Dark_Surface_BottomNav   = Gray900    // #161618
val Dark_Image_Well          = Gray850    // #1C1C1F
val Dark_Image_Placeholder   = Gray800    // #232325

val Dark_Overlay       = Color(0x99000000) // rgba(0,0,0,0.60)
val Dark_Overlay_Light = Color(0x4D000000) // rgba(0,0,0,0.30)

// Primary text = #EBEBF0, NOT #FFFFFF — reduces eye strain, ~15:1 contrast AAA
val Dark_Text_Primary     = Gray100          // #EBEBF0
val Dark_Text_Secondary   = Gray300          // #AEAEB2
val Dark_Text_Tertiary    = Gray400          // #8E8E93
val Dark_Text_Disabled    = Gray500          // #636366
val Dark_Text_Placeholder = Gray500          // #636366
val Dark_Text_Hint        = Gray400          // #8E8E93
val Dark_Text_Inverse     = Gray950          // #111113
val Dark_Text_Error       = Palette_ErrorDark
val Dark_Text_Success     = Palette_SuccessDark
val Dark_Text_Warning     = Palette_WarningDark
val Dark_Text_Link        = Palette_InfoDark

val Dark_Icon_Primary         = Gray100          // #EBEBF0
val Dark_Icon_Secondary       = Gray400          // #8E8E93
val Dark_Icon_Inactive        = Gray500          // #636366
val Dark_Icon_Disabled        = Gray600          // #48484C
val Dark_Icon_Destructive     = Palette_ErrorDark
val Dark_Icon_Notification    = Palette_ErrorDark
val Dark_Icon_Rating          = Palette_WarningDark
val Dark_Icon_Wishlist_Empty  = Gray300          // #AEAEB2
val Dark_Icon_Wishlist_Filled = Palette_ErrorDark

// Dark primary = WHITE — single beacon on dark canvas. One per screen.
val Dark_Action_Primary_Bg       = Color(0xFFFFFFFF)
val Dark_Action_Primary_Fg       = Gray950           // #111113
val Dark_Action_Secondary_Bg     = Gray750           // #2A2A2D
val Dark_Action_Secondary_Fg     = Gray100           // #EBEBF0
val Dark_Action_Secondary_Border = Gray600           // #48484C
val Dark_Action_Outlined_Border  = Gray100
val Dark_Action_Outlined_Fg      = Gray100
val Dark_Action_Ghost_Fg         = Gray100
val Dark_Action_Disabled_Bg      = Gray800           // #232325
val Dark_Action_Disabled_Fg      = Gray500           // #636366 — explicit, NOT alpha(0.38)
val Dark_Action_Danger_Bg        = Color(0x1FFF6B6B) // rgba(255,107,107,0.12)
val Dark_Action_Danger_Fg        = Palette_ErrorDark
val Dark_Action_Danger_Border    = Color(0x40FF6B6B) // rgba(255,107,107,0.25)
val Dark_Action_Success_Bg       = Color(0x1F34D058) // rgba(52,208,88,0.12)
val Dark_Action_Success_Fg       = Palette_SuccessDark
val Dark_Action_Success_Border   = Color(0x4034D058) // rgba(52,208,88,0.25)

val Dark_Status_Success = Palette_SuccessDark
val Dark_Status_Warning = Palette_WarningDark
val Dark_Status_Error   = Palette_ErrorDark
val Dark_Status_Info    = Palette_InfoDark

val Dark_Border_Default  = Gray650           // #38383C
val Dark_Border_Subtle   = Gray750           // #2A2A2D
val Dark_Border_Focus    = Gray100           // #EBEBF0
val Dark_Border_Error    = Palette_ErrorDark
val Dark_Border_Success  = Palette_SuccessDark
val Dark_Border_Selected = Gray100
val Dark_Border_Nav      = Color(0x14EBEBF0) // rgba(235,235,240,0.08) hairline on bottom nav

val Dark_Divider        = Gray750  // #2A2A2D
val Dark_Divider_Strong = Gray700  // #313134

val Dark_Shadow_Sm = Color(0x40000000) // rgba(0,0,0,0.25)
val Dark_Shadow_Md = Color(0x59000000) // rgba(0,0,0,0.35)
val Dark_Shadow_Lg = Color(0x80000000) // rgba(0,0,0,0.50)
val Dark_Shadow_Xl = Color(0x99000000) // rgba(0,0,0,0.60)
val Dark_Ambient_Glow = Color(0x0FFFFFFF) // inset top-edge glow at elevation 5

val Dark_Selection = Color(0x26EBEBF0) // rgba(235,235,240,0.15)
val Dark_Focus     = Color(0x33EBEBF0) // rgba(235,235,240,0.20)
val Dark_Ripple    = Color(0x1AEBEBF0) // rgba(235,235,240,0.10)
val Dark_Hover     = Color(0x0FEBEBF0) // rgba(235,235,240,0.06)
val Dark_Pressed   = Color(0x1FEBEBF0) // rgba(235,235,240,0.12)

val Dark_Skeleton_Base      = Gray800  // #232325
val Dark_Skeleton_Highlight = Gray750  // #2A2A2D

val Dark_Nav_Icon_Selected  = Gray100          // #EBEBF0
val Dark_Nav_Icon_Inactive  = Gray500          // #636366
val Dark_Nav_Badge_Bg       = Palette_ErrorDark
val Dark_Nav_Badge_Fg       = Color(0xFFFFFFFF)

val Dark_Indicator_Active   = Gray100  // #EBEBF0
val Dark_Indicator_Inactive = Gray600  // #48484C

val Dark_Tab_Selected_Bg   = Color(0xFFFFFFFF)
val Dark_Tab_Selected_Fg   = Gray950            // #111113
val Dark_Tab_Unselected_Fg = Gray400            // #8E8E93
val Dark_Tab_Container     = Gray850            // #1C1C1F

val Dark_Swatch_Ring_Selected = Gray100         // #EBEBF0