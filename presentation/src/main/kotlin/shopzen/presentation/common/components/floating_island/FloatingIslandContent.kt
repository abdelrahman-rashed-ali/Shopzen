package shopzen.presentation.common.components.floating_island

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import shopzen.presentation.common.theme.ShopzenSectionTitle
import shopzen.presentation.common.theme.ShopzenSmall
import shopzen.presentation.common.theme.ShopzenSpacing

/**
 * Internal content layout for [FloatingIslandDialog].
 *
 * Renders [illustrationContent] | title + subtitle | [actionContent]
 * in a horizontal row. No animation logic — that lives on the caller's
 * `graphicsLayer { alpha = contentAlpha }`.
 *
 * Follows modifier-and-layout-style skill:
 * - `modifier` parameter applied to root.
 * - No hardcoded fillMaxWidth — parent decides.
 *
 * Follows slot-api-pattern skill:
 * - Nullable slots with `null` default.
 * - Primitives for title/subtitle (typography locked — this is a notification).
 */
@Composable
internal fun FloatingIslandContentLayout(
    title: String,
    subtitle: String?,
    colors: FloatingIslandColors,
    modifier: Modifier = Modifier,
    illustrationContent: (@Composable () -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.padding(
            horizontal = ShopzenSpacing.LG,
            vertical = ShopzenSpacing.MD,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
    ) {
        if (illustrationContent != null) {
            illustrationContent()
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
        ) {
            Text(
                text = title,
                style = ShopzenSectionTitle,
                color = colors.titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = ShopzenSmall,
                    color = colors.subtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (actionContent != null) {
            actionContent()
        }
    }
}
