package shopzen.presentation.auth.components


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun AuthHeader(
    title:String,
    subtitle:String
){

    Text(
        text = title,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    )


    Text(
        text = subtitle,
        fontSize = 14.sp
    )

}
