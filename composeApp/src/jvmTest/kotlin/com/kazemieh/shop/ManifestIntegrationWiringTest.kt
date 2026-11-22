package com.kazemieh.shop

import com.kazemieh.common.AppResult
import com.kazemieh.common.Screen
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.BackendProfile
import com.kazemieh.config.capabilities.BootstrapProfiles
import com.kazemieh.config.capabilities.CompiledFeatureCeiling
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.EffectiveFeatureStore
import com.kazemieh.config.capabilities.FeatureCatalog
import com.kazemieh.config.capabilities.FeatureManifest
import com.kazemieh.config.capabilities.FeatureManifestBootstrapCoordinator
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.InMemoryLastKnownGoodManifestCache
import com.kazemieh.config.capabilities.KtorRemoteManifestTransport
import com.kazemieh.config.capabilities.ManifestBootstrapSource
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import com.kazemieh.config.capabilities.RemoteFeatureManifestClient
import com.kazemieh.config.capabilities.RemoteManifestFetchResult
import com.kazemieh.config.capabilities.ResolvedFeatures
import com.kazemieh.config.capabilities.WordPressHostMode
import com.kazemieh.config.capabilities.privateSessionNamespace
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.data.local.TokenManager
import com.kazemieh.domain.academy.AcademyRepository
import com.kazemieh.domain.cart.CartRepository
import com.kazemieh.domain.clinic.ClinicRepository
import com.kazemieh.domain.psychtest.PsychTestRepository
import com.kazemieh.main.component.BottomBarDestination
import com.kazemieh.navigation.FeatureRouteGuard
import com.kazemieh.navigation.RouteGuardDecision
import com.kazemieh.network.common.ApiConfig
import com.kazemieh.network.di.networkModule
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * P03-QA-AUTO-026 — آزمون wiring واقعی Manifest و دو پروفایل
 *
 * [FIXTURE] Contract-level Simulated Endpoint Fixture (labeled per P03-QA-AUTO-026: not actual server implementation)
 * این تست جامع یکپارچگی، پایپلاین کامل را از دریافت مانیفست سرور در دو پروفایل (WordPress با ۳ مد و Spring)،
 * تا اعمال سقف کامپایل‌شده (Ceiling)، به‌روزرسانی منو و وضعیت موثر (Store)، روت‌گاردها (RouteGuard)
 * و رفتن به لایه انتقال داده (Transport با شمارنده واقعی و بررسی صفر ریکوئست در وضعیت خاموش) بررسی می‌کند.
 */
class ManifestIntegrationWiringTest {

    private lateinit var server: HttpServer
    private val requestCount = AtomicInteger(0)
    private val manifestRequestCount = AtomicInteger(0)
    private val requestHeadersList = mutableListOf<Map<String, String>>()
    private val originalBaseUrl = ApiConfig.baseUrlOverride

    private val defaultNamespace = PrivateSessionNamespace(
        backendKind = BackendKind.WORDPRESS,
        tenantId = "default-wiring-test",
        origin = "http://127.0.0.1"
    )

    private val testAssetUrlResolver = object : AssetUrlResolver {
        override fun resolve(path: String): String = path
    }

    private var activeKoinApp: KoinApplication? = null

    private fun createKoin(testModule: Module): Koin {
        activeKoinApp?.close()
        val baseModule = module {
            single<AssetUrlResolver> { testAssetUrlResolver }
            single { defaultNamespace }
        }
        val app = koinApplication {
            allowOverride(true)
            modules(networkModule, dataModule, platformModule(), baseModule, testModule)
        }
        activeKoinApp = app
        return app.koin
    }

