package com.kazemieh.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize

@Composable
fun CustomDrawer(
    isLoggedIn: Boolean,
    isAdmin: Boolean = false,
//    customer: RequestState<Customer>,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "NUTRISPORT",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = AppFont(),
            fontSize = FontSize.EXTRA_LARGE
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Healthy Lifestyle",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = FontSize.REGULAR
        )
        Spacer(modifier = Modifier.height(50.dp))
        if (isLoggedIn) {
            DrawerItemCard(
                drawerItem = DrawerItem.Profile,
                onClick = onProfileClick
            )
        } else {
            DrawerItemCard(
                drawerItem = DrawerItem.Login,
                onClick = onLoginClick
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        DrawerItem.entries
            .filter { item ->
                when (item) {
                    DrawerItem.Profile -> false
                    DrawerItem.Login -> false
                    DrawerItem.Admin -> false
                    DrawerItem.SignOut -> isLoggedIn
                    else -> true
                }
            }
            .forEach { item ->
            DrawerItemCard(
                drawerItem = item,
                onClick = {
                    when (item) {
                        DrawerItem.Contact -> onContactUsClick()
                        DrawerItem.SignOut -> onSignOutClick()
                        else -> {}
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        AnimatedContent(targetState = isAdmin) { isAdmin ->
            if (isAdmin) {
                DrawerItemCard(
                    drawerItem = DrawerItem.Admin,
                    onClick = onAdminPanelClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}