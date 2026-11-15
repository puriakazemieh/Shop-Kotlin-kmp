package com.kazemieh.data.academy

import com.kazemieh.common.AppResult
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.EffectiveFeatureStore
import com.kazemieh.config.capabilities.FeatureCatalog
import com.kazemieh.config.capabilities.FeatureManifest
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.ManifestBootstrapSource
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import com.kazemieh.config.capabilities.ResolvedFeatures
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.data.local.TokenManager
import com.kazemieh.domain.academy.AcademyRepository
import com.kazemieh.domain.academy.AdminAcademyRepository
import com.kazemieh.domain.courserequest.CourseRequestRepository
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

class AcademyOperationalGuardTest {

    private lateinit var server: HttpServer
    private val requestCount = AtomicInteger(0)
    private val requestHeadersList = mutableListOf<Map<String, String>>()
    private val originalBaseUrl = ApiConfig.baseUrlOverride

    private val namespace = PrivateSessionNamespace(
        backendKind = BackendKind.WORDPRESS,
        tenantId = "academy-guard-test",
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
            if (path.contains("/api/courses/forbidden-course") || path.contains("/api/academy/courses/999")) {
                val forbiddenBody = """{"message":"Access denied: course belongs to another tenant/user"}"""
                exchange.responseHeaders.add("Content-Type", "application/json")
                val bytes = forbiddenBody.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(403, bytes.size.toLong())
                exchange.responseBody.write(bytes)
                exchange.close()
                return@createContext
            }

            val responseBody = when {
                path.contains("/api/courses/sample-course") -> """{"id":1,"title":"Sample Course","slug":"sample-course","sections":[]}"""
                path.contains("/api/courses") -> """[]"""
                path.contains("/api/academy/my-courses") -> """[]"""
                path.contains("/api/academy/courses/1/quiz") -> """{"courseId":1,"title":"Quiz 1","passScore":70,"alreadyPassed":false,"hasQuiz":true,"questions":[]}"""
                path.contains("/api/academy/certificates") -> """[]"""
                path.contains("/api/course-requests") -> """[]"""
                path.contains("/api/admin/courses") -> """[]"""
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
    fun `when academy is disabled in DI then repositories emit 0 requests to transport and fail closed`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(
            mapOf(
                "academy.core" to false,
                "academy.quiz" to false,
                "academy.certificate" to false
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

        val academyRepo = koin.get<AcademyRepository>()
        val adminAcademyRepo = koin.get<AdminAcademyRepository>()
        val courseRequestRepo = koin.get<CourseRequestRepository>()

        // 1. Academy getCourses check
        val coursesResult = academyRepo.getCourses()
        assertIs<AppResult.Error>(coursesResult)
        assertEquals(403, coursesResult.code)
        assertTrue(coursesResult.message.toString().contains("academy.core"))
        assertEquals(0, requestCount.get(), "getCourses must produce 0 network requests when academy.core is disabled")

        // 2. Academy getCourse detail check
        val detailResult = academyRepo.getCourse("sample-course")
        assertIs<AppResult.Error>(detailResult)
        assertEquals(403, detailResult.code)
        assertTrue(detailResult.message.toString().contains("academy.core"))
        assertEquals(0, requestCount.get(), "getCourse must produce 0 network requests when academy.core is disabled")

        // 3. Academy getMyCourses check
        val myCoursesResult = academyRepo.getMyCourses()
        assertIs<AppResult.Error>(myCoursesResult)
        assertEquals(403, myCoursesResult.code)
        assertTrue(myCoursesResult.message.toString().contains("academy.core"))
        assertEquals(0, requestCount.get(), "getMyCourses must produce 0 network requests when academy.core is disabled")

        // 4. Academy quiz check
        val quizResult = academyRepo.getQuiz(1L)
        assertIs<AppResult.Error>(quizResult)
        assertEquals(403, quizResult.code)
        assertTrue(quizResult.message.toString().contains("academy.quiz"))
        assertEquals(0, requestCount.get(), "getQuiz must produce 0 network requests when academy.quiz is disabled")

        // 5. Academy certificates check
        val certsResult = academyRepo.getCertificates()
        assertIs<AppResult.Error>(certsResult)
        assertEquals(403, certsResult.code)
        assertTrue(certsResult.message.toString().contains("academy.certificate"))
        assertEquals(0, requestCount.get(), "getCertificates must produce 0 network requests when academy.certificate is disabled")

        // 6. Course request check
        val courseReqResult = courseRequestRepo.list()
        assertIs<AppResult.Error>(courseReqResult)
        assertEquals(403, courseReqResult.code)
        assertTrue(courseReqResult.message.toString().contains("academy.core"))
        assertEquals(0, requestCount.get(), "CourseRequest list must produce 0 network requests when academy.core is disabled")

        // 7. Admin Academy check
        val adminCoursesResult = adminAcademyRepo.listCourses()
        assertIs<AppResult.Error>(adminCoursesResult)
        assertEquals(403, adminCoursesResult.code)
        assertTrue(adminCoursesResult.message.toString().contains("academy.core"))
        assertEquals(0, requestCount.get(), "Admin listCourses must produce 0 network requests when academy.core is disabled")
    }

    @Test
    fun `when academy is enabled then valid requests reach transport and increment request counter`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "academy.core" to true,
                    "academy.quiz" to true,
                    "academy.certificate" to true
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
        tokenManager.saveTokens("synthetic-academy-bearer-token", "synthetic-academy-refresh-token")

        val academyRepo = koin.get<AcademyRepository>()
        val adminAcademyRepo = koin.get<AdminAcademyRepository>()
        val courseRequestRepo = koin.get<CourseRequestRepository>()

        // 1. getCourses call with feature enabled
        val coursesResult = academyRepo.getCourses()
        assertIs<AppResult.Success<*>>(coursesResult)
        assertEquals(1, requestCount.get(), "Enabled academy.core must forward getCourses request to transport (count=1)")

        // 2. getQuiz call with feature enabled
        val quizResult = academyRepo.getQuiz(1L)
        assertIs<AppResult.Success<*>>(quizResult)
        assertEquals(2, requestCount.get(), "Enabled academy.quiz must forward getQuiz request to transport (count=2)")

        // 3. getCertificates call with feature enabled
        val certsResult = academyRepo.getCertificates()
        assertIs<AppResult.Success<*>>(certsResult)
        assertEquals(3, requestCount.get(), "Enabled academy.certificate must forward getCertificates request to transport (count=3)")

        // 4. course requests call with feature enabled
        val reqResult = courseRequestRepo.list()
        assertIs<AppResult.Success<*>>(reqResult)
        assertEquals(4, requestCount.get(), "Enabled academy.core must forward course requests to transport (count=4)")

        // 5. admin academy call with feature enabled
        val adminResult = adminAcademyRepo.listCourses()
        assertIs<AppResult.Success<*>>(adminResult)
        assertEquals(5, requestCount.get(), "Enabled academy.core must forward admin listCourses to transport (count=5)")
    }

    @Test
    fun `hierarchy of academy sub-features - quiz and certificate disabled while academy core is enabled`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "academy.core" to true,
                    "academy.quiz" to false,
                    "academy.certificate" to false
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

        val academyRepo = koin.get<AcademyRepository>()

        // 1. Core courses call succeeds and produces 1 request
        val coursesResult = academyRepo.getCourses()
        assertIs<AppResult.Success<*>>(coursesResult)
        assertEquals(1, requestCount.get(), "Enabled academy.core must increment transport count to 1")

        // 2. Quiz call fails closed with 403 and produces 0 additional requests
        val quizResult = academyRepo.getQuiz(1L)
        assertIs<AppResult.Error>(quizResult)
        assertEquals(403, quizResult.code)
        assertTrue(quizResult.message.toString().contains("academy.quiz"))
        assertEquals(1, requestCount.get(), "Disabled academy.quiz must produce 0 additional requests (count stays 1)")

        // 3. Certificate call fails closed with 403 and produces 0 additional requests
        val certsResult = academyRepo.getCertificates()
        assertIs<AppResult.Error>(certsResult)
        assertEquals(403, certsResult.code)
        assertTrue(certsResult.message.toString().contains("academy.certificate"))
        assertEquals(1, requestCount.get(), "Disabled academy.certificate must produce 0 additional requests (count stays 1)")
    }

    @Test
    fun `auth token is forwarded when enabled and server ownership cannot be bypassed by client toggle`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "academy.core" to true,
                    "academy.quiz" to true,
                    "academy.certificate" to true
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
        tokenManager.saveTokens("academy-bearer-token-xyz", "academy-refresh-token-xyz")

        val academyRepo = koin.get<AcademyRepository>()

        // 1. Normal authorized call sends bearer header
        val coursesResult = academyRepo.getCourses()
        assertIs<AppResult.Success<*>>(coursesResult)
        assertEquals(1, requestCount.get())

        val lastHeaders = synchronized(requestHeadersList) { requestHeadersList.lastOrNull() }
        val authHeader = lastHeaders?.entries?.firstOrNull { it.key.equals("Authorization", ignoreCase = true) }?.value
        assertEquals("Bearer academy-bearer-token-xyz", authHeader, "Authorization Bearer token must be forwarded on transport")

        // 2. Call to forbidden course (another tenant/user's course) - server denies access (HTTP 403)
        val forbiddenResult = academyRepo.getCourse("forbidden-course")
        assertIs<AppResult.Error>(forbiddenResult)
        assertEquals(403, forbiddenResult.code, "Server ownership check must reject unauthorized access (403) even when client toggle is true")
        assertEquals(2, requestCount.get(), "Request must have reached server where ownership was verified and denied")
    }

