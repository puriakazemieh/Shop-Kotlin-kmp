package com.kazemieh.auth.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun RegisterScreen(
    onRegister: (String, String) -> Unit,
    onNavigateLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", fontSize = FontSize.LARGE)

        Spacer(Modifier.height(24.dp))

        AuthTextField(email, { email = it }, "Email")
        Spacer(Modifier.height(12.dp))
        AuthTextField(password, { password = it }, "Password", true)

        Spacer(Modifier.height(24.dp))

        AuthButton("Register") {
            onRegister(email, password)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateLogin) {
            Text("Already have an account?")
        }
    }
}