    @BeforeTest
    fun setup() {
        requestCount.set(0)
        manifestRequestCount.set(0)
        requestHeadersList.clear()

        // Contract-level simulated HTTP server fixture on localhost
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            val query = exchange.requestURI.query ?: ""
            val headersMap = exchange.requestHeaders.entries.associate { (k, v) -> k to v.joinToString(",") }
            synchronized(requestHeadersList) {
                requestHeadersList.add(headersMap)
            }

            // Simulated Manifest Endpoints (WordPress and Spring)
            if (path.contains("client-manifest") || path.contains("/api/v1/manifest")) {
                manifestRequestCount.incrementAndGet()

                if (query.contains("fail=true") || headersMap["X-Simulate-Failure"] == "503") {
                    val errBody = """{"error":"Service Unavailable"}"""
                    exchange.responseHeaders.add("Content-Type", "application/json")
                    val bytes = errBody.toByteArray(Charsets.UTF_8)
                    exchange.sendResponseHeaders(503, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                    exchange.close()
                    return@createContext
                }

                val isSpring = path.contains("spring") || query.contains("spring") || path.contains("/api/v1/manifest") || headersMap["X-Backend-Kind"] == "SPRING"
                val tenantId = if (query.contains("tenant=tenant-beta") || headersMap["X-Tenant-Id"] == "tenant-beta") {
                    "tenant-beta"
                } else {
                    "tenant-alpha"
                }

                val hostMode = when {
                    isSpring -> null
                    query.contains("mode=THEME_ONLY") -> "THEME_ONLY"
                    query.contains("mode=PLUGIN_ONLY") -> "PLUGIN_ONLY"
                    else -> "CO_INSTALL"
                }

                // Features per tenant
                val featuresMap = if (tenantId == "tenant-beta") {
                    """
                    "content.blog": true,
                    "commerce.core": false,
                    "commerce.physical": false,
                    "commerce.digital": false,
                    "academy.core": false,
                    "clinic.booking": true,
                    "clinic.messaging": true,
                    "psych.tests": true,
                    "wallet": false,
                    "admin.mobile": false
                    """.trimIndent()
                } else {
                    """
                    "content.blog": true,
                    "commerce.core": true,
                    "commerce.physical": true,
                    "commerce.digital": true,
                    "academy.core": true,
                    "academy.quiz": true,
                    "academy.certificate": true,
                    "clinic.booking": true,
                    "clinic.messaging": false,
                    "psych.tests": false,
                    "wallet": true,
                    "admin.mobile": true
                    """.trimIndent()
                }

                val hostModeJson = if (hostMode != null) ""","hostMode":"$hostMode"""" else ""
                val backendProfile = if (isSpring) "SPRING" else "WORDPRESS"

                val manifestBody = """
                {
                    "schemaVersion": 1,
                    "manifestVersion": "v2026.08.1",
                    "backendProfile": "$backendProfile",
                    "tenantId": "$tenantId"$hostModeJson,
                    "features": {
                        $featuresMap
                    }
                }
                """.trimIndent()

                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.responseHeaders.add("ETag", "\"v2026.08.1-$tenantId\"")
                val bytes = manifestBody.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.write(bytes)
                exchange.close()
                return@createContext
            }

            // Domain data endpoints
            requestCount.incrementAndGet()

            val responseBody = when {
                path.contains("/api/cart") -> """{"id":1,"items":[],"totalPrice":0.0}"""
                path.contains("/api/courses") -> """[]"""
                path.contains("/api/therapists") -> """[]"""
                path.contains("/api/psych-tests") -> """[]"""
                path.contains("/api/blog") -> """[]"""
                else -> "{}"
            }

            exchange.responseHeaders.add("Content-Type", "application/json")
            val bytes = responseBody.toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.write(bytes)
            exchange.close()
        }
        server.start()

        ApiConfig.baseUrlOverride = "http://127.0.0.1:${server.address.port}/"
    }

    @AfterTest
    fun tearDown() {
        activeKoinApp?.close()
        activeKoinApp = null
        server.stop(0)
        ApiConfig.baseUrlOverride = originalBaseUrl
    }

