package com.kazemieh.main.component

import com.kazemieh.common.Screen
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class BottomBarDestination(
    val icon: DrawableResource,
    val title: StringResource,
    val screen: Screen
) {
    ProductsOverview(
        icon = Resources.Icon.Home,
        title = Resources.String.Home,
        screen = Screen.ProductsOverview
    ),
    Cart(
        icon = Resources.Icon.ShoppingCart,
        title = Resources.String.Cart,
        screen = Screen.Cart
    ),
    Categories(
        icon = Resources.Icon.Categories,
        title = Resources.String.Categories,
        screen = Screen.Categories
    )
}
