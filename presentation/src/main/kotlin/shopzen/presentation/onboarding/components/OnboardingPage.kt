package shopzen.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image

import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    imageRes: Int,
    pageOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "floating")
        val floatingOffset by infiniteTransition.animateFloat(
            initialValue = -24f,
            targetValue = 24f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatingOffset"
        )

        // Hero Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 32.dp, bottom = 48.dp)
                .clip(RoundedCornerShape(18.dp))
                .graphicsLayer {
                    val absOffset = pageOffset.absoluteValue
                    alpha = 1f - (absOffset * 0.4f)
                    scaleX = 1f - (absOffset * 0.1f)
                    scaleY = 1f - (absOffset * 0.1f)
                    translationX = pageOffset * size.width * 0.2f
                    translationY = floatingOffset
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != 0) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }
        }

        // Text Section
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .graphicsLayer {
                    alpha = 1f - pageOffset.absoluteValue
                    translationY = pageOffset.absoluteValue * 40f
                }
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .graphicsLayer {
                    alpha = 1f - pageOffset.absoluteValue
                    translationY = pageOffset.absoluteValue * 60f
                }
        )
    }
}
