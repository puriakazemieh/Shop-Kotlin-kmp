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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.auth.component.AuthButton
import com.kazemieh.auth.component.AuthTextField
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onBack: () -> Unit
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
        Text("Forgot Password", fontSize = FontSize.LARGE)

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

        Spacer(Modifier.height(24.dp))

        AuthButton("Send Reset Link") {
            viewModel.onEvent(AuthEvent.SubmitForgotPassword)
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back to Login")
        }
    }
}