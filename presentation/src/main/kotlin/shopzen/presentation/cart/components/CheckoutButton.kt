package shopzen.presentation.cart.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenElevation
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSize

@Composable
fun CheckoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = onClick,
        modifier = modifier
            .height(ShopzenSize.ButtonHeight)
            .shadow(
                elevation = ShopzenElevation.MD,
                shape = ShopzenShapes.Full,
                ambientColor = c.shadowMd,
                spotColor = c.shadowMd,
            )
            .graphicsLayer {
                val scale = if (isPressed) ShopzenMotion.ScaleButton else 1f
                scaleX = scale
                scaleY = scale
            },
        interactionSource = interactionSource,
        shape = ShopzenShapes.Full,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.actionPrimaryBg,
            contentColor = c.actionPrimaryFg,
        ),
    ) {
        Text(
            text = stringResource(R.string.cart_checkout),
            style = ShopzenBody,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
