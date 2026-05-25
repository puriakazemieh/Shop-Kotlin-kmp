package com.kazemieh.auth.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.util.anyToString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state = viewModel.state
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    val emailSentMessage = stringResource(Resources.String.PasswordResetEmailSent)

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    messageBarState.addError(event.message ?: "Unknown Error")
                }

                is UiEvent.ShowSuccess -> {
                    messageBarState.addSuccess(emailSentMessage)
                }

                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            messageBarState = messageBarState,
            contentBackgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(Resources.String.ForgotPassword), fontSize = FontSize.LARGE)

                Spacer(Modifier.height(24.dp))

                AuthTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.onEvent(AuthEvent.OnEmailChange(it))
                    },
                    hint = stringResource(Resources.String.EmailHint)
                )

                state.emailError?.let {
                    Text(anyToString(it), color = colors.error)
                }

                Spacer(Modifier.height(24.dp))

                AuthButton(stringResource(Resources.String.SendResetLink)) {
                    viewModel.onEvent(AuthEvent.SubmitForgotPassword)
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onBack) {
                    Text(stringResource(Resources.String.BackToLogin))
                }
            }
        }
    }
}