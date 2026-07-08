package shopzen.presentation.cart.screen.content

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shopzen.presentation.R
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenBody
import shopzen.presentation.common.theme.ShopzenHeading3
import shopzen.presentation.common.theme.ShopzenShapes
import shopzen.presentation.common.theme.ShopzenSpacing
import shopzen.presentation.common.theme.ShopzenTheme

@Composable
fun CartEmptyState(
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "emptyStateBreath")
    val illustrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "illustrationScale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ShopzenSpacing.XL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    scaleX = illustrationScale
                    scaleY = illustrationScale
                }
                .drawBehind {
                    drawEmptyCartIllustration(
                        bagStrokeColor = if (c.isDark) Color(0xFF38383C) else Color(0xFFE7E7E7),
                        bagFillColor = if (c.isDark) Color(0xFF232325) else Color(0xFFF8F8F8),
                        accentColor = if (c.isDark) Color(0xFF48484C) else Color(0xFFD1D1D6),
                        handleColor = if (c.isDark) Color(0xFF48484C) else Color(0xFFB7B7B7),
                    )
                },
        )

        Spacer(Modifier.height(ShopzenSpacing.XXL))

        Text(
            text = stringResource(R.string.cart_empty_title),
            style = ShopzenHeading3,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ShopzenSpacing.SM))

        Text(
            text = stringResource(R.string.cart_empty_desc),
            style = ShopzenBody,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ShopzenSpacing.XXXL))

        Button(
            onClick = onStartShopping,
            shape = ShopzenShapes.Full,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
            ),
            modifier = Modifier
                .padding(horizontal = ShopzenSpacing.XXXXL)
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.cart_start_shopping),
                style = ShopzenBody,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun DrawScope.drawEmptyCartIllustration(
    bagStrokeColor: Color,
    bagFillColor: Color,
    accentColor: Color,
    handleColor: Color,
) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val bodyLeft = w * 0.15f
    val bodyTop = h * 0.38f
    val bodyRight = w * 0.85f
    val bodyBottom = h * 0.85f
    val cornerR = 24f

    val bodyPath = Path().apply {
        moveTo(bodyLeft + cornerR, bodyTop)
        lineTo(bodyRight - cornerR, bodyTop)
        quadraticTo(bodyRight, bodyTop, bodyRight, bodyTop + cornerR)
        lineTo(bodyRight, bodyBottom - cornerR)
        quadraticTo(bodyRight, bodyBottom, bodyRight - cornerR, bodyBottom)
        lineTo(bodyLeft + cornerR, bodyBottom)
        quadraticTo(bodyLeft, bodyBottom, bodyLeft, bodyBottom - cornerR)
        lineTo(bodyLeft, bodyTop + cornerR)
        quadraticTo(bodyLeft, bodyTop, bodyLeft + cornerR, bodyTop)
        close()
    }

    drawPath(bodyPath, color = bagFillColor)
    drawPath(bodyPath, color = bagStrokeColor, style = stroke)

    val handleLeft = w * 0.35f
    val handleRight = w * 0.65f
    val handleTop = h * 0.14f
    val handleMid = h * 0.38f

    val handlePath = Path().apply {
        moveTo(handleLeft, handleMid)
        cubicTo(
            handleLeft, handleTop,
            handleRight, handleTop,
            handleRight, handleMid,
        )
    }
    drawPath(handlePath, color = handleColor, style = stroke)

    val foldY = h * 0.55f
    drawLine(
        color = accentColor,
        start = Offset(bodyLeft + 20f, foldY),
        end = Offset(bodyRight - 20f, foldY),
        strokeWidth = 4f,
        cap = StrokeCap.Round,
    )

    drawCircle(color = accentColor, radius = 5f, center = Offset(w * 0.40f, foldY + h * 0.10f))
    drawCircle(color = accentColor, radius = 5f, center = Offset(w * 0.60f, foldY + h * 0.10f))
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun CartEmptyStatePreview() {
    ShopzenTheme {
        CartEmptyState(onStartShopping = {})
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
private fun CartEmptyStateDarkPreview() {
    ShopzenTheme(darkTheme = true) {
        CartEmptyState(onStartShopping = {})
    }
}