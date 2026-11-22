package com.kazemieh.data.psychtest

import com.kazemieh.common.AppResult
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.EffectiveFeatureStore
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.ManifestBootstrapSource
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import com.kazemieh.config.capabilities.ResolvedFeatures
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.data.local.TokenManager
import com.kazemieh.domain.psychtest.AdminPsychTestRepository
import com.kazemieh.domain.psychtest.PsychTestRepository
import com.kazemieh.network.common.ApiConfig
import com.kazemieh.network.di.networkModule
import com.sun.net.httpserver.HttpServer
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PsychTestOperationalGuardTest {

    private lateinit var server: HttpServer
    private val requestCount = AtomicInteger(0)
    private val requestHeadersList = mutableListOf<Map<String, String>>()
    private val originalBaseUrl = ApiConfig.baseUrlOverride

    private val namespace = PrivateSessionNamespace(
        backendKind = BackendKind.WORDPRESS,
        tenantId = "psychtest-guard-test",
        origin = "http://127.0.0.1"
    )

    private val testAssetUrlResolver = object : AssetUrlResolver {
        override fun resolve(path: String): String = path
    }

    private var activeKoinApp: KoinApplication? = null

    private fun createKoin(testModule: Module): Koin {
        activeKoinApp?.close()
        val app = koinApplication {
            modules(networkModule, dataModule, platformModule(), testModule)
        }
        activeKoinApp = app
        return app.koin
    }

    @BeforeTest
    fun setup() {
        requestCount.set(0)
        requestHeadersList.clear()

        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            requestCount.incrementAndGet()
            val path = exchange.requestURI.path
            val headersMap = exchange.requestHeaders.entries.associate { (k, v) -> k to v.joinToString(",") }
            synchronized(requestHeadersList) {
                requestHeadersList.add(headersMap)
            }

            // Server-side ownership / authorization simulation
            if (path.contains("/api/my-psych-tests/999")) {
                val forbiddenBody = """{"message":"Access denied: test attempt belongs to another user"}"""
                exchange.responseHeaders.add("Content-Type", "application/json")
                val bytes = forbiddenBody.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(403, bytes.size.toLong())
                exchange.responseBody.write(bytes)
                exchange.close()
                return@createContext
            }

            val responseBody = when {
                path.contains("/api/psych-tests/sample-test") -> """{"id":1,"title":"Sample Test","slug":"sample-test","description":"Sample Desc","price":0.0,"resultMode":"AUTOMATIC","owned":true,"questions":[]}"""
                path.contains("/api/psych-tests") -> """[]"""
                path.contains("/api/my-psych-tests/1/questions") -> """{"id":1,"title":"Sample Test","slug":"sample-test","description":"Sample Desc","price":0.0,"resultMode":"AUTOMATIC","owned":true,"questions":[]}"""
                path.contains("/api/my-psych-tests/1/submit") -> """{"id":1,"testId":1,"testTitle":"Sample Test","status":"COMPLETED","resultMode":"AUTOMATIC","totalScore":85.0}"""
                path.contains("/api/my-psych-tests") -> """[]"""
                path.contains("/api/admin/psych-tests/pending-interpretations") -> """[]"""
                path.contains("/api/admin/psych-tests/1") -> """{"id":1,"title":"Sample Test","slug":"sample-test","price":0.0,"resultMode":"AUTOMATIC","isPublished":true,"questions":[],"ranges":[]}"""
                path.contains("/api/admin/psych-tests") -> """[]"""
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
    fun `when psych test is disabled in DI then repositories emit 0 requests to transport and fail closed`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(
            mapOf(
                "psych.tests" to false
            )
        )
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(fallback)
        val guard = FeatureUseCaseGuard(store)

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)

        val psychRepo = koin.get<PsychTestRepository>()
        val adminPsychRepo = koin.get<AdminPsychTestRepository>()

        // 1. getTests check
        val testsResult = psychRepo.getTests()
        assertIs<AppResult.Error>(testsResult)
        assertEquals(403, testsResult.code)
        assertTrue(testsResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "getTests must produce 0 network requests when psych.tests is disabled")

        // 2. getTest detail check
        val detailResult = psychRepo.getTest("sample-test")
        assertIs<AppResult.Error>(detailResult)
        assertEquals(403, detailResult.code)
        assertTrue(detailResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "getTest must produce 0 network requests when psych.tests is disabled")

        // 3. getMyTests check
        val myTestsResult = psychRepo.getMyTests()
        assertIs<AppResult.Error>(myTestsResult)
        assertEquals(403, myTestsResult.code)
        assertTrue(myTestsResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "getMyTests must produce 0 network requests when psych.tests is disabled")

        // 4. getUserTestQuestions check
        val questionsResult = psychRepo.getUserTestQuestions(1L)
        assertIs<AppResult.Error>(questionsResult)
        assertEquals(403, questionsResult.code)
        assertTrue(questionsResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "getUserTestQuestions must produce 0 network requests when psych.tests is disabled")

        // 5. submit check
        val submitResult = psychRepo.submit(1L, mapOf(0 to 1))
        assertIs<AppResult.Error>(submitResult)
        assertEquals(403, submitResult.code)
        assertTrue(submitResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "submit must produce 0 network requests when psych.tests is disabled")

        // 6. Admin list check
        val adminListResult = adminPsychRepo.list()
        assertIs<AppResult.Error>(adminListResult)
        assertEquals(403, adminListResult.code)
        assertTrue(adminListResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "Admin list must produce 0 network requests when psych.tests is disabled")

        // 7. Admin detail check
        val adminDetailResult = adminPsychRepo.detail(1L)
        assertIs<AppResult.Error>(adminDetailResult)
        assertEquals(403, adminDetailResult.code)
        assertTrue(adminDetailResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "Admin detail must produce 0 network requests when psych.tests is disabled")

        // 8. Admin pendingInterpretations check
        val adminPendingResult = adminPsychRepo.pendingInterpretations()
        assertIs<AppResult.Error>(adminPendingResult)
        assertEquals(403, adminPendingResult.code)
        assertTrue(adminPendingResult.message.toString().contains("psych.tests"))
        assertEquals(0, requestCount.get(), "Admin pendingInterpretations must produce 0 network requests when psych.tests is disabled")
    }

    @Test
    fun `when psych test is enabled then valid requests reach transport and increment request counter`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "psych.tests" to true
                )
            )
        )
        val guard = FeatureUseCaseGuard(store)

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)

        val tokenManager = koin.get<TokenManager>()
        tokenManager.saveTokens("synthetic-psych-bearer-token", "synthetic-psych-refresh-token")

        val psychRepo = koin.get<PsychTestRepository>()
        val adminPsychRepo = koin.get<AdminPsychTestRepository>()

        // 1. getTests call with feature enabled
        val testsResult = psychRepo.getTests()
        assertIs<AppResult.Success<*>>(testsResult)
        assertEquals(1, requestCount.get(), "Enabled psych.tests must forward getTests request to transport (count=1)")

        // 2. getMyTests call with feature enabled
        val myTestsResult = psychRepo.getMyTests()
        assertIs<AppResult.Success<*>>(myTestsResult)
        assertEquals(2, requestCount.get(), "Enabled psych.tests must forward getMyTests request to transport (count=2)")

        // 3. getUserTestQuestions call with feature enabled
        val questionsResult = psychRepo.getUserTestQuestions(1L)
        assertIs<AppResult.Success<*>>(questionsResult)
        assertEquals(3, requestCount.get(), "Enabled psych.tests must forward getUserTestQuestions request to transport (count=3)")

        // 4. admin list call with feature enabled
        val adminListResult = adminPsychRepo.list()
        assertIs<AppResult.Success<*>>(adminListResult)
        assertEquals(4, requestCount.get(), "Enabled psych.tests must forward admin list to transport (count=4)")

        // 5. admin pendingInterpretations call with feature enabled
        val adminPendingResult = adminPsychRepo.pendingInterpretations()
        assertIs<AppResult.Success<*>>(adminPendingResult)
        assertEquals(5, requestCount.get(), "Enabled psych.tests must forward admin pendingInterpretations to transport (count=5)")
    }

    @Test
    fun `auth token is forwarded when enabled and server ownership cannot be bypassed by client toggle`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "psych.tests" to true
                )
            )
        )
        val guard = FeatureUseCaseGuard(store)

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)

        val tokenManager = koin.get<TokenManager>()
        tokenManager.saveTokens("psych-bearer-token-xyz", "psych-refresh-token-xyz")

        val psychRepo = koin.get<PsychTestRepository>()

        // 1. Normal authorized call sends bearer header
        val testsResult = psychRepo.getTests()
        assertIs<AppResult.Success<*>>(testsResult)
        assertEquals(1, requestCount.get())

        val lastHeaders = synchronized(requestHeadersList) { requestHeadersList.lastOrNull() }
        val authHeader = lastHeaders?.entries?.firstOrNull { it.key.equals("Authorization", ignoreCase = true) }?.value
        assertEquals("Bearer psych-bearer-token-xyz", authHeader, "Authorization Bearer token must be forwarded on transport")

        // 2. Call to forbidden user test (another user's test questions) - server denies access (HTTP 403)
        val forbiddenResult = psychRepo.getUserTestQuestions(999L)
        assertIs<AppResult.Error>(forbiddenResult)
        assertEquals(403, forbiddenResult.code, "Server ownership check must reject unauthorized access (403) even when client toggle is true")
        assertEquals(2, requestCount.get(), "Request must have reached server where ownership was verified and denied")
    }

    @Test
    fun `dynamic runtime transition - switching feature store state updates repository guard without DI rebuild`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "psych.tests" to false
                )
            )
        )
        val guard = FeatureUseCaseGuard(store)

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)
        val psychRepo = koin.get<PsychTestRepository>()

        // Phase 1: Disabled -> 0 requests, returns 403
        val initialResult = psychRepo.getTests()
        assertIs<AppResult.Error>(initialResult)
        assertEquals(403, initialResult.code)
        assertEquals(0, requestCount.get(), "Initially disabled feature must produce 0 requests")

        // Phase 2: Runtime dynamic update (simulate server manifest fetch or user setting update)
        store.update(
            ManifestBootstrapState.Ready(
                ResolvedFeatures.fromMap(
                    mapOf(
                        "psych.tests" to true
                    )
                ),
                ManifestBootstrapSource.REMOTE,
                "revision-psych-enabled"
            )
        )

        // Phase 3: Immediate call now succeeds and produces 1 request through the same repository instance
        val postUpdateResult = psychRepo.getTests()
        assertIs<AppResult.Success<*>>(postUpdateResult)
        assertEquals(1, requestCount.get(), "After dynamic store update, subsequent call must reach transport (count=1)")
    }
}
