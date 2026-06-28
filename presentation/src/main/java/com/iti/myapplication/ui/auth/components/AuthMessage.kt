package com.shopzen.presentation.auth.components


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.iti.myapplication.ui.theme.*


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