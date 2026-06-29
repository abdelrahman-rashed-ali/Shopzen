package shopzen.presentation.auth.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color


@Composable
fun AuthCard(
    content:@Composable ColumnScope.()->Unit
){

    Surface(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(8.dp),

        color = Color.White,

        shadowElevation = 4.dp

    ){

        Column(
            modifier = Modifier.padding(32.dp),
            content = content
        )

    }

}
