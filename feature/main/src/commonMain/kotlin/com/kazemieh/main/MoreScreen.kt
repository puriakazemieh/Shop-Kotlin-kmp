package com.kazemieh.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.main.component.DrawerItem
import com.kazemieh.main.component.DrawerItemCard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoreScreen(
    isLoggedIn: Boolean,
    isAdmin: Boolean = false,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onBlogClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Image(
            modifier = Modifier.size(120.dp),
            painter = painterResource(Resources.Image.AppLogo),
            contentDescription = stringResource(Resources.String.AppLogoDesc)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Resources.String.Carmilla),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = AppFont(),
            fontSize = FontSize.EXTRA_LARGE
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Resources.String.CarmillaSlogan),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
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
                            DrawerItem.Settings -> onSettingsClick()
                            DrawerItem.SignOut -> onSignOutClick()
                            DrawerItem.Blog -> onBlogClick()
                            else -> {}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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
