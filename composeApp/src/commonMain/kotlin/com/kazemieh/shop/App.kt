package com.kazemieh.shop

import androidx.compose.runtime.Composable
import com.kazemieh.auth.authModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.navigation.AppNavHost
import com.kazemieh.network.di.networkModule
import com.kazemieh.profile.profileModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@Composable
fun App() {
    AppTheme {
        AppNavHost()
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            dataModule,
            networkModule,
            authModule,
            profileModule,
            platformModule()
        )
    }
}