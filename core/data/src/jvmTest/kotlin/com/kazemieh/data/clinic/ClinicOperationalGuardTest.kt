package com.kazemieh.data.clinic

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
import com.kazemieh.domain.clinic.AdminClinicRepository
import com.kazemieh.domain.clinic.ClinicRepository
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

class ClinicOperationalGuardTest {

    private lateinit var server: HttpServer
    private val requestCount = AtomicInteger(0)
    private val requestHeadersList = mutableListOf<Map<String, String>>()
    private val originalBaseUrl = ApiConfig.baseUrlOverride

    private val namespace = PrivateSessionNamespace(
        backendKind = BackendKind.WORDPRESS,
        tenantId = "clinic-guard-test",
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
            if (path.contains("/api/clinic/appointments/999/cancel") || path.contains("/api/clinic/appointments/999/receipt")) {
                val forbiddenBody = """{"message":"Access denied: appointment belongs to another patient"}"""
                exchange.responseHeaders.add("Content-Type", "application/json")
                val bytes = forbiddenBody.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(403, bytes.size.toLong())
                exchange.responseBody.write(bytes)
                exchange.close()
                return@createContext
            }

            val responseBody = when {
                path.contains("/api/therapists/sample-therapist") -> """{"id":1,"name":"Dr. Sara","slug":"sample-therapist","specialty":"CBT","sessionPrice":500000.0,"sessionDurationMinutes":50,"slots":[]}"""
                path.contains("/api/therapists") -> """[]"""
                path.contains("/api/clinic/my-appointments") -> """[]"""
                path.contains("/api/clinic/appointments/1/receipt") -> """{"appointmentId":1,"patientName":"Ali","therapistName":"Dr. Sara","therapistSpecialty":"CBT","sessionMode":"ONLINE","sessionDate":"2026-11-22","sessionDurationMinutes":50,"amountPaid":500000.0}"""
                path.contains("/api/clinic/therapists/1/messages") -> """[]"""
                path.contains("/api/clinic/therapists/1/messaging-status") -> """{"therapistId":1,"active":true,"freeMessagesRemaining":5}"""
                path.contains("/api/clinic/homework") -> """[]"""
                path.contains("/api/clinic/journal") -> """[]"""
                path.contains("/api/clinic/therapist-match/questions") -> """[]"""
                path.contains("/api/admin/therapists/1/patients/2/messages") -> """[]"""
                path.contains("/api/admin/therapists/appointments") -> """[]"""
                path.contains("/api/admin/therapists") -> """[]"""
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
    fun `when clinic is disabled in DI then repositories emit 0 requests to transport and fail closed`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(
            mapOf(
                "clinic.booking" to false,
                "clinic.messaging" to false
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

        val clinicRepo = koin.get<ClinicRepository>()
        val adminClinicRepo = koin.get<AdminClinicRepository>()

        // 1. Clinic getTherapists check
        val therapistsResult = clinicRepo.getTherapists()
        assertIs<AppResult.Error>(therapistsResult)
        assertEquals(403, therapistsResult.code)
        assertTrue(therapistsResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "getTherapists must produce 0 network requests when clinic.booking is disabled")

        // 2. Clinic getTherapist detail check
        val detailResult = clinicRepo.getTherapist("sample-therapist")
        assertIs<AppResult.Error>(detailResult)
        assertEquals(403, detailResult.code)
        assertTrue(detailResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "getTherapist must produce 0 network requests when clinic.booking is disabled")

        // 3. Clinic getMyAppointments check
        val myAppointmentsResult = clinicRepo.getMyAppointments()
        assertIs<AppResult.Error>(myAppointmentsResult)
        assertEquals(403, myAppointmentsResult.code)
        assertTrue(myAppointmentsResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "getMyAppointments must produce 0 network requests when clinic.booking is disabled")

        // 4. Clinic messaging check
        val messagesResult = clinicRepo.listMessages(1L)
        assertIs<AppResult.Error>(messagesResult)
        assertEquals(403, messagesResult.code)
        assertTrue(messagesResult.message.toString().contains("clinic.messaging"))
        assertEquals(0, requestCount.get(), "listMessages must produce 0 network requests when clinic.messaging is disabled")

        // 5. Clinic homework check
        val homeworkResult = clinicRepo.myHomework()
        assertIs<AppResult.Error>(homeworkResult)
        assertEquals(403, homeworkResult.code)
        assertTrue(homeworkResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "myHomework must produce 0 network requests when clinic.booking is disabled")

        // 6. Clinic journal check
        val journalResult = clinicRepo.myJournal()
        assertIs<AppResult.Error>(journalResult)
        assertEquals(403, journalResult.code)
        assertTrue(journalResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "myJournal must produce 0 network requests when clinic.booking is disabled")

        // 7. Clinic match questions check
        val matchResult = clinicRepo.matchQuestions()
        assertIs<AppResult.Error>(matchResult)
        assertEquals(403, matchResult.code)
        assertTrue(matchResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "matchQuestions must produce 0 network requests when clinic.booking is disabled")

        // 8. Admin Clinic listTherapists check
        val adminTherapistsResult = adminClinicRepo.listTherapists()
        assertIs<AppResult.Error>(adminTherapistsResult)
        assertEquals(403, adminTherapistsResult.code)
        assertTrue(adminTherapistsResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "Admin listTherapists must produce 0 network requests when clinic.booking is disabled")

        // 9. Admin Clinic listAppointments check
        val adminAppointmentsResult = adminClinicRepo.listAppointments()
        assertIs<AppResult.Error>(adminAppointmentsResult)
        assertEquals(403, adminAppointmentsResult.code)
        assertTrue(adminAppointmentsResult.message.toString().contains("clinic.booking"))
        assertEquals(0, requestCount.get(), "Admin listAppointments must produce 0 network requests when clinic.booking is disabled")

        // 10. Admin Clinic listMessagesWithPatient check
        val adminMessagesResult = adminClinicRepo.listMessagesWithPatient(1L, 2L)
        assertIs<AppResult.Error>(adminMessagesResult)
        assertEquals(403, adminMessagesResult.code)
        assertTrue(adminMessagesResult.message.toString().contains("clinic.messaging"))
        assertEquals(0, requestCount.get(), "Admin listMessagesWithPatient must produce 0 network requests when clinic.messaging is disabled")
    }

    @Test
    fun `when clinic is enabled then valid requests reach transport and increment request counter`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "clinic.booking" to true,
                    "clinic.messaging" to true
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
        tokenManager.saveTokens("synthetic-clinic-bearer-token", "synthetic-clinic-refresh-token")

        val clinicRepo = koin.get<ClinicRepository>()
        val adminClinicRepo = koin.get<AdminClinicRepository>()

        // 1. getTherapists call with feature enabled
        val therapistsResult = clinicRepo.getTherapists()
        assertIs<AppResult.Success<*>>(therapistsResult)
        assertEquals(1, requestCount.get(), "Enabled clinic.booking must forward getTherapists request to transport (count=1)")

        // 2. getMyAppointments call with feature enabled
        val appointmentsResult = clinicRepo.getMyAppointments()
        assertIs<AppResult.Success<*>>(appointmentsResult)
        assertEquals(2, requestCount.get(), "Enabled clinic.booking must forward getMyAppointments request to transport (count=2)")

        // 3. listMessages call with feature enabled
        val messagesResult = clinicRepo.listMessages(1L)
        assertIs<AppResult.Success<*>>(messagesResult)
        assertEquals(3, requestCount.get(), "Enabled clinic.messaging must forward listMessages request to transport (count=3)")

        // 4. myHomework call with feature enabled
        val homeworkResult = clinicRepo.myHomework()
        assertIs<AppResult.Success<*>>(homeworkResult)
        assertEquals(4, requestCount.get(), "Enabled clinic.booking must forward myHomework request to transport (count=4)")

        // 5. admin clinic listTherapists call with feature enabled
        val adminTherapistsResult = adminClinicRepo.listTherapists()
        assertIs<AppResult.Success<*>>(adminTherapistsResult)
        assertEquals(5, requestCount.get(), "Enabled clinic.booking must forward admin listTherapists to transport (count=5)")

        // 6. admin clinic listMessagesWithPatient call with feature enabled
        val adminMessagesResult = adminClinicRepo.listMessagesWithPatient(1L, 2L)
        assertIs<AppResult.Success<*>>(adminMessagesResult)
        assertEquals(6, requestCount.get(), "Enabled clinic.messaging must forward admin listMessagesWithPatient to transport (count=6)")
    }

    @Test
    fun `hierarchy of clinic sub-features - messaging disabled while clinic booking is enabled`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "clinic.booking" to true,
                    "clinic.messaging" to false
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

        val clinicRepo = koin.get<ClinicRepository>()
        val adminClinicRepo = koin.get<AdminClinicRepository>()

        // 1. Booking call succeeds and produces 1 request
        val therapistsResult = clinicRepo.getTherapists()
        assertIs<AppResult.Success<*>>(therapistsResult)
        assertEquals(1, requestCount.get(), "Enabled clinic.booking must increment transport count to 1")

        // 2. Messaging call fails closed with 403 and produces 0 additional requests
        val messagesResult = clinicRepo.listMessages(1L)
        assertIs<AppResult.Error>(messagesResult)
        assertEquals(403, messagesResult.code)
        assertTrue(messagesResult.message.toString().contains("clinic.messaging"))
        assertEquals(1, requestCount.get(), "Disabled clinic.messaging must produce 0 additional requests (count stays 1)")

        // 3. Messaging send call fails closed with 403 and produces 0 additional requests
        val sendResult = clinicRepo.sendMessage(1L, "Hello doctor")
        assertIs<AppResult.Error>(sendResult)
        assertEquals(403, sendResult.code)
        assertTrue(sendResult.message.toString().contains("clinic.messaging"))
        assertEquals(1, requestCount.get(), "Disabled clinic.messaging must produce 0 additional requests (count stays 1)")

        // 4. Messaging status call fails closed with 403 and produces 0 additional requests
        val statusResult = clinicRepo.messagingStatus(1L)
        assertIs<AppResult.Error>(statusResult)
        assertEquals(403, statusResult.code)
        assertTrue(statusResult.message.toString().contains("clinic.messaging"))
        assertEquals(1, requestCount.get(), "Disabled clinic.messaging must produce 0 additional requests (count stays 1)")

        // 5. Admin messaging call fails closed with 403 and produces 0 additional requests
        val adminMsgResult = adminClinicRepo.listMessagesWithPatient(1L, 2L)
        assertIs<AppResult.Error>(adminMsgResult)
        assertEquals(403, adminMsgResult.code)
        assertTrue(adminMsgResult.message.toString().contains("clinic.messaging"))
        assertEquals(1, requestCount.get(), "Disabled clinic.messaging must produce 0 additional requests (count stays 1)")
    }

    @Test
    fun `auth token is forwarded when enabled and server ownership cannot be bypassed by client toggle`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "admin.mobile" to true,
                    "clinic.booking" to true,
                    "clinic.messaging" to true
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
        tokenManager.saveTokens("clinic-bearer-token-xyz", "clinic-refresh-token-xyz")

        val clinicRepo = koin.get<ClinicRepository>()

        // 1. Normal authorized call sends bearer header
        val therapistsResult = clinicRepo.getTherapists()
        assertIs<AppResult.Success<*>>(therapistsResult)
        assertEquals(1, requestCount.get())

        val lastHeaders = synchronized(requestHeadersList) { requestHeadersList.lastOrNull() }
        val authHeader = lastHeaders?.entries?.firstOrNull { it.key.equals("Authorization", ignoreCase = true) }?.value
        assertEquals("Bearer clinic-bearer-token-xyz", authHeader, "Authorization Bearer token must be forwarded on transport")

        // 2. Call to forbidden appointment (another patient's appointment) - server denies access (HTTP 403)
        val forbiddenResult = clinicRepo.cancel(999L)
        assertIs<AppResult.Error>(forbiddenResult)
        assertEquals(403, forbiddenResult.code, "Server ownership check must reject unauthorized access (403) even when client toggle is true")
        assertEquals(2, requestCount.get(), "Request must have reached server where ownership was verified and denied")
    }

    @Test
    fun `dynamic runtime transition - switching feature store state updates repository guard without DI rebuild`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "clinic.booking" to false,
                    "clinic.messaging" to false
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
        val clinicRepo = koin.get<ClinicRepository>()

        // Phase 1: Disabled -> 0 requests, returns 403
        val initialResult = clinicRepo.getTherapists()
        assertIs<AppResult.Error>(initialResult)
        assertEquals(403, initialResult.code)
        assertEquals(0, requestCount.get(), "Initially disabled feature must produce 0 requests")

        // Phase 2: Runtime dynamic update (simulate server manifest fetch or user setting update)
        store.update(
            ManifestBootstrapState.Ready(
                ResolvedFeatures.fromMap(
                    mapOf(
                        "clinic.booking" to true,
                        "clinic.messaging" to true
                    )
                ),
                ManifestBootstrapSource.REMOTE,
                "revision-clinic-enabled"
            )
        )

        // Phase 3: Immediate call now succeeds and produces 1 request through the same repository instance
        val postUpdateResult = clinicRepo.getTherapists()
        assertIs<AppResult.Success<*>>(postUpdateResult)
        assertEquals(1, requestCount.get(), "After dynamic store update, subsequent call must reach transport (count=1)")
    }
}
