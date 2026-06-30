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
    Search(
        icon = Resources.Icon.Search,
        title = Resources.String.SearchTab,
        screen = Screen.Search
    ),
    Cart(
        icon = Resources.Icon.ShoppingCart,
        title = Resources.String.Cart,
        screen = Screen.Cart
    ),
    More(
        icon = Resources.Icon.Menu,
        title = Resources.String.Profile,
        screen = Screen.Categories
    )
}
