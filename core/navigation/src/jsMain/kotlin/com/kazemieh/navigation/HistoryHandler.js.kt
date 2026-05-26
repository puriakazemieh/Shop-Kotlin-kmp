package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import kotlinx.browser.window

@Composable
actual fun BindBrowserHistory(navController: NavController) {
    DisposableEffect(navController) {
        val onPopState = { _: Any ->
            navController.popBackStack()
            Unit
        }
        
        window.addEventListener("popstate", onPopState)
        
        onDispose {
            window.removeEventListener("popstate", onPopState)
        }
    }
}