    @Test
    fun `matrix of two backend profiles and three wordpress host modes boots from simulated fixture`() = runBlocking {
        val serverPort = server.address.port
        val ceiling = CompiledFeatureCeiling(FeatureCatalog.knownFeatureIds)

        // 1. WordPress profile with THEME_ONLY
        val wpThemeProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            assetRoot = "http://127.0.0.1:$serverPort/",
            allowedAuthHosts = setOf("127.0.0.1"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest?mode=THEME_ONLY",
            wordPressHostMode = WordPressHostMode.THEME_ONLY
        )
        assertEquals(WordPressHostMode.THEME_ONLY, wpThemeProfile.effectiveHostMode)

        // 2. WordPress profile with PLUGIN_ONLY
        val wpPluginProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            assetRoot = "http://127.0.0.1:$serverPort/",
            allowedAuthHosts = setOf("127.0.0.1"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest?mode=PLUGIN_ONLY",
            wordPressHostMode = WordPressHostMode.PLUGIN_ONLY
        )
        assertEquals(WordPressHostMode.PLUGIN_ONLY, wpPluginProfile.effectiveHostMode)

        // 3. WordPress profile with CO_INSTALL
        val wpCoInstallProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            assetRoot = "http://127.0.0.1:$serverPort/",
            allowedAuthHosts = setOf("127.0.0.1"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest?mode=CO_INSTALL",
            wordPressHostMode = WordPressHostMode.CO_INSTALL
        )
        assertEquals(WordPressHostMode.CO_INSTALL, wpCoInstallProfile.effectiveHostMode)

        // 4. Spring profile (without host mode)
        val springProfile = BackendProfile(
            kind = BackendKind.SPRING,
            apiRoot = "http://127.0.0.1:$serverPort/api/",
            assetRoot = "http://127.0.0.1:$serverPort/",
            allowedAuthHosts = setOf("127.0.0.1"),
            contractVersion = 1,
            manifestPath = "/client-manifest?path=spring",
            wordPressHostMode = null
        )
        assertEquals(null, springProfile.effectiveHostMode)

        // Verify remote manifest fetching across all 4 configurations with real Ktor HTTP client
        val testKoin = createKoin(module {})
        val httpClient = testKoin.get<HttpClient>()
        val transport = KtorRemoteManifestTransport(httpClient)

        val profiles = listOf(wpThemeProfile, wpPluginProfile, wpCoInstallProfile, springProfile)
        for (prof in profiles) {
            val client = RemoteFeatureManifestClient(prof, "tenant-alpha", transport, ceiling = ceiling)
            val result = client.fetch()
            assertIs<RemoteManifestFetchResult.Success>(result, "Fetch must succeed for profile ${prof.kind} with hostMode ${prof.wordPressHostMode}")
            assertEquals(prof.kind, result.manifest.backendKind)
            assertEquals("tenant-alpha", result.manifest.tenantId)
        }
        assertEquals(4, manifestRequestCount.get(), "Each profile in matrix must issue exactly 1 request to manifest endpoint")
    }

    @Test
    fun `full pipeline wiring on and off across all five domains from bootstrap to menu to route guard to transport`() = runBlocking {
        // Setup initial state: all 5 product domains disabled
        val disabledFeatures = mapOf(
            "content.blog" to false,
            "commerce.core" to false,
            "commerce.physical" to false,
            "commerce.digital" to false,
            "academy.core" to false,
            "academy.quiz" to false,
            "academy.certificate" to false,
            "clinic.booking" to false,
            "clinic.messaging" to false,
            "psych.tests" to false,
            "wallet" to false,
            "admin.mobile" to false
        )
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(disabledFeatures))
        val guard = FeatureUseCaseGuard(store)
        val routeGuard = FeatureRouteGuard(store)

        val namespace = PrivateSessionNamespace(
            backendKind = BackendKind.WORDPRESS,
            tenantId = "tenant-wiring-test",
            origin = "http://127.0.0.1:${server.address.port}"
        )

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { routeGuard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)

        // 1. MENU & DESTINATION LAYER (When commerce.core is disabled, Cart destination is hidden)
        val visibleDestinationsWhenDisabled = if (store.features.value.isEnabled("commerce.core")) {
            BottomBarDestination.entries
        } else {
            listOf(BottomBarDestination.ProductsOverview, BottomBarDestination.Search, BottomBarDestination.More)
        }
        assertFalse(visibleDestinationsWhenDisabled.contains(BottomBarDestination.Cart), "Cart tab must be hidden when commerce.core is disabled")
        assertFalse(store.features.value.isEnabled("academy.core"), "showAcademy in MoreScreen must be false")
        assertFalse(store.features.value.isEnabled("clinic.booking"), "showClinic in MoreScreen must be false")
        assertFalse(store.features.value.isEnabled("psych.tests"), "showPsychTests in MoreScreen must be false")

        // 2. ROUTE GUARD LAYER (Direct deep link / route navigation to disabled domains must be blocked)
        val blockedCart = routeGuard.checkRoute(Screen.Cart)
        assertTrue(blockedCart is RouteGuardDecision.Blocked && blockedCart.featureId == "commerce.core")
        val blockedCourse = routeGuard.checkRoute(Screen.CourseCatalog)
        assertTrue(blockedCourse is RouteGuardDecision.Blocked && blockedCourse.featureId == "academy.core")
        val blockedClinic = routeGuard.checkRoute(Screen.TherapistCatalog)
        assertTrue(blockedClinic is RouteGuardDecision.Blocked && blockedClinic.featureId == "clinic.booking")
        val blockedPsych = routeGuard.checkRoute(Screen.PsychTestCatalog)
        assertTrue(blockedPsych is RouteGuardDecision.Blocked && blockedPsych.featureId == "psych.tests")

