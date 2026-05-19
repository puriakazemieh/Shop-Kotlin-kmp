package com.kazemieh.common

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object AuthGraph: Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    @Serializable
    data object ForgotPassword : Screen()

    @Serializable
    data class HomeGraph(val showCart: Boolean = false) : Screen()

    @Serializable
    data object ProductsOverview : Screen()

    @Serializable
    data object Cart : Screen()

    @Serializable
    data object Categories : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object AdminPanel : Screen()

    @Serializable
    data class ManageProduct(val id: Long? = null) : Screen()

    @Serializable
    data object ManageOrders : Screen()

    @Serializable
    data class Checkout(val totalAmount: Double) : Screen()

    @Serializable
    data class PaymentCompleted(val success: Boolean, val error: String? = null) : Screen()

    @Serializable
    data class CategorySearch(val id: Long, val name: String) : Screen()

    @Serializable
    data class ProductDetail(val slug: String) : Screen()

}
