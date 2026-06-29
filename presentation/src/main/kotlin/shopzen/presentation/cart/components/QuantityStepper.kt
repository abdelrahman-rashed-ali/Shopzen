package shopzen.presentation.cart.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSize
import shopzen.presentation.theme.ShopzenTheme

/**
 * Quantity stepper: [−]  N  [+]
 *
 * Animations:
 *   - Quantity number: AnimatedContent with vertical slide (direction based on increment/decrement)
 *   - Buttons: scale 96% on press via MutableInteractionSource + graphicsLayer (deferred read)
 *
 * Design tokens:
 *   - Button bg: actionSecondaryBg (F3F3F3 / 2A2A2D)
 *   - Button icon: iconPrimary / iconDisabled
 *   - Size: QuantityButtonSize (40dp)
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    maxQuantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Decrement button
        StepperButton(
            icon = { Icon(Icons.Default.Remove, contentDescription = null) },
            enabled = quantity > 1,
            onClick = onDecrement,
            contentDesc = "Decrease quantity",
        )

        // Quantity value — slides up on increment, down on decrement
        AnimatedContent(
            targetState = quantity,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInVertically { height -> -direction * height } togetherWith
                    slideOutVertically { height -> direction * height })
            },
            label = "quantity",
        ) { qty ->
            Box(
                modifier = Modifier.size(width = 28.dp, height = ShopzenSize.QuantityButtonSize),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = qty.toString(),
                    style = ShopzenBody,
                    color = c.textPrimary,
                )
            }
        }

        // Increment button
        StepperButton(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            enabled = quantity < maxQuantity,
            onClick = onIncrement,
            contentDesc = "Increase quantity",
        )
    }
}

@Composable
private fun StepperButton(
    icon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDesc: String,
) {
    val c = LocalShopzenColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = if (enabled) c.actionSecondaryBg else c.actionDisabledBg
    val iconColor = if (enabled) c.iconPrimary else c.iconDisabled

    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(ShopzenSize.QuantityButtonSize)
            .clip(CircleShape)
            .background(bgColor)
            // Deferred scale read in graphicsLayer — does not trigger recomposition
            .graphicsLayer {
                val scale = if (isPressed) ShopzenMotion.ScaleButton else 1f
                scaleX = scale
                scaleY = scale
            }
            .semantics { contentDescription = contentDesc },
    ) {
        // Render icon with correct color
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides iconColor,
        ) {
            icon()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantityStepperPreview() {
    ShopzenTheme {
        QuantityStepper(
            quantity = 1,
            maxQuantity = 10,
            onIncrement = {},
            onDecrement = {},
        )
    }
}