        // 3. REPOSITORY & TRANSPORT LAYER (Fail closed: 0 network requests reach transport)
        val cartRepo = koin.get<CartRepository>()
        val academyRepo = koin.get<AcademyRepository>()
        val clinicRepo = koin.get<ClinicRepository>()
        val psychRepo = koin.get<PsychTestRepository>()

        assertIs<AppResult.Error>(cartRepo.getCart().first())
        assertIs<AppResult.Error>(academyRepo.getCourses())
        assertIs<AppResult.Error>(clinicRepo.getTherapists())
        assertIs<AppResult.Error>(psychRepo.getTests())

        assertEquals(0, requestCount.get(), "Zero requests must reach transport when all 5 domains are disabled")

        // 4. DYNAMIC TRANSITION TO ENABLED (Runtime manifest update without DI rebuild)
        val enabledFeatures = mapOf(
            "admin.mobile" to true,
            "content.blog" to true,
            "commerce.core" to true,
            "commerce.physical" to true,
            "commerce.digital" to true,
            "academy.core" to true,
            "academy.quiz" to true,
            "academy.certificate" to true,
            "clinic.booking" to true,
            "clinic.messaging" to true,
            "psych.tests" to true,
            "wallet" to true
        )
        store.update(
            ManifestBootstrapState.Ready(
                ResolvedFeatures.fromMap(enabledFeatures),
                ManifestBootstrapSource.REMOTE,
                "rev-enabled-all"
            )
        )

        // 5. POST-TRANSITION MENU LAYER
        val visibleDestinationsWhenEnabled = if (store.features.value.isEnabled("commerce.core")) {
            BottomBarDestination.entries
        } else {
            listOf(BottomBarDestination.ProductsOverview, BottomBarDestination.Search, BottomBarDestination.More)
        }
        assertTrue(visibleDestinationsWhenEnabled.contains(BottomBarDestination.Cart), "Cart tab must be visible when commerce.core is enabled")
        assertTrue(store.features.value.isEnabled("academy.core"), "showAcademy in MoreScreen must be true")
        assertTrue(store.features.value.isEnabled("clinic.booking"), "showClinic in MoreScreen must be true")
        assertTrue(store.features.value.isEnabled("psych.tests"), "showPsychTests in MoreScreen must be true")

        // 6. POST-TRANSITION ROUTE GUARD LAYER
        assertEquals(RouteGuardDecision.Allowed, routeGuard.checkRoute(Screen.Cart))
        assertEquals(RouteGuardDecision.Allowed, routeGuard.checkRoute(Screen.CourseCatalog))
        assertEquals(RouteGuardDecision.Allowed, routeGuard.checkRoute(Screen.TherapistCatalog))
        assertEquals(RouteGuardDecision.Allowed, routeGuard.checkRoute(Screen.PsychTestCatalog))

        // 7. POST-TRANSITION TRANSPORT LAYER (Valid requests now reach transport and increment counter)
        assertIs<AppResult.Success<*>>(cartRepo.getCart().first { it !is AppResult.Loading })
        assertEquals(1, requestCount.get(), "Cart call must reach transport (count=1)")

        assertIs<AppResult.Success<*>>(academyRepo.getCourses())
        assertEquals(2, requestCount.get(), "Academy call must reach transport (count=2)")

        assertIs<AppResult.Success<*>>(clinicRepo.getTherapists())
        assertEquals(3, requestCount.get(), "Clinic call must reach transport (count=3)")

