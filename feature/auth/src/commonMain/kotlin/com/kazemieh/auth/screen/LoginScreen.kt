package com.kazemieh.auth.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
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
    val state = viewModel.state
    val colors = AppTheme.colors
    val emailHint = stringResource(Resources.String.EmailHint)

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateBack -> onNavigateBack()
                is UiEvent.NavigateToHome -> {} // Handle if needed
                is UiEvent.NavigateToLogin -> {}
                is UiEvent.ShowError -> {}
                is UiEvent.ShowSuccess -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Resources.String.Login), fontSize = FontSize.LARGE)

        Spacer(Modifier.height(24.dp))

        AuthTextField(
            value = state.email,
            onValueChange = {
                viewModel.onEvent(AuthEvent.OnEmailChange(it))
            },
            hint = emailHint
        )

        state.emailError?.let {
            Text(anyToString(it), color = colors.error)
        }

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = state.password,
            onValueChange = {
                viewModel.onEvent(AuthEvent.OnPasswordChange(it))
            },
            hint = stringResource(Resources.String.PasswordHint),
            isPassword = true
        )

        state.passwordError?.let {
            Text(anyToString(it), color = colors.error)
        }

        Spacer(Modifier.height(24.dp))

        AuthButton(stringResource(Resources.String.Login)) {
            viewModel.onEvent(AuthEvent.SubmitLogin)
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