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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateRegister: () -> Unit,
    onNavigateForgot: () -> Unit
) {
    val state = viewModel.state
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", fontSize = FontSize.LARGE)

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

        AuthButton("Login") {
            viewModel.onEvent(AuthEvent.SubmitLogin)
        }

        if (state.isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        state.errorMessage?.let {
            Text(it, color = colors.error)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateForgot) {
            Text("Forgot Password?")
        }

        TextButton(onClick = onNavigateRegister) {
            Text("Create Account")
        }
    }
}