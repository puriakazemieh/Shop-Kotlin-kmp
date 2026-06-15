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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateRegister: () -> Unit,
    onNavigateForgot: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    
    val emailHint = stringResource(Resources.String.EmailHint)
    val mobileHint = stringResource(Resources.String.PhoneNumberPlaceholder)
    val otpHint = stringResource(Resources.String.EnterOtpCode)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateBack -> onNavigateBack()
                is AuthEffect.NavigateToHome -> {} // Handle if needed
                is AuthEffect.NavigateToLogin -> {}
                is AuthEffect.ShowError -> {
                    val message = when (effect.message) {
                        "MOBILE_ALREADY_EXISTS" -> Resources.String.MobileAlreadyExists
                        "INVALID_OTP" -> Resources.String.InvalidOtp
                        "USER_NOT_FOUND" -> Resources.String.UserNotFound
                        else -> effect.message
                    }
                    messageBarState.addError(message ?: "Error")
                }
                is AuthEffect.ShowSuccess -> {
                    if (effect.message == "OTP_SENT") {
                        messageBarState.addSuccess(Resources.String.OtpSent)
                    }
                }
            }
        }
    }

    ContentWithMessageBar(
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
            Text(stringResource(Resources.String.Login), fontSize = FontSize.LARGE)

            Spacer(Modifier.height(24.dp))

            if (state.isOtpMode) {
                if (!state.otpSent) {
                    AuthTextField(
                        value = state.mobile,
                        onValueChange = {
                            viewModel.handleIntent(AuthIntent.OnMobileChange(it))
                        },
                        hint = mobileHint
                    )

                    state.mobileError?.let {
                        Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text(
                        text = stringResource(Resources.String.OtpSent) + " " + state.mobile,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = state.otp,
                        onValueChange = {
                            viewModel.handleIntent(AuthIntent.OnOtpChange(it))
                        },
                        hint = otpHint
                    )

                    state.otpError?.let {
                        Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                    }

                    if (state.resendTimer > 0) {
                        Text(
                            text = state.resendTimer.toString(),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        TextButton(onClick = { viewModel.handleIntent(AuthIntent.ResendOtp) }) {
                            Text(stringResource(Resources.String.ResendOtp))
                        }
                    }
                }
            } else {
                AuthTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.handleIntent(AuthIntent.OnEmailChange(it))
                    },
                    hint = emailHint
                )

                state.emailError?.let {
                    Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))

                AuthTextField(
                    value = state.password,
                    onValueChange = {
                        viewModel.handleIntent(AuthIntent.OnPasswordChange(it))
                    },
                    hint = stringResource(Resources.String.PasswordHint),
                    isPassword = true
                )

                state.passwordError?.let {
                    Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(24.dp))

            AuthButton(
                if (state.isOtpMode && !state.otpSent) stringResource(Resources.String.SendResetLink)
                else stringResource(Resources.String.Login)
            ) {
                viewModel.handleIntent(AuthIntent.SubmitLogin)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { viewModel.handleIntent(AuthIntent.ToggleAuthMode) }) {
                Text(
                    if (state.isOtpMode) stringResource(Resources.String.LoginWithPassword)
                    else stringResource(Resources.String.LoginWithOtp)
                )
            }

            if (state.isLoading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateForgot) {
                Text(stringResource(Resources.String.ForgotPassword))
            }

            TextButton(onClick = onNavigateRegister) {
                Text(stringResource(Resources.String.CreateAccount))
            }
        }
    }
}
