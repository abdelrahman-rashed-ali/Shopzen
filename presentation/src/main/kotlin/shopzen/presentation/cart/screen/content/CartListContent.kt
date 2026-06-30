package shopzen.presentation.cart.screen.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shopzen.presentation.cart.components.AddCouponSection
import shopzen.presentation.cart.components.CartItemCard
import shopzen.presentation.cart.components.CartSummarySection
import shopzen.presentation.cart.components.CheckoutButton
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartState
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSpacing

@Composable
fun CartListContent(
    state: CartState,
    onIntent: (CartIntent) -> Unit,
) {
    val c = LocalShopzenColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = ShopzenSpacing.XL,
                vertical = ShopzenSpacing.LG,
            ),
            verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        ) {
            items(
                items = state.items,
                key = { it.id },
            ) { item ->
                AnimatedVisibility(
                    visible = true,
                    exit = shrinkVertically() + fadeOut(tween(ShopzenMotion.DurationSlow)),
                ) {
                    CartItemCard(
                        item = item,
                        onIncrement = { onIntent(CartIntent.IncrementQuantity(item.id)) },
                        onDecrement = { onIntent(CartIntent.DecrementQuantity(item.id)) },
                        onDelete = { onIntent(CartIntent.RequestRemoveItem(item.id)) },
                        onCardClick = { onIntent(CartIntent.NavigateToProduct(item.productId)) },
                    )
                }
            }
        }

        AddCouponSection(
            modifier = Modifier.padding(horizontal = ShopzenSpacing.MD),
            onAddCoupon = { onIntent(CartIntent.OpenCouponSheet) },
        )
        Spacer(Modifier.height(ShopzenSpacing.MD))

        HorizontalDivider(color = c.divider, thickness = 1.dp)
        Column(
            modifier = Modifier
                .background(c.backgroundPrimary)
                .navigationBarsPadding(),
        ) {
            CartSummarySection(
                formattedSubtotal = state.formattedSubtotal,
                formattedTotal = state.formattedTotal,
                formattedDiscount = state.formattedDiscount,
                couponApplied = state.couponApplied,
                appliedCouponLabel = state.appliedCouponLabel,
                onAddCoupon = { onIntent(CartIntent.OpenCouponSheet) },
                onRemoveCoupon = { onIntent(CartIntent.RemoveCoupon) },
                modifier = Modifier.padding(horizontal = ShopzenSpacing.XL),
            )

            CheckoutButton(
                onClick = { onIntent(CartIntent.Checkout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ShopzenSpacing.XL,
                        vertical = ShopzenSpacing.LG,
                    ),
            )
        }
    }
}
