package shopzen.presentation.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shopzen.presentation.theme.Palette_ErrorDark
import shopzen.presentation.theme.Palette_SuccessDark

@Composable
fun AuthMessage(
    text: String,
    error: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (error) {
        Palette_ErrorDark.copy(alpha = 0.12f)
    } else {
        Palette_SuccessDark.copy(alpha = 0.12f)
    }
    
    val textColor = if (error) {
        Palette_ErrorDark
    } else {
        Palette_SuccessDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
