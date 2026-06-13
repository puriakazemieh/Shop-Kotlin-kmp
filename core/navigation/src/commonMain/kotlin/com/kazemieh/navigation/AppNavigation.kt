package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kazemieh.admin.products.AdminPanelScreen
import com.kazemieh.admin.options.ManageOptionsScreen
import com.kazemieh.admin.orders.AdminOrderScreen
import com.kazemieh.admin.products.ManageProductScreen
import com.kazemieh.admin.products.AdminDiscountsScreen
import com.kazemieh.common.AuthState
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.Screen
import com.kazemieh.common.TokenExpiredEventBus
import com.kazemieh.details.DetailsScreen
import com.kazemieh.main.MainGraphScreen
import com.kazemieh.support.ContactUsScreen
import com.kazemieh.catalog.CategorySearchScreen
import com.kazemieh.cart.checkout.CheckoutScreen
import com.kazemieh.cart.payment_completed.PaymentCompleted
import com.kazemieh.settings.SettingsScreen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.orders.list.OrderListScreen
import com.kazemieh.orders.detail.OrderDetailScreen
import com.kazemieh.orders.tracking.OrderTrackingScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    startDestination: Any = getInitialDestination() ?: Screen.HomeGraph(),
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    BindBrowserHistory(navController)

    LaunchedEffect(true) {
        TokenExpiredEventBus.events.collect { authState ->
            if (authState is AuthState.Unauthenticated) {
                navController.navigate(Screen.AuthGraph)
            }
        }
    }

    LaunchedEffect(true) {
        PaymentEventBus.events.collect { result ->
            if (result.status == "success" || result.status == "failed") {
                val success = result.status == "success"
                PaymentEventBus.reset()
                navController.navigate(Screen.PaymentCompleted(success)) {
                    popUpTo<Screen.Checkout> { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        authNavGraph(navController)

        composable<Screen.HomeGraph> {
            val args = it.toRoute<Screen.HomeGraph>()
            MainGraphScreen(
                showCart = args.showCart,
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                navigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                navigateToContactUs = {
                    navController.navigate(Screen.ContactUs)
                },
                navigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel)
                },
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateToCategorySearch = { categoryId, categoryName ->
                    navController.navigate(Screen.CategorySearch(id = categoryId, name = categoryName))
                },
                navigateToCheckout = { totalAmount ->
                    navController.navigate(Screen.Checkout(totalAmount))
                },
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                navigateBack = {
                    navController.navigateBack()
                },
                navigateToMyOrders = {
                    navController.navigate(Screen.MyOrders)
                }
            )
        }

        composable<Screen.MyOrders> {
            OrderListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { id: Long ->
                    navController.navigate(Screen.OrderDetail(id))
                }
            )
        }

        composable<Screen.OrderDetail> {
            val args = it.toRoute<Screen.OrderDetail>()
            OrderDetailScreen(
                orderId = args.id,
                navigateBack = { navController.navigateBack() },
                navigateToTracking = { id: Long ->
                    navController.navigate(Screen.OrderTracking(id))
                }
            )
        }

        composable<Screen.OrderTracking> {
            val args = it.toRoute<Screen.OrderTracking>()
            OrderTrackingScreen(
                orderId = args.id,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen {
                navController.navigateBack()
            }
        }

        composable<Screen.ContactUs> {
            ContactUsScreen {
                navController.navigateBack()
            }
        }

        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = { navController.navigateBack() },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id))
                },
                navigateToManageOrders = {
                    navController.navigate(Screen.ManageOrders)
                },
                navigateToManageOptions = {
                    navController.navigate(Screen.ManageOptions)
                },
                navigateToManageDiscounts = {
                    navController.navigate(Screen.ManageDiscounts)
                }
            )
        }

        composable<Screen.ManageOrders> {
            AdminOrderScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageOptions> {
            ManageOptionsScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageProduct> {
            val args = it.toRoute<Screen.ManageProduct>()
            ManageProductScreen(
                id = args.id,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageDiscounts> {
            AdminDiscountsScreen(
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ProductDetail> {
            val args = it.toRoute<Screen.ProductDetail>()
            DetailsScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToCart = {
                    navController.navigate(Screen.HomeGraph(showCart = true)) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                },
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                }
            )
        }

        composable<Screen.CategorySearch> {
            val args = it.toRoute<Screen.CategorySearch>()
            CategorySearchScreen(
                categoryId = args.id,
                categoryName = args.name,
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.Checkout> {
            val args = it.toRoute<Screen.Checkout>()
            CheckoutScreen(
                totalAmount = args.totalAmount,
                navigateBack = { navController.navigateBack() },
                navigateToPaymentCompleted = { success, error ->
                    navController.navigate(Screen.PaymentCompleted(success ?: false, error)) {
                        popUpTo<Screen.Checkout> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.PaymentCompleted> {
            val args = it.toRoute<Screen.PaymentCompleted>()
            
            // If we came from a deep link, the args might be different or not populated correctly 
            // because of naming mismatch between Screen.PaymentCompleted and the deep link params.
            // But since we are using Type-Safe Navigation, it might be tricky.
            
            // Actually, if we use type-safe navigation, the deep link uri pattern should match the route structure.
            // Screen.PaymentCompleted(success: Boolean, error: String?)

            PaymentCompleted(
                navigateBack = {
                    navController.navigate(Screen.HomeGraph()) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                }
            )
        }


    }
}


fun NavController.navigateBack() {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        navigate(Screen.HomeGraph()) {
            popUpTo<Screen.HomeGraph> { inclusive = true }
        }
    }
}


@Composable
inline fun <reified VM : ViewModel, reified T : Any> sharedViewModel(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    navGraph: T
): VM {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(navGraph)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}
