package shopzen.presentation.cart.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import shopzen.presentation.R
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenBody
import shopzen.presentation.common.theme.ShopzenHeading3
import shopzen.presentation.common.theme.ShopzenShapes
import shopzen.presentation.common.theme.ShopzenSpacing

@Composable
fun CartErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ShopzenSpacing.XL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.cart_error_title),
            style = ShopzenHeading3,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShopzenSpacing.SM))
        Text(
            text = message,
            style = ShopzenBody,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShopzenSpacing.XXL))
        Button(
            onClick = onRetry,
            shape = ShopzenShapes.Full,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
            ),
        ) {
            Text(text = stringResource(R.string.cart_retry), style = ShopzenBody, fontWeight = FontWeight.SemiBold)
        }
    }
}
