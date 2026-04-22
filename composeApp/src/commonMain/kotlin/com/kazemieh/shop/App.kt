package com.kazemieh.shop

import androidx.compose.runtime.Composable
import com.kazemieh.auth.authModule
import com.kazemieh.auth.screen.RegisterScreen
import com.kazemieh.designsystem.AppTheme
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.startKoin

@Composable
fun App() {
    initKoin()
    AppTheme {
        RegisterScreen(
            onNavigateLogin = { }
        )
    }
}

fun initKoin() {
    startKoin {
        modules(authModule)
    }
}