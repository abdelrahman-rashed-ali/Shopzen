package shopzen.presentation.cart.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import shopzen.presentation.R
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenSectionTitle
import shopzen.presentation.common.theme.ShopzenSize
import shopzen.presentation.common.theme.ShopzenSpacing

@Composable
fun CartTopBar(
    hasItems: Boolean,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ShopzenSize.TopBarHeight)
            .background(c.surfaceNavigation)
            .padding(horizontal = ShopzenSpacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cart_back),
                tint = c.iconPrimary,
            )
        }

        Text(
            text = stringResource(R.string.cart_title),
            style = ShopzenSectionTitle,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier.size(ShopzenSize.TopBarHeight),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = hasItems,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                IconButton(onClick = onClearCart) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.cart_clear_cart_cd),
                        tint = c.iconDestructive,
                    )
                }
            }
        }
    }
}
