//package com.kazemieh.auth
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import com.kazemieh.auth.screen.ForgotPasswordScreen
//import com.kazemieh.auth.screen.LoginScreen
//import com.kazemieh.auth.screen.RegisterScreen
//
//@Composable
//fun AuthRoot() {
//    var screen by remember { mutableStateOf<AuthScreen>(AuthScreen.Login) }
//
//    when (screen) {
//        AuthScreen.Login -> LoginScreen(
//            onLogin = { _, _ -> },
//            onNavigateRegister = { screen = AuthScreen.Register },
//            onNavigateForgot = { screen = AuthScreen.ForgotPassword }
//        )
//
//        AuthScreen.Register -> RegisterScreen(
//            onRegister = { _, _ -> },
//            onNavigateLogin = { screen = AuthScreen.Login }
//        )
//
//        AuthScreen.ForgotPassword -> ForgotPasswordScreen(
//            onSubmit = {},
//            onBack = { screen = AuthScreen.Login }
//        )
//    }
//}
//
//sealed class AuthRoute(val route: String) {
//    object Login : AuthRoute("login")
//    object Register : AuthRoute("register")
//    object Forgot : AuthRoute("forgot")
//}