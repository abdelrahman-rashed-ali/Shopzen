package shopzen.presentation.auth.components


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import shopzen.presentation.theme.LocalShopzenColors


@Composable
fun AuthMessage(
    text:String,
    error:Boolean
){

    Text(

        text=text,

        color =
            if(error)
                MaterialTheme.colorScheme.error
            else
                LocalShopzenColors.current.textSuccess

    )

}
