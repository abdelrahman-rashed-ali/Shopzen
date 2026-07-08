package shopzen.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import shopzen.presentation.R

// ─────────────────────────────────────────────────────────────────────────────
//  TYPOGRAPHY — Shopzen Design System v1.0
//  Source of truth: DESIGN.md §5
//  Font: Plus Jakarta Sans (preferred) → Inter → SF Pro
// ─────────────────────────────────────────────────────────────────────────────

private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val PlusJakartaSans = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = PlusJakartaSans, fontProvider = GoogleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSans, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSans, fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSans, fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
)

// ─────────────────────────────────────────────────────────────────────────────
//  TYPE SCALE  — DESIGN.md §5
//
//  Style          Size   Weight
//  Display         32    Bold 700
//  Heading 1       28    Bold 700
//  Heading 2       24    Bold 700
//  Heading 3       20    SemiBold 600
//  ProductTitle    18    SemiBold 600
//  SectionTitle    16    SemiBold 600
//  Body            15    Regular 400
//  Small           13    Regular 400
//  Caption         12    Regular 400
//  Price           20    Bold 700
//  Badge           11    Medium 500
// ─────────────────────────────────────────────────────────────────────────────

/** Display — 32sp Bold */
val ShopzenDisplay = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp,
)

/** Heading 1 — 28sp Bold */
val ShopzenHeading1 = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.3).sp,
)

/** Heading 2 — 24sp Bold */
val ShopzenHeading2 = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.2).sp,
)

/** Heading 3 — 20sp SemiBold */
val ShopzenHeading3 = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
)

/** Product title — 18sp SemiBold */
val ShopzenProductTitle = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp,
)

/** Section title — 16sp SemiBold */
val ShopzenSectionTitle = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
)

/** Body — 15sp Regular */
val ShopzenBody = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
)

/** Small — 13sp Regular */
val ShopzenSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
)

/** Caption — 12sp Regular */
val ShopzenCaption = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
)

/** Price — 20sp Bold (always bolder than adjacent description) */
val ShopzenPrice = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
)

/** Badge — 11sp Medium */
val ShopzenBadge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.3.sp,
)

// ─────────────────────────────────────────────────────────────────────────────
//  MATERIAL 3 TYPOGRAPHY MAPPING
//  Maps Shopzen tokens → M3 Typography slots used by MaterialTheme.
// ─────────────────────────────────────────────────────────────────────────────

val AppTypography = Typography(
    // Display / Hero
    displayLarge  = ShopzenDisplay,
    displayMedium = ShopzenHeading1,
    displaySmall  = ShopzenHeading2,

    // Headings
    headlineLarge  = ShopzenHeading1,
    headlineMedium = ShopzenHeading2,
    headlineSmall  = ShopzenHeading3,

    // Titles
    titleLarge  = ShopzenHeading3,
    titleMedium = ShopzenProductTitle,
    titleSmall  = ShopzenSectionTitle,

    // Body
    bodyLarge  = ShopzenBody,
    bodyMedium = ShopzenSmall,
    bodySmall  = ShopzenCaption,

    // Labels / badges
    labelLarge  = ShopzenSectionTitle,
    labelMedium = ShopzenSmall,
    labelSmall  = ShopzenBadge,
)
