package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
import com.kazemieh.cart.checkout.CheckoutScreen
import com.kazemieh.cart.payment_completed.PaymentCompleted
import com.kazemieh.orders.list.OrderListScreen
import com.kazemieh.orders.detail.OrderDetailScreen
import com.kazemieh.orders.tracking.OrderTrackingScreen
import com.kazemieh.orders.returns.ReturnRequestScreen
import com.kazemieh.orders.recurring.RecurringOrdersScreen

fun NavGraphBuilder.ordersNavGraph(navController: NavController) {
        composable<Screen.RecurringOrders> {
            RecurringOrdersScreen(onBackClick = { navController.navigateBack() })
        }

        composable<Screen.MyOrders> {
            OrderListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { id: Long ->
                    navController.navigate(Screen.OrderDetail(id))
                }
            )
        }

        composable<Screen.OrderDetail> {
            val args = it.toRoute<Screen.OrderDetail>()
            OrderDetailScreen(
                orderId = args.id,
                navigateBack = { navController.navigateBack() },
                navigateToTracking = { id: Long ->
                    navController.navigate(Screen.OrderTracking(id))
                },
                navigateToReturnRequest = { itemId: Long, title: String ->
                    navController.navigate(Screen.ReturnRequest(itemId, title))
                }
            )
        }

        composable<Screen.OrderTracking> {
            val args = it.toRoute<Screen.OrderTracking>()
            OrderTrackingScreen(
                orderId = args.id,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ReturnRequest> {
            val args = it.toRoute<Screen.ReturnRequest>()
            ReturnRequestScreen(
                orderItemId = args.orderItemId,
                itemTitle = args.itemTitle,
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.Checkout> {
            val args = it.toRoute<Screen.Checkout>()
            CheckoutScreen(
                totalAmount = args.totalAmount,
                navigateBack = { navController.navigateBack() },
                navigateToPaymentCompleted = { success, error ->
                    navController.navigate(Screen.PaymentCompleted(orderId = null, success = success ?: false, error = error)) {
                        popUpTo<Screen.Checkout> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.PaymentCompleted> {
            val args = it.toRoute<Screen.PaymentCompleted>()
            
            // If we came from a deep link, the args might be different or not populated correctly 
            // because of naming mismatch between Screen.PaymentCompleted and the deep link params.
            // But since we are using Type-Safe Navigation, it might be tricky.
            
            // Actually, if we use type-safe navigation, the deep link uri pattern should match the route structure.
            // Screen.PaymentCompleted(success: Boolean, error: String?)

            PaymentCompleted(
                orderId = args.orderId,
                navigateBack = {
                    navController.navigate(Screen.HomeGraph()) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                }
            )
        }

}
