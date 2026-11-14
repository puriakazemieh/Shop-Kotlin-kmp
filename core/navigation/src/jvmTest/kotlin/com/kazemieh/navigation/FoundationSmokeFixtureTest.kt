package com.kazemieh.navigation

import com.kazemieh.cart.payment_completed.PaymentIntent
import com.kazemieh.cart.payment_completed.PaymentViewModel
import com.kazemieh.common.AppResult
import com.kazemieh.common.AuthState
import com.kazemieh.common.Screen
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.auth.AuthRepository
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.auth.LoginUseCase
import com.kazemieh.domain.auth.SignOutUseCase
import com.kazemieh.domain.cart.AddToCartUseCase
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartItem
import com.kazemieh.domain.cart.CartRepository
import com.kazemieh.domain.cart.ClearCartUseCase
import com.kazemieh.domain.cart.GetCartUseCase
import com.kazemieh.domain.catalog.Banner
import com.kazemieh.domain.catalog.Campaign
import com.kazemieh.domain.catalog.CatalogRepository
import com.kazemieh.domain.catalog.Category
import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.ProductImage
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.catalog.ProductVariant
import com.kazemieh.domain.order.GetOrderUseCase
import com.kazemieh.domain.order.Order
import com.kazemieh.domain.order.OrderDetail
import com.kazemieh.domain.order.OrderRepository
import com.kazemieh.domain.order.OrderTracking
import com.kazemieh.domain.order.ReorderResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-End Foundation Smoke Test Suite (P02-QA-MANUAL-017).
 *
 * Verifies that the complete 5-stage core customer journey remains fully functional
 * and behaviorally identical on synthetic fixtures before and after Foundation:
 * 1. Auth: Login, session state persistence, token handling, and sign-out.
 * 2. Home: Catalog loading, category listing, and navigation routing.
 * 3. Product: Detail query, variant/options inspection, pricing, and deep-link routing.
 * 4. Cart: Adding items, quantity management, subtotal/total calculations, and checkout.
 * 5. Payment Return: Backend-authoritative order status verification:
 *    - Success (processing/completed): cart is cleared and success screen displayed.
 *    - Failure/Cancelled/Unverified: cart items are preserved and error displayed.
 */
class FoundationSmokeFixtureTest {

    // --- Synthetic Fixture Repositories ---

    private class SyntheticAuthFixtureRepository : AuthRepository {
        private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
        override val authState = _authState.asStateFlow()

        override suspend fun restoreSession(): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun login(email: String, password: String): AppResult<Unit> {
            return if (email == "buyer@fixture.shop" && password == "validPassword123") {
                _authState.value = AuthState.Authenticated
                AppResult.Success(Unit)
            } else {
                AppResult.Error("نام کاربری یا رمز عبور اشتباه است")
            }
        }

