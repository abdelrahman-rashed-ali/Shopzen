package shopzen.presentation.checkout.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import shopzen.presentation.R
import shopzen.presentation.checkout.components.CheckoutTopBar
import shopzen.presentation.checkout.components.OrderSuccessContent
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    orderNumber: String,
    onNavigateBack: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(c.backgroundSecondary)
            .statusBarsPadding(),
        containerColor = c.backgroundSecondary,
        topBar = {
            CheckoutTopBar(
                title = stringResource(R.string.checkout_confirmation_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OrderSuccessContent(
                orderNumber = orderNumber.ifBlank { orderId },
                onContinueShopping = onContinueShopping,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
