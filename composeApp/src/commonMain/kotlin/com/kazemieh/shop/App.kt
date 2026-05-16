package com.kazemieh.shop

import androidx.compose.runtime.Composable
import com.kazemieh.admin_panel.adminPanelModule
import com.kazemieh.auth.authModule
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.home.homeModule
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
        printLogger()
        config?.invoke(this)
        modules(
            platformModule(),
            networkModule,
            dataModule,
            homeModule,
            authModule,
            profileModule,
            adminPanelModule
        )
    }
}