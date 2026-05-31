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
    // Listen for NavController changes and update the browser URL using Hash
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            val currentHash = window.location.hash // Starts with #
            val newPath = try {
                mapRouteToPath(entry)
            } catch (e: Exception) {
                "/"
            }
            
            val newHash = if (newPath == "/") "" else "#$newPath"
            
            // Only push if the hash actually changed to avoid infinite loops
            if (currentHash != newHash) {
                window.history.pushState(null, "", window.location.pathname + newHash)
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
    val hash = window.location.hash
    if (hash.isEmpty() || hash == "#/") return null
    
    val fullPath = hash.substring(1) // Remove the '#'
    val path = try { js("decodeURIComponent")(fullPath) as String } catch (e: Exception) { fullPath }
    
    // Simple query param parsing from hash if needed (e.g. #/search?id=1)
    val pathPart = if (path.contains("?")) path.substringBefore("?") else path
    val queryPart = if (path.contains("?")) path.substringAfter("?") else ""
    val params = URLSearchParams(queryPart)

    return when {
        pathPart == "/login" -> Screen.Login
        pathPart == "/register" -> Screen.Register
        pathPart == "/forgot-password" -> Screen.ForgotPassword
        pathPart == "/reset-password" -> {
            val token = params.get("token") ?: ""
            Screen.ResetPassword(token)
        }
        pathPart == "/profile" -> Screen.Profile
        pathPart == "/settings" -> Screen.Settings
        pathPart == "/admin" -> Screen.AdminPanel
        pathPart == "/admin/products" -> Screen.AdminPanel
        pathPart == "/admin/orders" -> Screen.ManageOrders
        pathPart == "/admin/options" -> Screen.ManageOptions
        pathPart.startsWith("/admin/products/") -> {
            val id = pathPart.substringAfterLast("/").toLongOrNull()
            Screen.ManageProduct(id)
        }
        pathPart == "/checkout" -> {
            val amount = params.get("amount")?.toDoubleOrNull() ?: 0.0
            Screen.Checkout(amount)
        }
        pathPart == "/contact" -> Screen.ContactUs
        pathPart.startsWith("/product/") -> {
            val slug = pathPart.substringAfter("/product/")
            Screen.ProductDetail(slug)
        }
        pathPart == "/search" -> {
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
    
    // Using try-catch to prevent crashes during type-safe argument extraction
    return try {
        when {
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
    } catch (e: Exception) {
        // Fallback for screens without parameters if toRoute fails
        when {
            route.contains("Login") -> "/login"
            route.contains("Register") -> "/register"
            route.contains("AdminPanel") -> "/admin"
            else -> "/"
        }
    }
}
