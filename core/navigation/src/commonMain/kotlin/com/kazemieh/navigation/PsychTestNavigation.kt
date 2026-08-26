package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
import com.kazemieh.psychtest.list.PsychTestListScreen
import com.kazemieh.psychtest.take.TakeTestScreen

fun NavGraphBuilder.psychTestNavGraph(navController: NavController) {
        composable<Screen.PsychTestCatalog> {
            PsychTestListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToProduct = { productSlug -> navController.navigate(Screen.ProductDetail(slug = productSlug)) },
                navigateToTakeTest = { userTestId -> navController.navigate(Screen.TakeTest(userTestId)) }
            )
        }

        composable<Screen.TakeTest> {
            val args = it.toRoute<Screen.TakeTest>()
            TakeTestScreen(
                userTestId = args.userTestId,
                navigateBack = { navController.navigateBack() }
            )
        }

}
