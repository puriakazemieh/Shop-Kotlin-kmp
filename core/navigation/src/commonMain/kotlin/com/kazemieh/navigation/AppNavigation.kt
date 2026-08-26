package com.kazemieh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kazemieh.admin.products.AdminPanelScreen
import com.kazemieh.admin.options.ManageOptionsScreen
import com.kazemieh.admin.orders.AdminOrderScreen
import com.kazemieh.admin.products.ManageProductScreen
import com.kazemieh.admin.products.AdminDiscountsScreen
import com.kazemieh.admin.story.AdminStoryScreen
import com.kazemieh.admin.wallet.AdminWalletScreen
import com.kazemieh.admin.wallet.AdminWithdrawalsScreen
import com.kazemieh.admin.blog.AdminBlogListScreen
import com.kazemieh.admin.blog.ManageBlogScreen
import com.kazemieh.blog.BlogListScreen
import com.kazemieh.blog.BlogDetailScreen
import com.kazemieh.common.AuthState
import com.kazemieh.common.PaymentEventBus
import com.kazemieh.common.Screen
import com.kazemieh.common.TokenExpiredEventBus
import com.kazemieh.details.DetailsScreen
import com.kazemieh.main.MainGraphScreen
import com.kazemieh.support.ContactUsScreen
import com.kazemieh.catalog.CategorySearchScreen
import com.kazemieh.cart.checkout.CheckoutScreen
import com.kazemieh.cart.payment_completed.PaymentCompleted
import com.kazemieh.profile.WalletScreen
import com.kazemieh.profile.ReferralScreen
import com.kazemieh.profile.MembershipScreen
import com.kazemieh.settings.SettingsScreen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.profile.FavoritesScreen
import com.kazemieh.profile.club.CustomerClubScreen
import com.kazemieh.orders.list.OrderListScreen
import com.kazemieh.orders.detail.OrderDetailScreen
import com.kazemieh.orders.tracking.OrderTrackingScreen
import com.kazemieh.orders.returns.ReturnRequestScreen
import com.kazemieh.orders.recurring.RecurringOrdersScreen
import com.kazemieh.academy.list.CourseListScreen
import com.kazemieh.academy.detail.CourseDetailScreen
import com.kazemieh.academy.learn.CourseLearnScreen
import com.kazemieh.academy.quiz.CourseQuizScreen
import com.kazemieh.academy.cert.CertificatesScreen
import com.kazemieh.academy.courserequest.CourseRequestScreen
import com.kazemieh.academy.lessonquiz.LessonQuizScreen
import com.kazemieh.academy.project.ProjectSubmissionScreen
import com.kazemieh.academy.peerreview.PeerReviewScreen
import com.kazemieh.academy.cert.CertificateVerifyScreen
import com.kazemieh.academy.placement.PlacementQuizScreen
import com.kazemieh.catalog.bundle.BundleListScreen
import com.kazemieh.catalog.bundle.BundleDetailScreen
import com.kazemieh.catalog.assistant.ShoppingAssistantScreen
import com.kazemieh.psychtest.list.PsychTestListScreen
import com.kazemieh.psychtest.take.TakeTestScreen
import com.kazemieh.comparison.ComparisonScreen
import com.kazemieh.clinic.list.TherapistListScreen
import com.kazemieh.clinic.detail.TherapistDetailScreen
import com.kazemieh.clinic.appointments.MyAppointmentsScreen
import com.kazemieh.clinic.mood.MoodCheckInScreen
import com.kazemieh.clinic.resources.EmergencyResourcesScreen
import com.kazemieh.clinic.receipt.SessionReceiptScreen
import com.kazemieh.clinic.messaging.MessagingScreen
import com.kazemieh.clinic.homework.HomeworkScreen
import com.kazemieh.clinic.journal.JournalScreen
import com.kazemieh.clinic.match.TherapistMatchScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    startDestination: Any = getInitialDestination() ?: Screen.HomeGraph(),
    routeGuard: FeatureRouteGuard? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    BindBrowserHistory(navController)

    DisposableEffect(navController, routeGuard) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener
            if (routeGuard?.checkRoute(route) is RouteGuardDecision.Blocked) {
                controller.navigate(Screen.HomeGraph()) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    LaunchedEffect(true) {
        TokenExpiredEventBus.events.collect { authState ->
            if (authState is AuthState.Unauthenticated) {
                navController.navigate(Screen.AuthGraph)
            }
        }
    }

    LaunchedEffect(Unit) {
        PaymentEventBus.events.collect { result ->
            val token = result.token
            if (token != null) {
                navController.navigate(Screen.PaymentCompleted(orderId = token.toLongOrNull(), success = true)) {
                    popUpTo<Screen.Checkout> { inclusive = true }
                }
                PaymentEventBus.reset()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        authNavGraph(navController)
        adminNavGraph(navController)
        academyNavGraph(navController)
        clinicNavGraph(navController)
        psychTestNavGraph(navController)
        ordersNavGraph(navController)
        profileNavGraph(navController)
        catalogNavGraph(navController)

        composable<Screen.HomeGraph> {
            val args = it.toRoute<Screen.HomeGraph>()
            MainGraphScreen(
                showCart = args.showCart,
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                navigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                navigateToContactUs = {
                    navController.navigate(Screen.ContactUs)
                },
                navigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel)
                },
                navigateToBlog = {
                    navController.navigate(Screen.BlogList)
                },
                navigateToBlogDetail = { slug ->
                    navController.navigate(Screen.BlogDetail(slug))
                },
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateToCategorySearch = { categoryId, categoryName ->
                    navController.navigate(Screen.CategorySearch(id = categoryId, name = categoryName))
                },
                navigateToCheckout = { totalAmount ->
                    navController.navigate(Screen.Checkout(totalAmount))
                },
                navigateToMyOrders = {
                    navController.navigate(Screen.MyOrders)
                },
                navigateToWallet = {
                    navController.navigate(Screen.Wallet)
                },
                navigateToFavorites = {
                    navController.navigate(Screen.Favorites)
                },
                navigateToCustomerClub = {
                    navController.navigate(Screen.CustomerClub)
                },
                navigateToMyCourses = {
                    navController.navigate(Screen.MyCourses)
                },
                navigateToCourseCatalog = {
                    navController.navigate(Screen.CourseCatalog)
                },
                navigateToCertificates = {
                    navController.navigate(Screen.Certificates)
                },
                navigateToMyAppointments = {
                    navController.navigate(Screen.MyAppointments)
                },
                navigateToTherapistCatalog = {
                    navController.navigate(Screen.TherapistCatalog)
                },
                navigateToPsychTests = {
                    navController.navigate(Screen.PsychTestCatalog)
                },
                navigateToComparison = {
                    navController.navigate(Screen.Comparison)
                },
                navigateToFreeCourses = {
                    navController.navigate(Screen.FreeCourses)
                },
                navigateToBundles = {
                    navController.navigate(Screen.BundleList)
                },
                navigateToReferral = {
                    navController.navigate(Screen.Referral)
                },
                navigateToRecurringOrders = {
                    navController.navigate(Screen.RecurringOrders)
                },
                navigateToMembership = {
                    navController.navigate(Screen.Membership)
                },
                navigateToShoppingAssistant = {
                    navController.navigate(Screen.ShoppingAssistant)
                },
                navigateToCourseDetail = { slug ->
                    navController.navigate(Screen.CourseDetail(slug))
                },
                navigateToTherapistDetail = { slug ->
                    navController.navigate(Screen.TherapistDetail(slug))
                },
                navigateToPlacementQuiz = {
                    navController.navigate(Screen.PlacementQuiz)
                },
                navigateToCertificateVerify = {
                    navController.navigate(Screen.CertificateVerify)
                },
                navigateToHomework = {
                    navController.navigate(Screen.Homework)
                },
                navigateToJournal = {
                    navController.navigate(Screen.Journal)
                },
                navigateToTherapistMatch = {
                    navController.navigate(Screen.TherapistMatch)
                },
            )
        }






        // ---- Academy (vertical) ----










        // ---- تاییدِ عمومیِ گواهی (بدونِ نیازِ ورود) ----

        // ---- آزمونِ تعیینِ سطح ----




        // ---- Product bundles (general shop feature) ----


        // ---- Clinic (vertical) ----










        // ---- Psychology tests (vertical) ----


        // ---- Product comparison ----



























    }
}


fun NavController.navigateBack() {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        navigate(Screen.HomeGraph()) {
            popUpTo<Screen.HomeGraph> { inclusive = true }
        }
    }
}


@Composable
inline fun <reified VM : ViewModel, reified T : Any> sharedViewModel(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
    navGraph: T
): VM {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(navGraph)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}
