package com.shopzen.presentation.auth.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.iti.myapplication.ui.theme.*


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

                color = ColorOnSurfaceVariant

            )

        },



        placeholder = {

            Text(

                text = hint,

                color = ColorOutlineVariant

            )

        },



        textStyle = LocalTextStyle.current.copy(

            color = ColorOnSurface

        ),



        singleLine = true,



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
                Color.Transparent,


            disabledIndicatorColor =
                Color.Transparent

        )

    )

}