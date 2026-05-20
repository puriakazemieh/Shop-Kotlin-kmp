package com.kazemieh.home.component

import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class DrawerItem(
    val title: StringResource,
    val icon: DrawableResource
) {
    Login(
        Resources.String.Login,
        Resources.Icon.SignOut // todo change by login icon
    ),
    Profile(
        title = Resources.String.Profile,
        icon = Resources.Icon.Person
    ),
    Blog(
        title = Resources.String.Blog,
        icon = Resources.Icon.Book
    ),
    Locations(
        title = Resources.String.Locations,
        icon = Resources.Icon.MapPin
    ),
    Contact(
        title = Resources.String.ContactUs,
        icon = Resources.Icon.Edit
    ),
    SignOut(
        title = Resources.String.SignOut,
        icon = Resources.Icon.SignOut
    ),
    Admin(
        title = Resources.String.AdminPanel,
        icon = Resources.Icon.Unlock
    )
}
