package shopzen.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val DarkColorScheme = darkColorScheme(
    primary = ColorOnPrimary,
    secondary = ColorOutlineVariant,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = ColorError

)


private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,

    onPrimary = ColorOnPrimary,


    background = ColorSurface,

    surface = ColorSurface,


    onSurface = ColorOnSurface,


    secondary = ColorOnSurfaceVariant,


    outline = ColorOutlineVariant,


    error = ColorError

)



@Composable
fun MyApplicationTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),

    content: @Composable () -> Unit

) {


    val colorScheme = if (darkTheme) {

        DarkColorScheme

    } else {

        LightColorScheme

    }



    MaterialTheme(

        colorScheme = colorScheme,

        typography = AppTypography,

        content = content

    )

}
