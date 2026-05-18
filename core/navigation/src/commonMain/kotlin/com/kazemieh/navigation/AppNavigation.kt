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
import com.kazemieh.admin_panel.AdminPanelScreen
import com.kazemieh.admin_panel.manage_product.ManageProductScreen
import com.kazemieh.common.AuthState
import com.kazemieh.common.Screen
import com.kazemieh.common.TokenExpiredEventBus
import com.kazemieh.details.DetailsScreen
import com.kazemieh.home.HomeGraphScreen
import com.kazemieh.home.category.CategorySearchScreen
import com.kazemieh.home.cart.checkout.CheckoutScreen
import com.kazemieh.profile.ProfileScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    startDestination: Any = Screen.HomeGraph(),
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    LaunchedEffect(true) {
        TokenExpiredEventBus.events.collect { authState ->
            if (authState is AuthState.Unauthenticated) {
                navController.navigate(Screen.AuthGraph)
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
            HomeGraphScreen(
                showCart = args.showCart,
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile)
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
            ProfileScreen {
                navController.navigateUp()
            }
        }

        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = { navController.navigateUp() },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id))
                }
            )
        }

        composable<Screen.ManageProduct> {
            val args = it.toRoute<Screen.ManageProduct>()
            ManageProductScreen(
                id = args.id,
                navigateBack = { navController.navigateUp() }
            )
        }

        composable<Screen.ProductDetail> {
            val args = it.toRoute<Screen.ProductDetail>()
            DetailsScreen(
                slug = args.slug,
                navigateBack = { navController.navigateUp() },
                navigateToCart = {
                    navController.navigate(Screen.HomeGraph(showCart = true)) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
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
                navigateBack = { navController.navigateUp() }
            )
        }

        composable<Screen.Checkout> {
            val args = it.toRoute<Screen.Checkout>()
            CheckoutScreen(
                totalAmount = args.totalAmount,
                navigateBack = { navController.navigateUp() },
                navigateToPaymentCompleted = { orderId, status -> }
            )
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
