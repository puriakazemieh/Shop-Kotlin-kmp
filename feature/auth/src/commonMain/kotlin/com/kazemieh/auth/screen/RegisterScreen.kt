package com.kazemieh.auth.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateLogin: () -> Unit
) {
    val state = viewModel.state
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {

                UiEvent.NavigateToHome -> {
//                     navController.navigate("home") {
//                         popUpTo("login") { inclusive = true }
//                     }
                }

                is UiEvent.ShowError -> {
                    event.message?.let { messageBarState.addError(it) }
                }
            }
        }
    }

    Scaffold { padding ->
        ContentWithMessageBar(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            messageBarState = messageBarState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Register", fontSize = FontSize.LARGE)

                Spacer(Modifier.height(24.dp))

                AuthTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.onEvent(AuthEvent.OnEmailChange(it))
                    },
                    hint = "Email"
                )

                state.emailError?.let {
                    Text(it, color = colors.error)
                }

                Spacer(Modifier.height(12.dp))

                AuthTextField(
                    value = state.password,
                    onValueChange = {
                        viewModel.onEvent(AuthEvent.OnPasswordChange(it))
                    },
                    hint = "Password",
                    isPassword = true
                )

                state.passwordError?.let {
                    Text(it, color = colors.error)
                }

                Spacer(Modifier.height(24.dp))

                AuthButton("Register") {
                    viewModel.onEvent(AuthEvent.SubmitRegister)
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateLogin) {
                    Text("Already have an account?")
                }
            }
        }
    }
}