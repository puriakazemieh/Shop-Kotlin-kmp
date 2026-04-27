package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object Splash

@Composable
fun AppNavHost(
//    navController: NavHostController,
    startDestination: Any = AuthGraph,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
//        composable<Splash> {
//            SplashRoute {
//                navController.navigate(Home) {
//                    popUpTo(Splash) { inclusive = true }
//                }
//            }
//        }

        authNavGraph(navController) {

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
