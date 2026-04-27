package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kazemieh.common.Screen
import com.kazemieh.home.HomeGraphScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    startDestination: Any = Screen.HomeGraph,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        authNavGraph(navController)

        composable<Screen.HomeGraph> {

            HomeGraphScreen(
                navigateToAuth = {
//                    navController.navigate(Screen.Auth) {
//                        popUpTo<Screen.HomeGraph> { inclusive = true }
//                    }
                },
                navigateToProfile = {
//                    navController.navigate(Screen.Profile)
                },
                navigateToAdminPanel = {
//                    navController.navigate(Screen.AdminPanel)
                },
                navigateToDetails = { productId ->
//                    navController.navigate(Screen.Details(id = productId))
                },
                navigateToCategorySearch = { categoryName ->
//                    navController.navigate(Screen.CategorySearch(categoryName))
                },
                navigateToCheckout = { totalAmount ->
//                    navController.navigate(Screen.Checkout(totalAmount))
                },
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
