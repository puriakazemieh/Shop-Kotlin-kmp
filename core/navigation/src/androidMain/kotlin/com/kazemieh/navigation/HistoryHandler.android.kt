package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
actual fun BindBrowserHistory(navController: NavController) {
    // No-op for Android as NavHost handles back button integration automatically
}

actual fun getInitialDestination(): Any? = null
