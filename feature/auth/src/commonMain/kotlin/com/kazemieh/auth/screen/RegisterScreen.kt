package com.kazemieh.auth.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthBrandHeader
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
fun RegisterScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                AuthEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is AuthEffect.ShowError -> {
                    val message = when (effect.message) {
                        "MOBILE_ALREADY_EXISTS" -> Resources.String.MobileAlreadyExists
                        "INVALID_OTP" -> Resources.String.InvalidOtp
                        "USER_NOT_FOUND" -> Resources.String.UserNotFound
                        else -> effect.message
                    }
                    messageBarState.addError(message ?: "Error")
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
                    title = stringResource(Resources.String.CreateAccount),
                    subtitle = "به جامعه‌ی مد و استایل کارمیلا بپیوندید"
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, AppTheme.colors.line, RoundedCornerShape(22.dp))
                        .padding(24.dp)
                ) {
                AuthTextField(
                    value = if (state.isOtpMode) state.mobile else state.email,
                    onValueChange = {
                        if (state.isOtpMode) viewModel.handleIntent(AuthIntent.OnMobileChange(it))
                        else viewModel.handleIntent(AuthIntent.OnEmailChange(it))
                    },
                    hint = if (state.isOtpMode) stringResource(Resources.String.PhoneNumberPlaceholder)
                    else stringResource(Resources.String.EmailHint)
                )

                val currentError = if (state.isOtpMode) state.mobileError else state.emailError
                currentError?.let {
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

                Spacer(Modifier.height(24.dp))

                AuthButton(stringResource(Resources.String.CreateAccount)) {
                    viewModel.handleIntent(AuthIntent.SubmitRegister)
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { viewModel.handleIntent(AuthIntent.ToggleAuthMode) }) {
                    Text(
                        if (state.isOtpMode) stringResource(Resources.String.LoginWithPassword)
                        else stringResource(Resources.String.LoginWithOtp)
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateLogin) {
                    Text(stringResource(Resources.String.AlreadyHaveAccount))
                }
                }
            }
        }
    }
}
