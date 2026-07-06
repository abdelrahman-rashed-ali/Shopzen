package shopzen.presentation.cart.screen.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun CartLoadingContent() {
    val c = LocalShopzenColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = c.actionPrimaryBg,
            strokeCap = StrokeCap.Round,
        )
    }
}
