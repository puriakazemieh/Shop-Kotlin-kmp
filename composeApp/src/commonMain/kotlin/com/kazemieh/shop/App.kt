package com.kazemieh.shop

import androidx.compose.runtime.Composable
import com.kazemieh.auth.authModule
import com.kazemieh.data.dataModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.navigation.AppNavHost
import com.kazemieh.network.di.networkModule
import org.koin.core.context.startKoin

@Composable
fun App() {
    initKoin()
    AppTheme {
        AppNavHost()
    }
}

fun initKoin() {
    startKoin {
        modules(
            authModule,
            networkModule,
            dataModule,
        )
    }
}