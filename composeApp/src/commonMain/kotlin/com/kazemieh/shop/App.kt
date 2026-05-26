package com.kazemieh.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.admin.options.adminOptionsModule
import com.kazemieh.admin.orders.adminOrdersModule
import com.kazemieh.admin.products.adminProductsModule
import com.kazemieh.auth.authModule
import com.kazemieh.cart.cartModule
import com.kazemieh.catalog.catalogModule
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.details.di.detailsModule
import com.kazemieh.domain.di.domainModule
import com.kazemieh.domain.usecase.settings.ObserveLanguageUseCase
import com.kazemieh.domain.usecase.settings.ObserveThemeModeUseCase
import com.kazemieh.main.mainModule
import com.kazemieh.navigation.AppNavHost
import com.kazemieh.network.di.networkModule
import com.kazemieh.profile.profileModule
import com.kazemieh.settings.settingsModule
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
            domainModule,
            mainModule,
            authModule,
            profileModule,
            cartModule,
            catalogModule,
            settingsModule,
            adminProductsModule,
            adminOrdersModule,
            adminOptionsModule,
            detailsModule
        )
    }
}
