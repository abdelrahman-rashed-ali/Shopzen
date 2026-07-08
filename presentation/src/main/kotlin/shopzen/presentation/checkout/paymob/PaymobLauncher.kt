package shopzen.presentation.checkout.paymob

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.paymob.paymob_sdk.PaymobSdk
import com.paymob.paymob_sdk.ui.PaymobSdkListener
import shopzen.presentation.R
import shopzen.presentation.common.theme.Dark_Background_Primary
import shopzen.presentation.common.theme.Dark_Text_Primary

fun launchPaymobSdk(
    context: Context,
    clientSecret: String,
    publicKey: String,
    paymobSdkListener: PaymobSdkListener,
) {
    PaymobSdk.Builder(
        context = context,
        clientSecret = clientSecret,
        publicKey = publicKey,
        paymobSdkListener = paymobSdkListener,
    )
        .showSaveCard(false)
        .setButtonBackgroundColor(Dark_Background_Primary.value.toInt())
        .setButtonTextColor(Dark_Text_Primary.value.toInt())
        .setAppName("Shopzen")
        .build().start()
}
