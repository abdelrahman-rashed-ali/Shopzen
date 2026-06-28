package com.shopzen.presentation.auth.screen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.shopzen.presentation.auth.components.*
import com.shopzen.presentation.auth.viewmodel.RegisterViewModel
import com.shopzen.presentation.navigation.Routes
import com.iti.myapplication.ui.theme.ColorSurface
import iti.presentation.R

@Composable
fun EmailVerificationScreen(


    navController: NavController,


    viewModel: RegisterViewModel = hiltViewModel()


){


    val state by viewModel.state.collectAsStateWithLifecycle()



    var notVerified by remember {
        mutableStateOf(false)
    }



    var resent by remember {
        mutableStateOf(false)
    }





    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface),


        contentAlignment = Alignment.Center

    ){



        AuthCard {


            AuthHeader(

                title = stringResource(
                    R.string.verify_email
                ),

                subtitle = stringResource(
                    R.string.verify_email_desc
                )


            )



            Spacer(
                Modifier.height(32.dp)
            )




            AnimatedVisibility(
                visible = resent
            ){

                AuthMessage(

                    text = stringResource(
                        R.string.email_resent
                    ),

                    error = false

                )


            }




            AnimatedVisibility(
                visible = notVerified
            ){


                AuthMessage(

                    text = stringResource(
                        R.string.email_not_verified
                    ),

                    error = true

                )


            }




            Spacer(
                Modifier.height(24.dp)
            )




            AuthButton(

                text = stringResource(
                    R.string.continue_text
                ),

                loading = false

            ){



                val user =

                    FirebaseAuth
                        .getInstance()
                        .currentUser



                user?.reload()




                if(user?.isEmailVerified == true){


                    navController.navigate(
                        Routes.HOME
                    ){

                        popUpTo(
                            Routes.SPLASH
                        ){

                            inclusive = true

                        }

                    }



                }else{


                    notVerified = true

                    resent = false


                }



            }




            Spacer(
                Modifier.height(16.dp)
            )





            OutlinedButton(

                onClick = {


                    viewModel
                        .resendVerificationEmail()



                    resent = true

                    notVerified = false



                },


                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)

            ){


                Text(

                    text = stringResource(
                        R.string.resend_email
                    )

                )


            }





            AnimatedVisibility(

                visible = state.error != null

            ){


                state.error?.let {


                    AuthMessage(

                        text = it,

                        error = true

                    )


                }


            }



        }



    }



}