    @Test
    fun `dynamic runtime transition - switching feature store state updates repository guard without DI rebuild`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "academy.core" to false,
                    "academy.quiz" to false,
                    "academy.certificate" to false
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
        val academyRepo = koin.get<AcademyRepository>()

        // Phase 1: Disabled -> 0 requests, returns 403
        val initialResult = academyRepo.getCourses()
        assertIs<AppResult.Error>(initialResult)
        assertEquals(403, initialResult.code)
        assertEquals(0, requestCount.get(), "Initially disabled feature must produce 0 requests")

        // Phase 2: Runtime dynamic update (simulate server manifest fetch or user setting update)
        store.update(
            ManifestBootstrapState.Ready(
                ResolvedFeatures.fromMap(
                    mapOf(
                        "academy.core" to true,
                        "academy.quiz" to true,
                        "academy.certificate" to true
                    )
                ),
                ManifestBootstrapSource.REMOTE,
                "revision-academy-enabled"
            )
        )

        // Phase 3: Immediate call now succeeds and produces 1 request through the same repository instance
        val postUpdateResult = academyRepo.getCourses()
        assertIs<AppResult.Success<*>>(postUpdateResult)
        assertEquals(1, requestCount.get(), "After dynamic store update, subsequent call must reach transport (count=1)")
    }
}
