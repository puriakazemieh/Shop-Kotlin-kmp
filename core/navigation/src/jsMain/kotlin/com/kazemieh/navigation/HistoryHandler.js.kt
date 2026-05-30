package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams

@Composable
actual fun BindBrowserHistory(navController: NavController) {
    // Listen for NavController changes and update the browser URL
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            val currentPath = window.location.pathname
            val newPath = mapRouteToPath(entry)
            
            // Only push if the path actually changed to avoid infinite loops or redundant entries
            if (currentPath != newPath) {
                window.history.pushState(null, "", newPath)
            }
        }
    }

    // Listen for Browser Back/Forward buttons
    DisposableEffect(navController) {
        val onPopState: (Event) -> Unit = {
            // This is a simple implementation. In a more complex app, 
            // you might want to check if the NavController can actually pop.
            navController.popBackStack()
        }
        
        window.addEventListener("popstate", onPopState)
        
        onDispose {
            window.removeEventListener("popstate", onPopState)
        }
    }
}

actual fun getInitialDestination(): Any? {
    val path = window.location.pathname
    val params = URLSearchParams(window.location.search)
    return when {
        path == "/" || path == "" -> {
            val showCart = params.get("cart") == "true"
            Screen.HomeGraph(showCart)
        }
        path == "/login" -> Screen.Login
        path == "/register" -> Screen.Register
        path == "/forgot-password" -> Screen.ForgotPassword
        path == "/reset-password" -> {
            val token = params.get("token") ?: ""
            Screen.ResetPassword(token)
        }
        path == "/profile" -> Screen.Profile
        path == "/settings" -> Screen.Settings
        path == "/admin" -> Screen.AdminPanel
        path == "/admin/products" -> Screen.AdminPanel
        path == "/admin/orders" -> Screen.ManageOrders
        path == "/admin/options" -> Screen.ManageOptions
        path == "/checkout" -> {
            val amount = params.get("amount")?.toDoubleOrNull() ?: 0.0
            Screen.Checkout(amount)
        }
        path == "/contact" -> Screen.ContactUs
        path.startsWith("/product/") -> {
            val slug = path.substringAfter("/product/")
            Screen.ProductDetail(slug)
        }
        path == "/search" -> {
            val id = params.get("id")?.toLongOrNull() ?: 0L
            val name = params.get("name") ?: ""
            Screen.CategorySearch(id, name)
        }
        else -> null
    }
}

/**
 * Maps Navigation Routes to URL Paths.
 */
private fun mapRouteToPath(entry: NavBackStackEntry): String {
    val route = entry.destination.route ?: return "/"
    return when {
        // Main Graph / Home
        route.contains("HomeGraph") -> {
            val showCart = entry.toRoute<Screen.HomeGraph>().showCart
            if (showCart) "/?cart=true" else "/"
        }
        
        // Auth
        route.contains("AuthGraph") -> "/auth"
        route.contains("Login") -> "/login"
        route.contains("Register") -> "/register"
        route.contains("ForgotPassword") -> "/forgot-password"
        route.contains("ResetPassword") -> {
            val token = entry.toRoute<Screen.ResetPassword>().token
            if (token.isNotEmpty()) "/reset-password?token=$token" else "/reset-password"
        }
        
        // Profile & Settings
        route.contains("Profile") -> "/profile"
        route.contains("Settings") -> "/settings"
        
        // Admin
        route.contains("AdminPanel") -> "/admin"
        route.contains("ManageProduct") -> {
            val id = entry.toRoute<Screen.ManageProduct>().id
            if (id != null && id != 0L) "/admin/products/$id" else "/admin/products"
        }
        route.contains("ManageOrders") -> "/admin/orders"
        route.contains("ManageOptions") -> "/admin/options"
        
        // Shop Features
        route.contains("Checkout") -> {
            val total = entry.toRoute<Screen.Checkout>().totalAmount
            if (total != 0.0) "/checkout?amount=$total" else "/checkout"
        }
        route.contains("PaymentCompleted") -> "/payment-completed"
        route.contains("CategorySearch") -> {
            val search = entry.toRoute<Screen.CategorySearch>()
            "/search?id=${search.id}&name=${search.name}"
        }
        route.contains("ContactUs") -> "/contact"
        route.contains("ProductDetail") -> {
            val slug = entry.toRoute<Screen.ProductDetail>().slug
            "/product/$slug"
        }
        
        else -> "/"
    }
}
