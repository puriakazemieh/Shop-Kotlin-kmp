package com.kazemieh.data.commerce

import com.kazemieh.common.AppResult
import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.EffectiveFeatureStore
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.ManifestBootstrapSource
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import com.kazemieh.config.capabilities.ResolvedFeatures
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.data.di.dataModule
import com.kazemieh.data.di.platformModule
import com.kazemieh.data.local.TokenManager
import com.kazemieh.domain.bundle.BundleRepository
import com.kazemieh.domain.cart.CartRepository
import com.kazemieh.domain.catalog.CatalogRepository
import com.kazemieh.domain.order.OrderRepository
import com.kazemieh.domain.order.RecurringOrderRepository
import com.kazemieh.domain.order.ReturnRequestRepository
import com.kazemieh.domain.wallet.WalletRepository
import com.kazemieh.network.common.ApiConfig
import com.kazemieh.network.di.networkModule
import com.sun.net.httpserver.HttpServer
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommerceOperationalGuardTest {

    private lateinit var server: HttpServer
    private val requestCount = AtomicInteger(0)
    private val requestHeadersList = mutableListOf<Map<String, String>>()
    private val originalBaseUrl = ApiConfig.baseUrlOverride

    private val namespace = PrivateSessionNamespace(
        backendKind = BackendKind.WORDPRESS,
        tenantId = "commerce-guard-test",
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

            if (path.contains("/api/orders/999")) {
                val forbiddenBody = """{"message":"Access denied: order belongs to another customer"}"""
                exchange.responseHeaders.add("Content-Type", "application/json")
                val bytes = forbiddenBody.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(403, bytes.size.toLong())
                exchange.responseBody.write(bytes)
                exchange.close()
                return@createContext
            }

            val responseBody = when {
                path.contains("/api/cart") -> """{"items":[],"savedForLater":[],"total":0.0,"subtotal":0.0,"totalQty":0,"discountAmount":0.0}"""
                path.contains("/api/orders") || path.contains("/api/order") -> """[]"""
                path.contains("/api/wallet/balance") -> """{"balance":500.0,"userId":1}"""
                path.contains("/api/products") -> """{"items":[],"total":0,"page":0,"size":10}"""
                path.contains("/api/bundle") -> """[]"""
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
    fun `when commerce core is disabled in DI then repositories emit 0 requests to transport and fail closed`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(
            mapOf(
                "commerce.core" to false,
                "wallet" to false
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

        val cartRepo = koin.get<CartRepository>()
        val orderRepo = koin.get<OrderRepository>()
        val catalogRepo = koin.get<CatalogRepository>()
        val walletRepo = koin.get<WalletRepository>()
        val returnRepo = koin.get<ReturnRequestRepository>()
        val recurringRepo = koin.get<RecurringOrderRepository>()
        val bundleRepo = koin.get<BundleRepository>()

        // 1. Cart check
        val cartResult = cartRepo.getCart().first()
        assertIs<AppResult.Error>(cartResult)
        assertEquals(403, cartResult.code)
        assertTrue(cartResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Cart call must produce 0 network requests when commerce.core is disabled")

        // 2. Order check
        val orderResult = orderRepo.getMyOrders().first()
        assertIs<AppResult.Error>(orderResult)
        assertEquals(403, orderResult.code)
        assertTrue(orderResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Order call must produce 0 network requests when commerce.core is disabled")

        // 3. Catalog check
        val catalogResult = catalogRepo.getProducts(page = 0, size = 10)
        assertIs<AppResult.Error>(catalogResult)
        assertEquals(403, catalogResult.code)
        assertTrue(catalogResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Catalog call must produce 0 network requests when commerce.core is disabled")

        // 4. Wallet check
        val walletResult = walletRepo.getBalance()
        assertIs<AppResult.Error>(walletResult)
        assertEquals(403, walletResult.code)
        assertTrue(walletResult.message.toString().contains("wallet"))
        assertEquals(0, requestCount.get(), "Wallet call must produce 0 network requests when wallet is disabled")

        // 5. Return requests check
        val returnResult = returnRepo.listMine()
        assertIs<AppResult.Error>(returnResult)
        assertEquals(403, returnResult.code)
        assertTrue(returnResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Return request call must produce 0 network requests when commerce.core is disabled")

        // 6. Recurring order check
        val recurringResult = recurringRepo.listMine()
        assertIs<AppResult.Error>(recurringResult)
        assertEquals(403, recurringResult.code)
        assertTrue(recurringResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Recurring order call must produce 0 network requests when commerce.core is disabled")

        // 7. Bundle check
        val bundleResult = bundleRepo.listBundles()
        assertIs<AppResult.Error>(bundleResult)
        assertEquals(403, bundleResult.code)
        assertTrue(bundleResult.message.toString().contains("commerce.core"))
        assertEquals(0, requestCount.get(), "Bundle call must produce 0 network requests when commerce.core is disabled")
    }

    @Test
    fun `when commerce core is enabled then valid requests reach transport and increment request counter`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "commerce.core" to true,
                    "wallet" to true
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
        tokenManager.saveTokens("synthetic-bearer-token", "synthetic-refresh-token")

        val cartRepo = koin.get<CartRepository>()
        val orderRepo = koin.get<OrderRepository>()
        val walletRepo = koin.get<WalletRepository>()

        // 1. Cart call with feature enabled
        val cartResult = cartRepo.getCart().first { it !is AppResult.Loading }
        assertIs<AppResult.Success<*>>(cartResult)
        assertEquals(1, requestCount.get(), "Enabled commerce.core must forward request to transport (count=1)")

        // 2. Order call with feature enabled
        val orderResult = orderRepo.getMyOrders().first { it !is AppResult.Loading }
        assertIs<AppResult.Success<*>>(orderResult)
        assertEquals(2, requestCount.get(), "Enabled commerce.core must forward order request to transport (count=2)")

        // 3. Wallet call with feature enabled
        val walletResult = walletRepo.getBalance()
        assertIs<AppResult.Success<*>>(walletResult)
        assertEquals(3, requestCount.get(), "Enabled wallet must forward wallet request to transport (count=3)")
    }

    @Test
    fun `when wallet is disabled while commerce core is enabled then wallet produces 0 requests`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "commerce.core" to true,
                    "wallet" to false
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
        tokenManager.saveTokens("synthetic-bearer-token", "synthetic-refresh-token")

        val cartRepo = koin.get<CartRepository>()
        val walletRepo = koin.get<WalletRepository>()

        // Wallet is disabled: exactly 0 requests
        val walletResult = walletRepo.getBalance()
        assertIs<AppResult.Error>(walletResult)
        assertEquals(403, walletResult.code)
        assertEquals(0, requestCount.get(), "Wallet call must not reach transport when wallet feature is disabled")

        // Cart is enabled: exactly 1 request
        val cartResult = cartRepo.getCart().first { it !is AppResult.Loading }
        assertIs<AppResult.Success<*>>(cartResult)
        assertEquals(1, requestCount.get(), "Cart call must reach transport when commerce.core is enabled")
    }

    @Test
    fun `auth and ownership cannot be bypassed by enabling feature toggle`() = runBlocking {
        val store: EffectiveFeatureStore = DefaultEffectiveFeatureStore(
            ResolvedFeatures.fromMap(
                mapOf(
                    "commerce.core" to true,
                    "wallet" to true
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
        tokenManager.saveTokens("synthetic-token-42", "synthetic-refresh")

        val orderRepo = koin.get<OrderRepository>()

        // 1. Authorized call carries Bearer token to transport
        val myOrdersResult = orderRepo.getMyOrders().first { it !is AppResult.Loading }
        assertIs<AppResult.Success<*>>(myOrdersResult)
        assertEquals(1, requestCount.get())

        val lastHeaders = synchronized(requestHeadersList) { requestHeadersList.lastOrNull() }
        val authHeader = lastHeaders?.entries?.firstOrNull { it.key.equals("Authorization", ignoreCase = true) }?.value
        assertTrue(
            authHeader?.contains("Bearer synthetic-token-42") == true,
            "Enabled feature must transmit real authentication credentials; got: $authHeader"
        )

        // 2. Ownership check: even when commerce.core is enabled, accessing a forbidden resource returns server 403
        val forbiddenOrderResult = orderRepo.getOrder(999)
        assertIs<AppResult.Error>(forbiddenOrderResult)
        assertEquals(403, forbiddenOrderResult.code)
        assertEquals(2, requestCount.get())
    }

    @Test
    fun `runtime transition from disabled to enabled dynamically opens transport`() = runBlocking {
        val fallback = ResolvedFeatures.fromMap(mapOf("commerce.core" to false, "wallet" to false))
        val store = DefaultEffectiveFeatureStore(fallback)
        val guard = FeatureUseCaseGuard(store)

        val testModule = module {
            single<EffectiveFeatureStore> { store }
            single { guard }
            single { namespace }
            single<AssetUrlResolver> { testAssetUrlResolver }
        }

        val koin = createKoin(testModule)

        val cartRepo = koin.get<CartRepository>()

        // Phase 1: disabled -> 0 requests
        val result1 = cartRepo.getCart().first()
        assertIs<AppResult.Error>(result1)
        assertEquals(0, requestCount.get())

        // Phase 2: update store to enabled -> sends 1 request
        store.update(
            ManifestBootstrapState.Ready(
                ResolvedFeatures.fromMap(mapOf("commerce.core" to true, "wallet" to true)),
                ManifestBootstrapSource.REMOTE,
                "revision-commerce-enabled"
            )
        )

        val result2 = cartRepo.getCart().first { it !is AppResult.Loading }
        assertIs<AppResult.Success<*>>(result2)
        assertEquals(1, requestCount.get(), "Dynamically enabled feature must now allow network request")
    }
}
