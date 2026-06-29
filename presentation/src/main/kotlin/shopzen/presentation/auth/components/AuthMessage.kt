package shopzen.presentation.auth.components


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import shopzen.presentation.theme.ColorError
import shopzen.presentation.theme.ColorSuccess


@Composable
fun AuthMessage(
    text:String,
    error:Boolean
){

    Text(

        text=text,

        color =
            if(error)
                ColorError
            else
                ColorSuccess

    )

}
