package shopzen.presentation.cart.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import shopzen.presentation.R
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenElevation
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSectionTitle
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

@Composable
fun CartItemCard(
    item: CartItemUi,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current

    val cardInteraction = remember { MutableInteractionSource() }
    val cardPressed by cardInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (cardPressed) ShopzenMotion.ScaleCard else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ShopzenElevation.SM,
                shape = ShopzenShapes.XL,
                ambientColor = c.shadowSm,
                spotColor = c.shadowSm,
            )
            .clip(ShopzenShapes.XL)
            .background(c.surfaceCard)
            .clickable(
                interactionSource = cardInteraction,
                indication = null,
                onClick = onCardClick,
            )
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductImageWell(imageUrl = item.imageUrl)

            Spacer(Modifier.width(ShopzenSpacing.MD))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.title,
                        style = ShopzenSectionTitle,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    DeleteButton(onClick = onDelete)
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = item.variantTitle,
                    style = ShopzenSmall,
                    color = c.textSecondary,
                )

                Spacer(Modifier.height(ShopzenSpacing.SM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.formattedPrice,
                        style = ShopzenBody,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                    )
                    QuantityStepper(
                        quantity = item.quantity,
                        maxQuantity = item.maxQuantity,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductImageWell(imageUrl: String) {
    val c = LocalShopzenColors.current
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(ShopzenShapes.MD)
            .background(c.imageWell),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imageUrl.ifBlank { null },
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(64.dp)
                .clip(ShopzenShapes.SM),
        )
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val c = LocalShopzenColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    IconButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer {
                val scale = if (pressed) ShopzenMotion.ScaleButton else 1f
                scaleX = scale
                scaleY = scale
            },
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.cart_remove_item_cd),
            tint = c.iconDestructive,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CartItemCardPreview() {
    ShopzenTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CartItemCard(
                item = CartItemUi(
                    id = "1",
                    productId = "p1",
                    variantId = "v1",
                    title = "Woman Sweater",
                    variantTitle = "Woman Fashion",
                    formattedPrice = "$70.00",
                    quantity = 1,
                    maxQuantity = 10,
                    imageUrl = "",
                ),
                onIncrement = {},
                onDecrement = {},
                onDelete = {},
                onCardClick = {},
            )
        }
    }
}