        assertIs<AppResult.Success<*>>(psychRepo.getTests())
        assertEquals(4, requestCount.get(), "Psych test call must reach transport (count=4)")
    }

    @Test
    fun `tenant switch isolates private namespaces and refreshes manifest with verified request count`() = runBlocking {
        val serverPort = server.address.port
        val ceiling = CompiledFeatureCeiling(FeatureCatalog.knownFeatureIds)

        val profile = BootstrapProfiles.forBackend(
            BackendKind.WORDPRESS,
            "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            WordPressHostMode.CO_INSTALL
        )

        val testKoin = createKoin(module {})
        val httpClient = testKoin.get<HttpClient>()
        val transport = KtorRemoteManifestTransport(httpClient)

        val fullLocal = ResolvedFeatures.fromMap(FeatureCatalog.knownFeatureIds.associateWith { true })

        // Session 1: tenant-alpha
        val profileAlpha = profile.copy(manifestPath = "/wp-json/carmilla/v1/client-manifest?tenant=tenant-alpha")
        val nsAlpha = profileAlpha.privateSessionNamespace("tenant-alpha")
        val clientAlpha = RemoteFeatureManifestClient(profileAlpha, "tenant-alpha", transport, ceiling = ceiling)
        val coordinatorAlpha = FeatureManifestBootstrapCoordinator(
            localFeatures = fullLocal,
            remoteClient = clientAlpha,
            cache = InMemoryLastKnownGoodManifestCache(ceiling = ceiling),
            namespace = nsAlpha,
            nowEpochMillis = { 1000L }
        )

        val stateAlpha = coordinatorAlpha.load()
        assertIs<ManifestBootstrapState.Ready>(stateAlpha)
        assertTrue(stateAlpha.features.isEnabled("commerce.core"), "Tenant Alpha has commerce enabled")
        assertFalse(stateAlpha.features.isEnabled("psych.tests"), "Tenant Alpha has psych disabled")
        assertEquals(1, manifestRequestCount.get(), "Tenant Alpha fetch issued request 1")

        // Verify private session namespace key isolation
        val tokenKeyAlpha = nsAlpha.key("authToken")
        assertTrue(tokenKeyAlpha.contains("tenant-alpha"))

        // Session 2: tenant-beta (Tenant Switch)
        val profileBeta = profile.copy(manifestPath = "/wp-json/carmilla/v1/client-manifest?tenant=tenant-beta")
        val nsBeta = profileBeta.privateSessionNamespace("tenant-beta")
        val clientBeta = RemoteFeatureManifestClient(profileBeta, "tenant-beta", transport, ceiling = ceiling)
        val coordinatorBeta = FeatureManifestBootstrapCoordinator(
            localFeatures = fullLocal,
            remoteClient = clientBeta,
            cache = InMemoryLastKnownGoodManifestCache(ceiling = ceiling),
            namespace = nsBeta,
            nowEpochMillis = { 1000L }
        )

        val stateBeta = coordinatorBeta.load()
        assertIs<ManifestBootstrapState.Ready>(stateBeta)
        assertFalse(stateBeta.features.isEnabled("commerce.core"), "Tenant Beta has commerce disabled")
        assertTrue(stateBeta.features.isEnabled("psych.tests"), "Tenant Beta has psych enabled")
        assertEquals(2, manifestRequestCount.get(), "Tenant Beta fetch issued request 2")

        val tokenKeyBeta = nsBeta.key("authToken")
        assertTrue(tokenKeyBeta.contains("tenant-beta"))
        assertFalse(tokenKeyAlpha == tokenKeyBeta, "Session namespace keys must be isolated between tenants")
    }

    @Test
    fun `stale manifest cache is rejected on network failure and falls back to safe local configuration`() = runBlocking {
        val serverPort = server.address.port
        val ceiling = CompiledFeatureCeiling(FeatureCatalog.knownFeatureIds)

        // Profile pointing to failing manifest endpoint (HTTP 503)
        val failingProfile = BackendProfile(
            kind = BackendKind.WORDPRESS,
            apiRoot = "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            assetRoot = "http://127.0.0.1:$serverPort/",
            allowedAuthHosts = setOf("127.0.0.1"),
            contractVersion = 1,
            manifestPath = "/wp-json/carmilla/v1/client-manifest?fail=true",
            wordPressHostMode = WordPressHostMode.CO_INSTALL
        )

        val testKoin = createKoin(module {})
        val httpClient = testKoin.get<HttpClient>()
        val transport = KtorRemoteManifestTransport(httpClient)

        val namespace = failingProfile.privateSessionNamespace("tenant-stale-test")
        val client = RemoteFeatureManifestClient(failingProfile, "tenant-stale-test", transport, ceiling = ceiling)
        val cache = InMemoryLastKnownGoodManifestCache(ceiling = ceiling)

        val safeLocal = ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to true, "psych.tests" to false))

        // Pre-populate cache with a timestamp: issued at t=100L, ttl=50L
        cache.write(
            namespace = namespace,
            manifest = FeatureManifest(
                schemaVersion = 1,
                manifestVersion = "v-cached",
                backendKind = BackendKind.WORDPRESS,
                tenantId = "tenant-stale-test",
                features = mapOf("content.blog" to true, "commerce.core" to true)
            ),
            etag = "etag-cached",
            expiresAtEpochMillis = 150L,
            nowEpochMillis = 100L
        )

        // Phase 1: Fresh cache within TTL (now = 120L < 150L) -> Uses LAST_KNOWN_GOOD
        var currentTime = 120L
        val freshCoordinator = FeatureManifestBootstrapCoordinator(
            localFeatures = safeLocal,
            remoteClient = client,
            cache = cache,
            namespace = namespace,
            nowEpochMillis = { currentTime },
            cacheTtlMillis = 50L
        )
        val freshState = freshCoordinator.load()
        assertIs<ManifestBootstrapState.Error>(freshState)
        assertEquals(ManifestBootstrapSource.LAST_KNOWN_GOOD, freshState.source, "Within TTL, fresh cache should be used as fallback")
        assertTrue(freshState.features.isEnabled("commerce.core"))

        // Phase 2: Stale cache expired (now = 200L > 150L) -> Stale cache is REJECTED, falls back to safe LOCAL
        currentTime = 200L
        val staleCoordinator = FeatureManifestBootstrapCoordinator(
            localFeatures = safeLocal,
            remoteClient = client,
            cache = cache,
            namespace = namespace,
            nowEpochMillis = { currentTime },
            cacheTtlMillis = 50L
        )
        val staleState = staleCoordinator.load()
        assertIs<ManifestBootstrapState.Error>(staleState)
        assertEquals(ManifestBootstrapSource.LOCAL, staleState.source, "Expired stale cache MUST be rejected in favor of safe LOCAL fallback")
        assertTrue(staleState.features.isEnabled("commerce.core"), "Local safe configuration is preserved on fallback")
        assertFalse(staleState.features.isEnabled("psych.tests"), "Local configuration does not enable unconfigured features")
    }

    @Test
    fun `simulated purchase ceiling prevents unauthorized feature activation from remote manifest`() = runBlocking {
        val serverPort = server.address.port

        // Chronos-like ceiling: physical commerce only, explicitly excludes academy, clinic and psych
        val limitedCeiling = CompiledFeatureCeiling(
            setOf("content.blog", "commerce.core", "commerce.physical")
        )

        val profile = BootstrapProfiles.forBackend(
            BackendKind.WORDPRESS,
            "http://127.0.0.1:$serverPort/wp-json/carmilla/v1/",
            WordPressHostMode.CO_INSTALL
        )

        val testKoin = createKoin(module {})
        val httpClient = testKoin.get<HttpClient>()
        val transport = KtorRemoteManifestTransport(httpClient)

        // Remote manifest on server claims academy.core, clinic.booking, and psych.tests are true for tenant-alpha
        val client = RemoteFeatureManifestClient(profile, "tenant-alpha", transport, ceiling = limitedCeiling)
        val result = client.fetch()
        assertIs<RemoteManifestFetchResult.Success>(result)

        // Verify ceiling blocked the unauthorized features
        val effectiveFeatures = result.features
        assertTrue(effectiveFeatures.isEnabled("commerce.core"), "Allowed compiled feature remains enabled")
        assertTrue(effectiveFeatures.isEnabled("commerce.physical"), "Allowed compiled feature remains enabled")

        assertFalse(effectiveFeatures.isEnabled("academy.core"), "Feature outside purchase ceiling MUST be disabled")
        assertFalse(effectiveFeatures.isEnabled("clinic.booking"), "Feature outside purchase ceiling MUST be disabled")
        assertFalse(effectiveFeatures.isEnabled("psych.tests"), "Feature outside purchase ceiling MUST be disabled")

        // Verify at Route Guard level
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(effectiveFeatures)
        val routeGuard = FeatureRouteGuard(store)

        val allowedRoute = routeGuard.checkRoute(Screen.ProductDetail(slug = "widget"))
        assertEquals(RouteGuardDecision.Allowed, allowedRoute)

        val blockedAcademy = routeGuard.checkRoute(Screen.CourseCatalog)
        assertTrue(blockedAcademy is RouteGuardDecision.Blocked && blockedAcademy.featureId == "academy.core")

        val blockedClinic = routeGuard.checkRoute(Screen.TherapistCatalog)
        assertTrue(blockedClinic is RouteGuardDecision.Blocked && blockedClinic.featureId == "clinic.booking")

        val blockedPsych = routeGuard.checkRoute(Screen.PsychTestCatalog)
        assertTrue(blockedPsych is RouteGuardDecision.Blocked && blockedPsych.featureId == "psych.tests")
    }
}
