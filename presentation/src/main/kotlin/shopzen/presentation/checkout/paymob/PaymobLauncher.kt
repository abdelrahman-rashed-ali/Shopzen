package shopzen.presentation.checkout.paymob

import android.content.Context
import com.paymob.paymob_sdk.PaymobSdk
import com.paymob.paymob_sdk.ui.PaymobSdkListener

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
    ).build().start()
}
