package com.kazemieh.shop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.admin.options.adminOptionsModule
import com.kazemieh.admin.orders.adminOrdersModule
import com.kazemieh.admin.products.adminProductsModule
import com.kazemieh.admin.wallet.adminWalletModule
import com.kazemieh.admin.blog.adminBlogModule
import com.kazemieh.admin.academy.adminAcademyModule
import com.kazemieh.admin.clinic.adminClinicModule
import com.kazemieh.admin.psychtest.adminPsychTestModule
import com.kazemieh.psychtest.psychTestModule
import com.kazemieh.comparison.comparisonModule
import com.kazemieh.auth.authModule
import com.kazemieh.blog.blogModule
import com.kazemieh.cart.cartModule
import com.kazemieh.catalog.catalogModule
import com.kazemieh.common.AppLanguage
import com.kazemieh.common.AppThemeMode
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.brand.BrandConfig
import com.kazemieh.designsystem.brand.BrandRegistry
import com.kazemieh.network.common.ApiConfig
import com.kazemieh.details.di.detailsModule
import com.kazemieh.domain.settings.ObserveLanguageUseCase
import com.kazemieh.domain.settings.ObserveThemeModeUseCase
import com.kazemieh.main.mainModule
import com.kazemieh.navigation.AppNavHost
import com.kazemieh.network.di.networkModule
import com.kazemieh.orders.di.ordersModule
import com.kazemieh.profile.profileModule
import com.kazemieh.settings.settingsModule
import com.kazemieh.support.supportModule
import com.kazemieh.academy.academyModule
import com.kazemieh.clinic.clinicModule
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
    val themeMode by observeThemeModeUseCase().collectAsState(AppThemeMode.LIGHT)

    val brand = koinInject<BrandConfig>()

    AppTheme(
        themeMode = themeMode,
        language = language,
        brandColors = brand.colors
    ) {
        AppNavHost()
    }
}

fun initKoin(brand: BrandConfig = BrandRegistry.default, config: KoinAppDeclaration? = null) {
    // اگر برند BASE_URL اختصاصی داشته باشد، شبکه از آن استفاده می‌کند.
    ApiConfig.baseUrlOverride = brand.apiBaseUrl
    startKoin {
        printLogger()
        config?.invoke(this)
        modules(
            org.koin.dsl.module { single { brand } },
            platformModule(),
            networkModule,
            dataModule,
            mainModule,
            authModule,
            profileModule,
            cartModule,
            catalogModule,
            settingsModule,
            adminProductsModule,
            adminOrdersModule,
            adminOptionsModule,
            adminWalletModule,
            adminBlogModule,
            adminAcademyModule,
            adminClinicModule,
            adminPsychTestModule,
            psychTestModule,
            comparisonModule,
            blogModule,
            detailsModule,
            ordersModule,
            supportModule,
            academyModule,
            clinicModule
        )
    }
}
