package com.kazemieh.shop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.kazemieh.designsystem.ProvideWindowSizeClass
import com.kazemieh.designsystem.brand.BrandConfig
import com.kazemieh.designsystem.brand.BrandRegistry
import com.kazemieh.network.common.ApiConfig
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.BackendProfile
import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.BootstrapProfiles
import com.kazemieh.config.capabilities.EndpointResolver
import com.kazemieh.config.capabilities.FeatureManifestBootstrapCoordinator
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.FeatureFlagShadowMode
import com.kazemieh.config.capabilities.FeatureFlagShadowReporter
import com.kazemieh.config.capabilities.InMemoryLastKnownGoodManifestCache
import com.kazemieh.config.capabilities.KtorRemoteManifestTransport
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.RemoteFeatureManifestClient
import com.kazemieh.config.capabilities.RemoteManifestTransport
import com.kazemieh.config.capabilities.TenantConfig
import com.kazemieh.navigation.FeatureRouteGuard
import com.kazemieh.config.capabilities.ProfileAssetUrlResolver
import com.kazemieh.config.capabilities.ProfileEndpointResolver
import com.kazemieh.config.capabilities.privateSessionNamespace
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
import kotlinx.coroutines.launch
import kotlin.time.Clock

@OptIn(InternalResourceApi::class)
@Composable
fun App() {
    val observeLanguageUseCase = koinInject<ObserveLanguageUseCase>()
    val observeThemeModeUseCase = koinInject<ObserveThemeModeUseCase>()

    val language by observeLanguageUseCase().collectAsState(AppLanguage.PERSIAN)
    val themeMode by observeThemeModeUseCase().collectAsState(AppThemeMode.LIGHT)

    val brand = koinInject<BrandConfig>()
    val bootstrapCoordinator = koinInject<FeatureManifestBootstrapCoordinator>()
    val routeGuard = koinInject<FeatureRouteGuard>()
    var bootstrapState by remember { mutableStateOf<ManifestBootstrapState>(bootstrapCoordinator.state) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bootstrapCoordinator) {
        bootstrapState = bootstrapCoordinator.load()
    }

    AppTheme(
        themeMode = themeMode,
        language = language,
        brandColors = brand.colors
    ) {
        ProvideWindowSizeClass {
            when (val state = bootstrapState) {
                ManifestBootstrapState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is ManifestBootstrapState.Ready -> AppNavHost(routeGuard = routeGuard)
                is ManifestBootstrapState.Error -> {
                    Box(Modifier.fillMaxSize()) {
                        AppNavHost(routeGuard = routeGuard)
                        Column(Modifier.align(Alignment.TopCenter).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("پیکربندی راه‌دور در دسترس نیست؛ حالت امن فعال است.", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { scope.launch { bootstrapState = bootstrapCoordinator.retry() } }) {
                                Text("تلاش دوباره")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun initKoin(brand: BrandConfig = BrandRegistry.default, config: KoinAppDeclaration? = null) {
    // backend فقط یک dimension دوحالته است؛ tenant و branding مستقل می‌مانند.
    ApiConfig.baseUrlOverride = brand.apiBaseUrl
    val backendKind = if (brand.id.equals("wp", ignoreCase = true)) BackendKind.WORDPRESS else BackendKind.SPRING
    val tenantConfig = TenantConfig("local-default")
    val backendProfile: BackendProfile = BootstrapProfiles.forBackend(backendKind, ApiConfig.baseUrl)
    startKoin {
        printLogger()
        config?.invoke(this)
        modules(
            org.koin.dsl.module {
                single { brand }
                single { backendProfile }
                single { tenantConfig }
                single { get<BackendProfile>().privateSessionNamespace(get<TenantConfig>().id) }
                single<EndpointResolver> { ProfileEndpointResolver(get()) }
                single<AssetUrlResolver> { ProfileAssetUrlResolver(get()) }
                single<RemoteManifestTransport> { KtorRemoteManifestTransport(get()) }
                single { InMemoryLastKnownGoodManifestCache() }
                single {
                    RemoteFeatureManifestClient(
                        profile = get(),
                        expectedTenantId = get<TenantConfig>().id,
                        transport = get()
                    )
                }
                single<FeatureFlagShadowReporter> { FeatureFlagShadowReporter { } }
                single { FeatureFlagShadowMode(get()) }
                single {
                    GeneratedLocalFeatureManifest.sourceFor(get<BackendProfile>(), get<TenantConfig>()).resolveFor(get<BackendProfile>().kind)
                }
                single { FeatureUseCaseGuard(get()) }
                single { FeatureRouteGuard(get()) }
                single {
                    FeatureManifestBootstrapCoordinator(
                        localFeatures = get(),
                        remoteClient = get(),
                        cache = get(),
                        namespace = get(),
                        nowEpochMillis = { Clock.System.now().toEpochMilliseconds() },
                        shadowMode = get()
                    )
                }
            },
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
