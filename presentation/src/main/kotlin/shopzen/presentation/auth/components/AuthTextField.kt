package shopzen.presentation.auth.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun AuthTextField(

    value: String,

    label: String,

    hint: String,

    onValueChange: (String) -> Unit

){


    TextField(

        value = value,


        onValueChange = onValueChange,


        modifier = Modifier.fillMaxWidth(),



        label = {

            Text(

                text = label,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant

            )

        },



        placeholder = {

            Text(

                text = hint,

                color =
                    MaterialTheme.colorScheme.outlineVariant

            )

        },



        textStyle = LocalTextStyle.current.copy(

            color =
                MaterialTheme.colorScheme.onSurface

        ),



        singleLine = true,



        colors = TextFieldDefaults.colors(


            focusedContainerColor =

                MaterialTheme.colorScheme.surfaceContainerLow,


            unfocusedContainerColor =
                MaterialTheme.colorScheme.surfaceContainerLow,


            disabledContainerColor =
                MaterialTheme.colorScheme.surfaceContainerLow,


            focusedTextColor =
                MaterialTheme.colorScheme.onSurface,


            unfocusedTextColor =
                MaterialTheme.colorScheme.onSurface,


            cursorColor =
                MaterialTheme.colorScheme.onSurface,


            focusedIndicatorColor =
                MaterialTheme.colorScheme.onSurface,


            unfocusedIndicatorColor =
                Color.Transparent,


            disabledIndicatorColor =
                Color.Transparent

        )

    )

}
