package com.kazemieh.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.admin_panel.adminPanelModule
import com.kazemieh.auth.authModule
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.details.di.detailsModule
import com.kazemieh.domain.usecase.settings.ObserveLanguageUseCase
import com.kazemieh.domain.usecase.settings.ObserveThemeModeUseCase
import com.kazemieh.home.homeModule
import com.kazemieh.navigation.AppNavHost
import com.kazemieh.network.di.networkModule
import com.kazemieh.profile.profileModule
import org.jetbrains.compose.resources.InternalResourceApi
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

@OptIn(InternalResourceApi::class)
@Composable
fun App() {
    val observeLanguageUseCase = koinInject<ObserveLanguageUseCase>()
    val observeThemeModeUseCase = koinInject<ObserveThemeModeUseCase>()

    val language by observeLanguageUseCase().collectAsState(AppLanguage.ENGLISH)
    val themeMode by observeThemeModeUseCase().collectAsState(AppThemeMode.SYSTEM)

    AppTheme(
        themeMode = themeMode,
        language = language
    ) {
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
            adminPanelModule,
            detailsModule
        )
    }
}