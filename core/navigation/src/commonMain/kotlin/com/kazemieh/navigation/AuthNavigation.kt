package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.auth.screen.ForgotPasswordScreen
import com.kazemieh.auth.screen.LoginScreen
import com.kazemieh.auth.screen.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

@Serializable
object Login

@Serializable
object Register

@Serializable
object ForgotPassword


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    navigation<AuthGraph>(startDestination = Login) {

        composable<Login> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = AuthGraph
            )
            LoginScreen(
                viewModel = viewModel,
                onNavigateRegister = {
                    navController.navigate(Register)
                },
                onNavigateForgot = {
                    navController.navigate(ForgotPassword)
                })
        }

        composable<Register> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = AuthGraph
            )
            RegisterScreen(
                viewModel = viewModel,
                onNavigateLogin = {
                    navController.navigate(Login)
                })
        }

        composable<ForgotPassword> { backStackEntry ->

            val viewModel: AuthViewModel = sharedViewModel(
                navController = navController,
                backStackEntry = backStackEntry,
                navGraph = AuthGraph
            )
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                })
        }

    }
}