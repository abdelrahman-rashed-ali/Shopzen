package shopzen.presentation.auth.screen


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.auth.components.*
import shopzen.presentation.auth.intent.RegisterIntent
import shopzen.presentation.auth.viewmodel.RegisterViewModel


@Composable
fun RegisterScreen(

    navigateToEmailVerification: () -> Unit,

    navigateToLogin: () -> Unit,

    viewModel: RegisterViewModel = hiltViewModel()

) {


    val state by viewModel.state.collectAsStateWithLifecycle()



    LaunchedEffect(state.isRegistered) {

        if (state.isRegistered) {

            navigateToEmailVerification()

        }

    }



    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),


        horizontalAlignment = Alignment.CenterHorizontally

    ) {


        Spacer(
            Modifier.height(32.dp)
        )



        Text(

            text = stringResource(
                R.string.app_logo
            ),

            fontSize = 20.sp,

            fontWeight = FontWeight.SemiBold,

            color = MaterialTheme.colorScheme.onSurface

        )



        Spacer(
            Modifier.height(32.dp)
        )



        AuthCard {


            AuthHeader(

                title = stringResource(
                    R.string.create_account
                ),

                subtitle = stringResource(
                    R.string.create_account_desc
                )

            )



            Spacer(
                Modifier.height(32.dp)
            )



            AuthTextField(

                value = state.name,

                label = stringResource(
                    R.string.full_name
                ),

                hint = stringResource(
                    R.string.name_hint
                ),

                onValueChange = {

                    viewModel.processIntent(

                        RegisterIntent.NameChanged(it)

                    )

                }

            )



            Spacer(
                Modifier.height(16.dp)
            )



            AuthTextField(

                value = state.email,

                label = stringResource(
                    R.string.email_address
                ),

                hint = stringResource(
                    R.string.email_hint
                ),

                onValueChange = {

                    viewModel.processIntent(

                        RegisterIntent.EmailChanged(it)

                    )

                }

            )



            Spacer(
                Modifier.height(16.dp)
            )



            PasswordInputField(

                value = state.password,

                label = stringResource(
                    R.string.password
                ),

                onChange = {

                    viewModel.processIntent(

                        RegisterIntent.PasswordChanged(it)

                    )

                }

            )



            Spacer(
                Modifier.height(16.dp)
            )



            PasswordInputField(

                value = state.confirmPassword,

                label = stringResource(
                    R.string.confirm_password
                ),

                onChange = {

                    viewModel.processIntent(

                        RegisterIntent.ConfirmPasswordChanged(it)

                    )

                }

            )



            Spacer(
                Modifier.height(16.dp)
            )



            AnimatedVisibility(

                visible = state.error != null

            ) {


                state.error?.let {


                    AuthMessage(

                        text = it,

                        error = true

                    )

                }

            }



            Spacer(
                Modifier.height(32.dp)
            )



            AuthButton(

                text = stringResource(
                    R.string.sign_up
                ),

                loading = state.isLoading

            ) {


                viewModel.processIntent(

                    RegisterIntent.Submit

                )

            }



            Spacer(
                Modifier.height(24.dp)
            )



            Row(

                verticalAlignment = Alignment.CenterVertically

            ) {


                Text(

                    text = stringResource(
                        R.string.already_have_account
                    )

                )



                TextButton(

                    onClick = {

                        navigateToLogin()

                    }

                ) {


                    Text(

                        text = stringResource(
                            R.string.login
                        )

                    )

                }

            }

        }

    }

}