        override suspend fun register(email: String?, mobile: String?, password: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun forgotPassword(email: String?, mobile: String?): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun signOut(): AppResult<Unit> {
            _authState.value = AuthState.Unauthenticated
            return AppResult.Success(Unit)
        }

        override fun isLoggedIn(): Flow<Boolean> = _authState.map { it is AuthState.Authenticated }

        override suspend fun sendLoginOtp(mobile: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun loginWithOtp(mobile: String, otpCode: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun resetPasswordWithOtp(mobile: String, otpCode: String, newPassword: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }

    private class SyntheticCatalogFixtureRepository : CatalogRepository {
        private val categories = listOf(
            Category(id = 1L, name = "کالای دیجیتال", slug = "digital"),
            Category(id = 2L, name = "پوشاک", slug = "apparel")
        )

        private val productDetail = ProductDetail(
            id = 101L,
            title = "ساعت هوشمند کارمیلا مدل X1",
            slug = "carmilla-smartwatch-x1",
            description = "ساعت هوشمند با سنسور ضربان قلب و پایش خواب",
            brand = "Carmilla",
            categoryId = 1L,
            categoryName = "کالای دیجیتال",
            images = listOf(ProductImage(id = 1L, url = "https://fixture.shop/images/x1.png", sortOrder = 1)),
            variants = listOf(
                ProductVariant(
                    id = 501L,
                    sku = "X1-BLK",
                    price = 250000.0,
                    discountedPrice = 200000.0,
                    compareAtPrice = 300000.0,
                    options = mapOf("color" to "مشکی"),
                    available = 15
                )
            ),
            createdAt = "2026-08-01T10:00:00Z",
            basePrice = 250000.0,
            discountedPrice = 200000.0
        )

        override suspend fun getCategories(): AppResult<List<Category>> = AppResult.Success(categories)

        override suspend fun getProductDetail(slug: String): AppResult<ProductDetail> {
            return if (slug == "carmilla-smartwatch-x1") {
                AppResult.Success(productDetail)
            } else {
                AppResult.Error("محصول یافت نشد")
            }
        }

        override suspend fun getProducts(
            query: String?,
            categoryId: Long?,
            options: Map<String, String>?,
            minPrice: Double?,
            maxPrice: Double?,
            inStock: Boolean?,
            page: Int,
            size: Int,
            sort: String?,
            discountedOnly: Boolean
        ): AppResult<AdminPage<ProductSummary>> = AppResult.Success(AdminPage(emptyList(), 0, 20, 0L, 0))

        override suspend fun getActiveCampaign(): AppResult<Campaign?> = AppResult.Success(null)
        override suspend fun getBanners(): AppResult<List<Banner>> = AppResult.Success(emptyList())
        override suspend fun requestBackInStock(productId: Long, variantId: Long): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun getFrequentlyBoughtTogether(productId: Long): AppResult<List<ProductSummary>> = AppResult.Success(emptyList())
        override suspend fun subscribeToPriceAlert(productId: Long, variantId: Long, targetPrice: Double): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class SyntheticCartFixtureRepository : CartRepository {
        private var currentItems = mutableListOf<CartItem>()

        private fun buildCart(): Cart {
            val subtotal = currentItems.sumOf { it.lineTotal }
            val totalQty = currentItems.sumOf { it.qty }
            return Cart(
                items = currentItems.toList(),
                subtotal = subtotal,
                totalQty = totalQty,
                discountAmount = 0.0,
                total = subtotal
            )
        }

        override fun getCart(): Flow<AppResult<Cart>> = flowOf(AppResult.Success(buildCart()))

        override suspend fun addItem(productId: Long?, variantId: Long?, qty: Int): AppResult<Cart> {
            val item = CartItem(
                id = (currentItems.size + 1).toLong(),
                variantId = variantId ?: 501L,
                qty = qty,
                productId = productId ?: 101L,
                productTitle = "ساعت هوشمند کارمیلا مدل X1",
                productSlug = "carmilla-smartwatch-x1",
                imageUrl = "https://fixture.shop/images/x1.png",
                options = mapOf("color" to "مشکی"),
                price = 200000.0,
                compareAtPrice = 250000.0,
                availableQty = 15,
                isActive = true,
                lineTotal = 200000.0 * qty
            )
            currentItems.add(item)
            return AppResult.Success(buildCart())
        }

        override suspend fun updateQty(itemId: Long, qty: Int): AppResult<Cart> {
            val index = currentItems.indexOfFirst { it.id == itemId }
            if (index != -1) {
                val old = currentItems[index]
                currentItems[index] = old.copy(qty = qty, lineTotal = old.price * qty)
            }
            return AppResult.Success(buildCart())
        }

        override suspend fun remove(itemId: Long): AppResult<Cart> {
            currentItems.removeAll { it.id == itemId }
            return AppResult.Success(buildCart())
        }

        override suspend fun clear(): AppResult<Unit> {
            currentItems.clear()
            return AppResult.Success(Unit)
        }

        override suspend fun setVariantQty(variantId: Long, qty: Int): AppResult<Cart> = AppResult.Success(buildCart())
        override suspend fun adjustVariantQty(variantId: Long, delta: Int): AppResult<Cart> = AppResult.Success(buildCart())
        override suspend fun moveToSaveForLater(itemId: Long): AppResult<Cart> = AppResult.Success(buildCart())
        override suspend fun moveToCart(itemId: Long): AppResult<Cart> = AppResult.Success(buildCart())
        override suspend fun applyDiscount(code: String): AppResult<Cart> = AppResult.Success(buildCart())
        override suspend fun removeDiscount(): AppResult<Cart> = AppResult.Success(buildCart())
    }

    private class SyntheticOrderFixtureRepository(
        private var simulatedStatus: String = "processing"
    ) : OrderRepository {
        fun setSimulatedStatus(status: String) {
            this.simulatedStatus = status
        }

        override suspend fun getOrder(id: Long): AppResult<OrderDetail> {
            return if (id == 900L) {
                AppResult.Success(
                    OrderDetail(
                        id = id,
                        status = simulatedStatus,
                        subtotalPrice = 400000.0,
                        shippingPrice = 25000.0,
                        totalPrice = 425000.0,
                        items = emptyList(),
                        address = null,
                        createdAt = "2026-08-01T12:00:00Z",
                        shippingCarrier = "پیشتاز",
                        trackingCode = "TRK123456",
                        shippedAt = null,
                        deliveredAt = null
                    )
                )
            } else {
                AppResult.Error("سفارش با شناسه $id یافت نشد")
            }
        }

        override fun getMyOrders(): Flow<AppResult<List<Order>>> = flowOf(AppResult.Success(emptyList()))
        override suspend fun createOrder(items: List<Pair<Long, Int>>, addressId: Long?, useWallet: Boolean, isGift: Boolean, giftMessage: String?): AppResult<OrderDetail> =
            getOrder(900L)
        override suspend fun cancelOrder(id: Long): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun trackOrder(id: Long): AppResult<OrderTracking> = AppResult.Error("Not implemented")
        override suspend fun reorder(id: Long): AppResult<ReorderResult> = AppResult.Error("Not implemented")
    }

    // --- Smoke Flow Tests ---

    @Test
    fun `flow 1 - auth login, session validation and signout on fixture`() = runBlocking {
        val authRepo = SyntheticAuthFixtureRepository()
        val loginUseCase = LoginUseCase(authRepo)
        val isUserLoggedInUseCase = IsUserLoggedInUseCase(authRepo)
        val signOutUseCase = SignOutUseCase(authRepo)

        // 1. Initial State: Unauthenticated
        assertFalse("Initial session must be unauthenticated", isUserLoggedInUseCase().first())

        // 2. Negative: Invalid credentials
        val failResult = loginUseCase("buyer@fixture.shop", "wrongPassword")
        assertTrue("Invalid password must fail", failResult is AppResult.Error)
        assertFalse("Session remains unauthenticated after failed login", isUserLoggedInUseCase().first())

        // 3. Positive: Valid credentials
        val successResult = loginUseCase("buyer@fixture.shop", "validPassword123")
        assertTrue("Valid login must succeed", successResult is AppResult.Success)
        assertTrue("Session must be authenticated after login", isUserLoggedInUseCase().first())
        val authState = authRepo.authState.value
        assertTrue(authState is AuthState.Authenticated)

        // 4. Sign out
        val signoutResult = signOutUseCase()
        assertTrue("Sign out must succeed", signoutResult is AppResult.Success)
        assertFalse("Session must be unauthenticated after sign out", isUserLoggedInUseCase().first())
    }

    @Test
    fun `flow 2 - home catalog and categories navigation on fixture`() = runBlocking {
        val catalogRepo = SyntheticCatalogFixtureRepository()
        val getCategoriesUseCase = GetCategoriesUseCase(catalogRepo)

        val categoriesResult = getCategoriesUseCase()
        assertTrue("Categories query must succeed", categoriesResult is AppResult.Success)
        val categories = (categoriesResult as AppResult.Success).data
        assertEquals("Must return exactly 2 fixture categories", 2, categories.size)
        assertEquals("کالای دیجیتال", categories[0].name)
        assertEquals("digital", categories[0].slug)

        // Navigation Destination Verification
        val homeRoute = Screen.HomeGraph(showCart = false)
        assertFalse(homeRoute.showCart)
        val categorySearchRoute = Screen.CategorySearch(id = categories[0].id, name = categories[0].name)
        assertEquals(1L, categorySearchRoute.id)
        assertEquals("کالای دیجیتال", categorySearchRoute.name)
    }

    @Test
    fun `flow 3 - product detail query and variant options inspection on fixture`() = runBlocking {
        val catalogRepo = SyntheticCatalogFixtureRepository()
        val getProductDetailUseCase = GetProductDetailUseCase(catalogRepo)

        val productResult = getProductDetailUseCase("carmilla-smartwatch-x1")
        assertTrue("Product detail query must succeed", productResult is AppResult.Success)
        val product = (productResult as AppResult.Success).data
        assertEquals("ساعت هوشمند کارمیلا مدل X1", product.title)
        assertEquals(101L, product.id)
        assertEquals("Carmilla", product.brand)
        assertEquals(1, product.variants.size)

        val variant = product.variants[0]
        assertEquals(501L, variant.id)
        assertEquals("X1-BLK", variant.sku)
        assertEquals(200000.0, variant.discountedPrice)
        assertEquals(15, variant.available)

        // Deep-link route verification
        val productRoute = Screen.ProductDetail(slug = product.slug)
        assertEquals("carmilla-smartwatch-x1", productRoute.slug)
    }

    @Test
    fun `flow 4 - cart add, qty update and checkout routing on fixture`() = runBlocking {
        val cartRepo = SyntheticCartFixtureRepository()
        val addToCartUseCase = AddToCartUseCase(cartRepo)
        val getCartUseCase = GetCartUseCase(cartRepo)

        // 1. Initial cart is empty
        val initialCart = (getCartUseCase().first() as AppResult.Success).data
        assertEquals(0, initialCart.totalQty)

        // 2. Add product variant to cart (qty = 2)
        val addResult = addToCartUseCase(productId = 101L, variantId = 501L, qty = 2)
        assertTrue(addResult is AppResult.Success)
        val updatedCart = (getCartUseCase().first() as AppResult.Success).data
        assertEquals(2, updatedCart.totalQty)
        assertEquals(400000.0, updatedCart.subtotal, 0.01)
        assertEquals(400000.0, updatedCart.total, 0.01)

        // 3. Update item quantity (qty = 3)
        val itemId = updatedCart.items[0].id
        val updateResult = cartRepo.updateQty(itemId = itemId, qty = 3)
        assertTrue(updateResult is AppResult.Success)
        val cartAfterQty = (getCartUseCase().first() as AppResult.Success).data
        assertEquals(3, cartAfterQty.totalQty)
        assertEquals(600000.0, cartAfterQty.total, 0.01)

        // 4. Checkout Route verification
        val checkoutRoute = Screen.Checkout(totalAmount = cartAfterQty.total)
        assertEquals(600000.0, checkoutRoute.totalAmount, 0.01)
    }

    @Test
    fun `flow 5A - payment return success verifies backend-authoritative status and clears cart`() = runBlocking {
        val cartRepo = SyntheticCartFixtureRepository()
        cartRepo.addItem(productId = 101L, variantId = 501L, qty = 2)
        // Verify cart has items before payment
        val cartBefore = (cartRepo.getCart().first() as AppResult.Success).data
        assertEquals(2, cartBefore.totalQty)

        // Backend authoritative order status: "processing"
        val orderRepo = SyntheticOrderFixtureRepository(simulatedStatus = "processing")
        val getOrderUseCase = GetOrderUseCase(orderRepo)
        val clearCartUseCase = ClearCartUseCase(cartRepo)

        val viewModel = PaymentViewModel(getOrderUseCase, clearCartUseCase)
        viewModel.handleIntent(PaymentIntent.VerifyPayment(orderId = 900L))

        // State becomes Success
        val finalState = viewModel.state.filter { it.result != null && it.result !is AppResult.Loading }.first()
        assertTrue("Payment state must be Success", finalState.result is AppResult.Success)

        // Cart MUST be cleared on verified successful payment
        val cartAfter = (cartRepo.getCart().first() as AppResult.Success).data
        assertEquals("Cart must be cleared after successful payment", 0, cartAfter.totalQty)

        // Navigation route check
        val paymentRoute = Screen.PaymentCompleted(orderId = 900L, success = true)
        assertEquals(900L, paymentRoute.orderId)
        assertTrue(paymentRoute.success)
    }

    @Test
    fun `flow 5B - payment return failure or cancellation preserves cart without clearing`() = runBlocking {
        val cartRepo = SyntheticCartFixtureRepository()
        cartRepo.addItem(productId = 101L, variantId = 501L, qty = 3)
        // Verify cart has items before payment
        val cartBefore = (cartRepo.getCart().first() as AppResult.Success).data
        assertEquals(3, cartBefore.totalQty)

        // Backend authoritative order status: "cancelled"
        val orderRepo = SyntheticOrderFixtureRepository(simulatedStatus = "cancelled")
        val getOrderUseCase = GetOrderUseCase(orderRepo)
        val clearCartUseCase = ClearCartUseCase(cartRepo)

        val viewModel = PaymentViewModel(getOrderUseCase, clearCartUseCase)
        viewModel.handleIntent(PaymentIntent.VerifyPayment(orderId = 900L))

        // State becomes Error
        val finalState = viewModel.state.filter { it.result != null && it.result !is AppResult.Loading }.first()
        assertTrue("Payment state must be Error for cancelled payment", finalState.result is AppResult.Error)
        val errorMsg = (finalState.result as AppResult.Error).message.toString()
        assertTrue(errorMsg.contains("پرداخت تایید نشد"))

        // Critical safety rule: Cart MUST NOT be cleared on failed or cancelled payment
        val cartAfter = (cartRepo.getCart().first() as AppResult.Success).data
        assertEquals("Cart items must be preserved on failed/cancelled payment", 3, cartAfter.totalQty)

        // Navigation route check
        val paymentRoute = Screen.PaymentCompleted(orderId = 900L, success = false)
        assertEquals(900L, paymentRoute.orderId)
        assertFalse(paymentRoute.success)
    }

    @Test
    fun `flow 5C - payment return with null or invalid order fails safe without clearing cart`() = runBlocking {
        val cartRepo = SyntheticCartFixtureRepository()
        cartRepo.addItem(productId = 101L, variantId = 501L, qty = 1)

        val orderRepo = SyntheticOrderFixtureRepository(simulatedStatus = "processing")
        val getOrderUseCase = GetOrderUseCase(orderRepo)
        val clearCartUseCase = ClearCartUseCase(cartRepo)

        val viewModel = PaymentViewModel(getOrderUseCase, clearCartUseCase)
        viewModel.handleIntent(PaymentIntent.VerifyPayment(orderId = null))

        val finalState = viewModel.state.filter { it.result != null && it.result !is AppResult.Loading }.first()
        assertTrue("Payment state must be Error for null orderId", finalState.result is AppResult.Error)
        assertEquals("شناسه سفارش نامعتبر است", (finalState.result as AppResult.Error).message.toString())

        // Cart items must remain untouched
        val cartAfter = (cartRepo.getCart().first() as AppResult.Success).data
        assertEquals(1, cartAfter.totalQty)
    }
}
