package iti.presentation.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ShopzenColorScheme = lightColorScheme(
    primary = ShopzenBlack,
    onPrimary = ShopzenWhite,
    primaryContainer = ShopzenPrimaryContainer,
    onPrimaryContainer = ShopzenOnPrimaryContainer,
    secondary = ShopzenSecondary,
    onSecondary = ShopzenOnSecondary,
    secondaryContainer = ShopzenSecondaryContainer,
    onSecondaryContainer = ShopzenOnSecondaryContainer,
    tertiary = ShopzenTertiary,
    onTertiary = ShopzenOnTertiary,
    tertiaryContainer = ShopzenTertiaryContainer,
    onTertiaryContainer = ShopzenOnTertiaryContainer,
    error = ShopzenError,
    onError = ShopzenOnError,
    errorContainer = ShopzenErrorContainer,
    onErrorContainer = ShopzenOnErrorContainer,
    background = ShopzenSurface,
    onBackground = ShopzenOnSurface,
    surface = ShopzenSurface,
    onSurface = ShopzenOnSurface,
    surfaceVariant = ShopzenSurfaceVariant,
    onSurfaceVariant = ShopzenOnSurfaceVariant,
    outline = ShopzenOutline,
    outlineVariant = ShopzenOutlineVariant,
    inverseSurface = ShopzenInverseSurface,
    inverseOnSurface = ShopzenInverseOnSurface,
    inversePrimary = ShopzenInversePrimary,
    surfaceTint = ShopzenSurfaceTint,
    surfaceDim = ShopzenSurfaceDim,
    surfaceBright = ShopzenSurfaceBright,
    surfaceContainerLowest = ShopzenSurfaceContainerLowest,
    surfaceContainerLow = ShopzenSurfaceContainerLow,
    surfaceContainer = ShopzenSurfaceContainer,
    surfaceContainerHigh = ShopzenSurfaceContainerHigh,
    surfaceContainerHighest = ShopzenSurfaceContainerHighest
)

@Composable
fun ShopzenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShopzenColorScheme,
        typography = ShopzenTypography,
        content = content
    )
}
