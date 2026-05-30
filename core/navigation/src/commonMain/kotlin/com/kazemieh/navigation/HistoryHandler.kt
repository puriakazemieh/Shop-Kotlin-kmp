package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
expect fun BindBrowserHistory(navController: NavController)

expect fun getInitialDestination(): Any?
