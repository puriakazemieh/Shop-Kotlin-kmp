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
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Resources.String.Login), fontSize = FontSize.LARGE)

        Spacer(Modifier.height(24.dp))

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

        Spacer(Modifier.height(24.dp))

        AuthButton(stringResource(Resources.String.Login)) {
            viewModel.handleIntent(AuthIntent.SubmitLogin)
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