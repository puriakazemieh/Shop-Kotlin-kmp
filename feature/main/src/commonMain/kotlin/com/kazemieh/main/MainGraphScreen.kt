package com.kazemieh.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kazemieh.cart.CartScreen
import com.kazemieh.catalog.SearchScreen
import com.kazemieh.catalog.ProductsOverviewScreen
import com.kazemieh.common.Screen
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.main.component.BottomBar
import com.kazemieh.main.component.BottomBarDestination
import com.kazemieh.main.component.HomeTopBar
import com.kazemieh.main.component.TitleTopBar
import com.kazemieh.designsystem.brand.BrandConfig
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainGraphScreen(
    showCart: Boolean = false,
    viewModel: MainViewModel = koinViewModel(),
    navigateToAuth: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToContactUs: () -> Unit,
    navigateToAdminPanel: () -> Unit,
    navigateToBlog: () -> Unit,
    navigateToBlogDetail: (String) -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (Long, String) -> Unit,
    navigateToCheckout: (Double) -> Unit,
    navigateToMyOrders: () -> Unit,
    navigateToWallet: () -> Unit,
    navigateToFavorites: () -> Unit,
    navigateToCustomerClub: () -> Unit,
    navigateToMyCourses: () -> Unit = {},
    navigateToCourseCatalog: () -> Unit = {},
    navigateToCertificates: () -> Unit = {},
    navigateToMyAppointments: () -> Unit = {},
    navigateToTherapistCatalog: () -> Unit = {},
    navigateToPsychTests: () -> Unit = {},
    navigateToComparison: () -> Unit = {},
    navigateToFreeCourses: () -> Unit = {},
    navigateToBundles: () -> Unit = {},
    navigateToReferral: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val brand = koinInject<BrandConfig>()
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
                route.contains(BottomBarDestination.Search.screen.toString()) -> BottomBarDestination.Search
                route.contains(BottomBarDestination.Cart.screen.toString()) -> BottomBarDestination.Cart
                route.contains(BottomBarDestination.More.screen.toString()) -> BottomBarDestination.More
                else -> BottomBarDestination.ProductsOverview
            }
        }
    }

    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MainEffect.NavigateToAuth -> navigateToAuth()
                is MainEffect.ShowError -> {
                    effect.message.let { messageBarState.addError(it) }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                when (selectedDestination) {
                    BottomBarDestination.ProductsOverview -> HomeTopBar()
                    // صفحه‌ی جستجو هدر و فیلدِ جستجوی خودش را دارد
                    BottomBarDestination.Search -> {}
                    else -> TitleTopBar(title = stringResource(selectedDestination.title))
                }
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

                    NavHost(
                        modifier = Modifier.weight(1f),
                        navController = navController,
                        startDestination = Screen.ProductsOverview
                    ) {
                        composable<Screen.ProductsOverview> {
                            ProductsOverviewScreen(
                                navigateToDetails = navigateToDetails,
                                navigateToCategorySearch = navigateToCategorySearch,
                                navigateToBlogDetail = navigateToBlogDetail,
                                navigateToAuth = navigateToAuth
                            )
                        }
                        composable<Screen.Search> {
                            SearchScreen(
                                navigateToDetails = navigateToDetails,
                                navigateToAuth = navigateToAuth
                            )
                        }
                        composable<Screen.Cart> {
                            CartScreen(navigateToCheckout = navigateToCheckout)
                        }
                        composable<Screen.Categories> {
                            MoreScreen(
                                isLoggedIn = state.isLoggedIn,
                                isAdmin = state.isAdmin,
                                userName = state.userName,
                                userPhone = state.userPhone,
                                showAcademy = brand.features.academy,
                                showClinic = brand.features.clinic,
                                showPsychTests = brand.features.psychTests,
                                showComparison = brand.features.productComparison,
                                showFreeCourses = brand.features.academyFreeCoursesTab,
                                showBundles = brand.features.productBundles,
                                onLoginClick = navigateToAuth,
                                onEditProfileClick = navigateToProfile,
                                onCustomerClubClick = navigateToCustomerClub,
                                onSupportClick = navigateToContactUs,
                                onSettingsClick = navigateToSettings,
                                onAdminPanelClick = navigateToAdminPanel,
                                onMyCoursesClick = navigateToMyCourses,
                                onBrowseCoursesClick = navigateToCourseCatalog,
                                onCertificatesClick = navigateToCertificates,
                                onMyAppointmentsClick = navigateToMyAppointments,
                                onBrowseTherapistsClick = navigateToTherapistCatalog,
                                onPsychTestsClick = navigateToPsychTests,
                                onComparisonClick = navigateToComparison,
                                onFreeCoursesClick = navigateToFreeCourses,
                                onBundlesClick = navigateToBundles,
                                onReferralClick = navigateToReferral
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 12.dp),
                        contentAlignment = Alignment.Center
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
