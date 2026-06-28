package com.shopzen.presentation.auth.components


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.iti.myapplication.ui.theme.*


@Composable
fun AuthButton(
    text:String,
    loading:Boolean,
    onClick:()->Unit
){

    Button(

        onClick = onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),


        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimary
        )

    ){


        if(loading){

            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ColorOnPrimary
            )

        }else{

            Text(text)

        }


    }

}