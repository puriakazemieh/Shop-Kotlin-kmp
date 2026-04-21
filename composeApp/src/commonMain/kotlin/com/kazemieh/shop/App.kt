package com.kazemieh.shop

import androidx.compose.runtime.Composable
import com.kazemieh.auth.screen.LoginScreen
import com.kazemieh.designsystem.AppTheme

@Composable
fun App() {
    AppTheme {
         LoginScreen(
            onLogin = { _, _ -> },
            onNavigateRegister = {  },
            onNavigateForgot = {  }
        )
    }
}