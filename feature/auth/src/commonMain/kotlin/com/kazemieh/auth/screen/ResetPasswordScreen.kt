package com.kazemieh.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthBrandHeader
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.responsiveMaxWidth
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
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()

    val successMessage = stringResource(Resources.String.PasswordResetSuccess)
    val passwordsDoNotMatchMessage = stringResource(Resources.String.PasswordsDoNotMatch)

    LaunchedEffect(token) {
        viewModel.handleIntent(AuthIntent.OnTokenReceived(token))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> {
                    val message = when (effect.message) {
                        "MOBILE_ALREADY_EXISTS" -> Resources.String.MobileAlreadyExists
                        "INVALID_OTP" -> Resources.String.InvalidOtp
                        "USER_NOT_FOUND" -> Resources.String.UserNotFound
                        "PASSWORDS_DO_NOT_MATCH" -> Resources.String.PasswordsDoNotMatch
                        else -> effect.message
                    }
                    messageBarState.addError(message ?: "Unknown Error")
                }

                is AuthEffect.ShowSuccess -> {
                    messageBarState.addSuccess(successMessage)
                }

                AuthEffect.NavigateToLogin -> {
                    onNavigateLogin()
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
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthBrandHeader(
                    title = stringResource(Resources.String.ResetPassword),
                    subtitle = "رمز عبور جدیدت را انتخاب کن"
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .responsiveMaxWidth(ContentWidth.readable)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, AppTheme.colors.line, RoundedCornerShape(22.dp))
                        .padding(24.dp)
                ) {
                if (state.isOtpMode) {
                    AuthTextField(
                        value = state.mobile,
                        onValueChange = {
                            viewModel.handleIntent(AuthIntent.OnMobileChange(it))
                        },
                        hint = stringResource(Resources.String.PhoneNumberPlaceholder)
                    )

                    state.mobileError?.let {
                        Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(12.dp))

                    AuthTextField(
                        value = state.otp,
                        onValueChange = {
                            viewModel.handleIntent(AuthIntent.OnOtpChange(it))
                        },
                        hint = stringResource(Resources.String.EnterOtpCode)
                    )

                    state.otpError?.let {
                        Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(12.dp))
                }

                AuthTextField(
                    value = state.newPassword,
                    onValueChange = {
                        viewModel.handleIntent(AuthIntent.OnNewPasswordChange(it))
                    },
                    hint = stringResource(Resources.String.NewPasswordHint),
                    isPassword = true
                )

                state.newPasswordError?.let {
                    Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(12.dp))

                AuthTextField(
                    value = state.confirmPassword,
                    onValueChange = {
                        viewModel.handleIntent(AuthIntent.OnConfirmPasswordChange(it))
                    },
                    hint = stringResource(Resources.String.ConfirmPasswordHint),
                    isPassword = true
                )

                state.confirmPasswordError?.let {
                    Text(anyToString(it), color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(24.dp))

                AuthButton(stringResource(Resources.String.ResetPassword)) {
                    viewModel.handleIntent(AuthIntent.SubmitResetPassword)
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }
                }
            }
        }
    }
}
