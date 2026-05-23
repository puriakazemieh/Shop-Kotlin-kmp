package com.kazemieh.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kazemieh.common.Screen
import com.kazemieh.designsystem.Alpha
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.home.cart.CartScreen
import com.kazemieh.home.category.CategoriesScreen
import com.kazemieh.home.component.BottomBar
import com.kazemieh.home.component.BottomBarDestination
import com.kazemieh.home.component.CustomDrawer
import com.kazemieh.home.component.CustomDrawerState
import com.kazemieh.home.component.isOpened
import com.kazemieh.home.component.opposite
import com.kazemieh.home.productsOverview.ProductsOverviewScreen
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeGraphScreen(
    showCart: Boolean = false,
    viewModel: HomeGraphViewModel = koinViewModel(),
    navigateToAuth: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToContactUs: () -> Unit,
    navigateToAdminPanel: () -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (Long, String) -> Unit,
    navigateToCheckout: (Double) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()
    
    // Switch to cart if needed when screen is loaded
    LaunchedEffect(showCart) {
        if (showCart) {
            navController.navigate(Screen.Cart) {
                popUpTo(Screen.ProductsOverview) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val currentRoute = navController.currentBackStackEntryAsState()
    val selectedDestination by remember {
        derivedStateOf {
            val route = currentRoute.value?.destination?.route.toString()
            when {
                route.contains(BottomBarDestination.ProductsOverview.screen.toString()) -> BottomBarDestination.ProductsOverview
                route.contains(BottomBarDestination.Cart.screen.toString()) -> BottomBarDestination.Cart
                route.contains(BottomBarDestination.Categories.screen.toString()) -> BottomBarDestination.Categories
                else -> BottomBarDestination.ProductsOverview
            }
        }
    }

    @Composable
    fun rememberScreenWidth(): Dp {
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current
        return with(density) { windowInfo.containerSize.width.toDp() }
    }

    val screenWidth = rememberScreenWidth()
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }

    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val drawerWidth = 280.dp

    val offsetValue = if (isRtl) -drawerWidth else drawerWidth

    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp
    )

    val animatedBackground by animateColorAsState(
        targetValue = if (drawerState.isOpened()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f
    )

    val animatedRadius by animateDpAsState(
        targetValue = if (drawerState.isOpened()) 20.dp else 0.dp
    )

//    val customer by viewModel.customer.collectAsState()
//    val totalAmount by viewModel.totalAmountFlow.collectAsState(RequestState.Loading)
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeEffect.NavigateToAuth -> navigateToAuth()
                is HomeEffect.ShowError -> {
                    effect.message.let { messageBarState.addError(it) }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackground)
            .systemBarsPadding()
    ) {
        CustomDrawer(
//            customer = customer,
            onProfileClick = navigateToProfile,
            onContactUsClick = navigateToContactUs,
            onSettingsClick = navigateToSettings,
            onSignOutClick = {
//                viewModel.signOut(
//                    onSuccess = navigateToAuth,
//                    onError = { message -> messageBarState.addError(message) }
//                )
            },
            onAdminPanelClick = navigateToAdminPanel,
            isLoggedIn = state.isLoggedIn,
            isAdmin = state.isAdmin,
            onLoginClick = navigateToAuth
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(size = animatedRadius))
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = animatedOffset.toPx()
                    transformOrigin = TransformOrigin(if (isRtl) 0f else 1f, 0.5f)
                }
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(size = animatedRadius),
                    ambientColor = Color.Black.copy(alpha = Alpha.DISABLED),
                    spotColor = Color.Black.copy(alpha = Alpha.DISABLED)
                )
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            AnimatedContent(
                                targetState = selectedDestination
                            ) { destination ->
                                Text(
                                    text = stringResource(destination.title),
                                    fontFamily = AppFont(),
                                    fontSize = FontSize.LARGE,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        actions = {
//                            AnimatedVisibility(
//                                visible = selectedDestination == BottomBarDestination.Cart
//                            ) {
//                                if (customer.isSuccess() && customer.getSuccessData().cart.isNotEmpty()) {
//                                    IconButton(onClick = {
//                                        if (totalAmount.isSuccess()) {
//                                            navigateToCheckout(
//                                                totalAmount.getSuccessData().toString()
//                                            )
//                                        } else if (totalAmount.isError()) {
//                                            messageBarState.addError("Error while calculating a total amount: ${totalAmount.getErrorMessage()}")
//                                        }
//                                    }) {
//                                        Icon(
//                                            painter = painterResource(Resources.Icon.RightArrow),
//                                            contentDescription = "Right icon",
//                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                                        )
//                                    }
//                                }
//                            }
                        },
                        navigationIcon = {
                            AnimatedContent(
                                targetState = drawerState
                            ) { drawer ->
                                if (drawer.isOpened()) {
                                    IconButton(onClick = { drawerState = drawerState.opposite() }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Close),
                                            contentDescription = stringResource(Resources.String.CloseIconDesc),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { drawerState = drawerState.opposite() }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Menu),
                                            contentDescription = stringResource(Resources.String.MenuDesc),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { padding ->
                ContentWithMessageBar(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        ),
                    messageBarState = messageBarState,
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        AnimatedContent(
                            modifier = Modifier.weight(1f),
                            targetState = selectedDestination
                        ) {
                            NavHost(
                                modifier = Modifier.weight(1f),
                                navController = navController,
                                startDestination = Screen.ProductsOverview
                            ) {
                                composable<Screen.ProductsOverview> {
                                    ProductsOverviewScreen(navigateToDetails = navigateToDetails)
                                }
                                composable<Screen.Cart> {
                                    CartScreen(navigateToCheckout=navigateToCheckout)
                                }
                                composable<Screen.Categories> {
                                    CategoriesScreen(navigateToCategorySearch = navigateToCategorySearch)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .padding(all = 12.dp)
                        ) {
                            BottomBar(
                                cartItemCount = state.cartItemCount,
                                selected = selectedDestination,
                                onSelect = { destination ->
                                    navController.navigate(destination.screen) {
                                        launchSingleTop = true
                                        popUpTo<Screen.ProductsOverview> {
                                            saveState = true
                                            inclusive = false
                                        }
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}