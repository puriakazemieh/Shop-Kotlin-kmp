package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.auth.screen.ForgotPasswordScreen
import com.kazemieh.auth.screen.LoginScreen
import com.kazemieh.auth.screen.RegisterScreen
import com.kazemieh.common.Screen


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    navigation<Screen.AuthGraph>(startDestination = Screen.Login) {

        composable<Screen.Login> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = Screen.AuthGraph
            )
            LoginScreen(
                viewModel = viewModel,
                onNavigateRegister = {
                    navController.navigate(Screen.Register)
                },
                onNavigateForgot = {
                    navController.navigate(Screen.ForgotPassword)
                })
        }

        composable<Screen.Register> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = Screen.AuthGraph
            )
            RegisterScreen(
                viewModel = viewModel,
                onNavigateLogin = {
                    navController.navigate(Screen.Login)
                })
        }

        composable<Screen.ForgotPassword> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = Screen.AuthGraph
            )
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                })
        }

    }
}