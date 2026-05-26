package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.auth.screen.ForgotPasswordScreen
import com.kazemieh.auth.screen.LoginScreen
import com.kazemieh.auth.screen.RegisterScreen
import com.kazemieh.auth.screen.ResetPasswordScreen
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
                },
                onNavigateBack = {
                    navController.navigateBack()
                }
            )
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
                },
                onNavigateBack = {
                    navController.navigateBack()
                }
            )
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
                    navController.navigateBack()
                })
        }

        composable<Screen.ResetPassword> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.ResetPassword>()
            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = Screen.AuthGraph
            )
            ResetPasswordScreen(
                token = args.token,
                viewModel = viewModel,
                onNavigateLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.AuthGraph) { inclusive = true }
                    }
                }
            )
        }

    }
}