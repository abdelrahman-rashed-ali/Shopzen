package com.shopzen.presentation.auth.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.iti.myapplication.ui.theme.*


@Composable
fun PasswordInputField(
    value: String,
    label: String,
    onChange: (String) -> Unit
){
    var visible by remember {
        mutableStateOf(false)
    }
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = ColorOnSurfaceVariant
            )
        },
        placeholder = {
            Text(
                text = "••••••••",
                color = ColorOutlineVariant
            )
        },
        textStyle = LocalTextStyle.current.copy(
            color = ColorOnSurface
        ),
        visualTransformation =
            if(visible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(
                onClick = {
                    visible = !visible
                }
            ){
                Icon(
                    imageVector =
                        if(visible)
                            Icons.Outlined.VisibilityOff
                        else
                            Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = ColorOnSurfaceVariant
                )
            }
        },



        colors = TextFieldDefaults.colors(
            focusedContainerColor =
                ColorSurfaceContainerLow,
            unfocusedContainerColor =
                ColorSurfaceContainerLow,
            disabledContainerColor =
                ColorSurfaceContainerLow,
            focusedTextColor =
                ColorOnSurface,
            unfocusedTextColor =
                ColorOnSurface,
            cursorColor =
                ColorOnSurface,

            focusedIndicatorColor =
                ColorOnSurface,
            unfocusedIndicatorColor =
                Color.Transparent

        )

    )

}