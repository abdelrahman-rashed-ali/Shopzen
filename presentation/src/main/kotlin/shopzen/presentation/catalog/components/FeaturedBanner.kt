package shopzen.presentation.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Tall, premium hero banner matching high-fashion e-commerce design.
 * Features a dark, elegant overlay with structured serif typography and a solid square CTA button.
 */
@Composable
fun FeaturedBanner(
    bannerImages: List<String>,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp) // Tall, dramatic hero view height
    ) {
        val imageUrl = bannerImages.firstOrNull()
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Featured collection banner image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High resolution luxury gold watch banner fallback
            AsyncImage(
                model = "https://images.unsplash.com/photo-1619134778706-7015533a6150?q=80&w=1200",
                contentDescription = "Fallback banner image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Elegant dark gradient overlay for copy contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 300f
                    )
                )
        )

        // Contents positioned near the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                ),
                color = Color.White,
                textAlign = TextAlign.Start
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    lineHeight = 22.sp
                ),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )

            // Flat square explore button
            Button(
                onClick = { /* Explore action */ },
                shape = CutCornerShape(0.dp), // Square edges
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "EXPLORE COLLECTION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
