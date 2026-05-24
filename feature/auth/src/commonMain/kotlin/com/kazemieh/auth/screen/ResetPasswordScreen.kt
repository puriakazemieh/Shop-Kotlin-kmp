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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordScreen(
    token: String,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateLogin: () -> Unit
) {
    val state = viewModel.state
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    val successMessage = stringResource(Resources.String.PasswordResetSuccess)
    val passwordsDoNotMatchMessage = stringResource(Resources.String.PasswordsDoNotMatch)

    LaunchedEffect(token) {
        viewModel.onEvent(AuthEvent.OnTokenReceived(token))
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    if (event.message == "PASSWORDS_DO_NOT_MATCH") {
                        messageBarState.addError(passwordsDoNotMatchMessage)
                    } else {
                        messageBarState.addError(event.message ?: "Unknown Error")
                    }
                }

                is UiEvent.ShowSuccess -> {
                    messageBarState.addSuccess(successMessage)
                }

                UiEvent.NavigateToLogin -> {
                    onNavigateLogin()
                }

                else -> {}
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
                Text(stringResource(Resources.String.ResetPassword), fontSize = FontSize.LARGE)

                Spacer(Modifier.height(24.dp))

                AuthTextField(
                    value = state.newPassword,
                    onValueChange = {
                        viewModel.onEvent(AuthEvent.OnNewPasswordChange(it))
                    },
                    hint = stringResource(Resources.String.NewPasswordHint),
                    isPassword = true
                )

                state.newPasswordError?.let {
                    Text(anyToString(it), color = colors.error)
                }

                Spacer(Modifier.height(12.dp))

                AuthTextField(
                    value = state.confirmPassword,
                    onValueChange = {
                        viewModel.onEvent(AuthEvent.OnConfirmPasswordChange(it))
                    },
                    hint = stringResource(Resources.String.ConfirmPasswordHint),
                    isPassword = true
                )

                state.confirmPasswordError?.let {
                    Text(anyToString(it), color = colors.error)
                }

                Spacer(Modifier.height(24.dp))

                AuthButton(stringResource(Resources.String.ResetPassword)) {
                    viewModel.onEvent(AuthEvent.SubmitResetPassword)
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
