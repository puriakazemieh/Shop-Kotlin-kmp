package com.kazemieh.navigation

import androidx.compose.runtime.Composable
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
import com.kazemieh.academy.lessonquiz.LessonQuizScreen
import com.kazemieh.academy.project.ProjectSubmissionScreen
import com.kazemieh.catalog.bundle.BundleListScreen
import com.kazemieh.catalog.bundle.BundleDetailScreen
import com.kazemieh.psychtest.list.PsychTestListScreen
import com.kazemieh.psychtest.take.TakeTestScreen
import com.kazemieh.comparison.ComparisonScreen
import com.kazemieh.clinic.list.TherapistListScreen
import com.kazemieh.clinic.detail.TherapistDetailScreen
import com.kazemieh.clinic.appointments.MyAppointmentsScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    startDestination: Any = getInitialDestination() ?: Screen.HomeGraph(),
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    BindBrowserHistory(navController)

    LaunchedEffect(true) {
        TokenExpiredEventBus.events.collect { authState ->
            if (authState is AuthState.Unauthenticated) {
                navController.navigate(Screen.AuthGraph)
            }
        }
    }

    LaunchedEffect(true) {
        PaymentEventBus.events.collect { result ->
            if (result.status == "success" || result.status == "failed") {
                val success = result.status == "success"
                PaymentEventBus.reset()
                navController.navigate(Screen.PaymentCompleted(success)) {
                    popUpTo<Screen.Checkout> { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        authNavGraph(navController)

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
            )
        }

        composable<Screen.Referral> {
            ReferralScreen(onBackClick = { navController.navigateBack() })
        }

        composable<Screen.RecurringOrders> {
            RecurringOrdersScreen(onBackClick = { navController.navigateBack() })
        }

        composable<Screen.Membership> {
            MembershipScreen(onBackClick = { navController.navigateBack() })
        }

        composable<Screen.Profile> {
            ProfileScreen(
                navigateBack = {
                    navController.navigateBack()
                },
                navigateToDetail = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateToOrderDetail = { id ->
                    navController.navigate(Screen.OrderDetail(id))
                },
                onSignedOut = {
                    navController.navigate(Screen.HomeGraph()) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.CustomerClub> {
            CustomerClubScreen(
                navigateBack = { navController.navigateBack() }
            )
        }

        // ---- Academy (vertical) ----
        composable<Screen.CourseCatalog> {
            CourseListScreen(
                mine = false,
                title = "دوره‌ها",
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) },
                navigateToInstructor = { name -> navController.navigate(Screen.InstructorCourses(name)) }
            )
        }

        composable<Screen.MyCourses> {
            CourseListScreen(
                mine = true,
                title = "دوره‌های من",
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) },
                navigateToCatalog = { navController.navigate(Screen.CourseCatalog) },
                navigateToInstructor = { name -> navController.navigate(Screen.InstructorCourses(name)) }
            )
        }

        composable<Screen.CourseDetail> {
            val args = it.toRoute<Screen.CourseDetail>()
            CourseDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToLearn = { slug -> navController.navigate(Screen.CourseLearn(slug)) }
            )
        }

        composable<Screen.CourseLearn> {
            val args = it.toRoute<Screen.CourseLearn>()
            CourseLearnScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToQuiz = { courseId -> navController.navigate(Screen.CourseQuiz(courseId)) },
                navigateToLessonQuiz = { lessonId -> navController.navigate(Screen.LessonQuiz(lessonId)) },
                navigateToProject = { courseId -> navController.navigate(Screen.ProjectSubmission(courseId)) }
            )
        }

        composable<Screen.CourseQuiz> {
            val args = it.toRoute<Screen.CourseQuiz>()
            CourseQuizScreen(
                courseId = args.courseId,
                navigateBack = { navController.navigateBack() },
                navigateToCertificates = {
                    navController.navigate(Screen.Certificates) {
                        popUpTo<Screen.CourseQuiz> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Certificates> {
            CertificatesScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.LessonQuiz> {
            val args = it.toRoute<Screen.LessonQuiz>()
            LessonQuizScreen(
                lessonId = args.lessonId,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ProjectSubmission> {
            val args = it.toRoute<Screen.ProjectSubmission>()
            ProjectSubmissionScreen(
                courseId = args.courseId,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.FreeCourses> {
            CourseListScreen(
                mine = false,
                title = "دوره‌های رایگان",
                freeOnly = true,
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) }
            )
        }

        composable<Screen.InstructorCourses> {
            val args = it.toRoute<Screen.InstructorCourses>()
            CourseListScreen(
                mine = false,
                title = args.instructorName,
                instructorFilter = args.instructorName,
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) }
            )
        }

        // ---- Product bundles (general shop feature) ----
        composable<Screen.BundleList> {
            BundleListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToBundle = { slug -> navController.navigate(Screen.BundleDetail(slug)) }
            )
        }

        composable<Screen.BundleDetail> {
            val args = it.toRoute<Screen.BundleDetail>()
            BundleDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToProduct = { productSlug -> navController.navigate(Screen.ProductDetail(productSlug)) }
            )
        }

        // ---- Clinic (vertical) ----
        composable<Screen.TherapistCatalog> {
            TherapistListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToTherapist = { slug -> navController.navigate(Screen.TherapistDetail(slug)) }
            )
        }

        composable<Screen.TherapistDetail> {
            val args = it.toRoute<Screen.TherapistDetail>()
            TherapistDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToMyAppointments = {
                    navController.navigate(Screen.MyAppointments) {
                        popUpTo<Screen.TherapistCatalog> { inclusive = false }
                    }
                },
                navigateToProduct = { productSlug ->
                    navController.navigate(Screen.ProductDetail(slug = productSlug))
                }
            )
        }

        composable<Screen.MyAppointments> {
            MyAppointmentsScreen(
                navigateBack = { navController.navigateBack() },
                navigateToCatalog = { navController.navigate(Screen.TherapistCatalog) }
            )
        }

        // ---- Psychology tests (vertical) ----
        composable<Screen.PsychTestCatalog> {
            PsychTestListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToProduct = { productSlug -> navController.navigate(Screen.ProductDetail(slug = productSlug)) },
                navigateToTakeTest = { userTestId -> navController.navigate(Screen.TakeTest(userTestId)) }
            )
        }

        composable<Screen.TakeTest> {
            val args = it.toRoute<Screen.TakeTest>()
            TakeTestScreen(
                userTestId = args.userTestId,
                navigateBack = { navController.navigateBack() }
            )
        }

        // ---- Product comparison ----
        composable<Screen.Comparison> {
            ComparisonScreen(
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { slug -> navController.navigate(Screen.ProductDetail(slug)) }
            )
        }

        composable<Screen.Favorites> {
            FavoritesScreen(
                navigateBack = {
                    navController.navigateBack()
                },
                navigateToDetail = { slug ->
                    navController.navigate(Screen.ProductDetail(slug))
                }
            )
        }

        composable<Screen.Wallet> {
            WalletScreen(
                onBackClick = { navController.navigateBack() }
            )
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

        composable<Screen.Settings> {
            SettingsScreen {
                navController.navigateBack()
            }
        }

        composable<Screen.ContactUs> {
            ContactUsScreen {
                navController.navigateBack()
            }
        }

        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = { navController.navigateBack() },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id))
                },
                navigateToManageBlog = { id: Long?, slug: String? ->
                    navController.navigate(Screen.ManageBlog(id, slug))
                },
                navigateToManageCategory = { _: Long? ->
                    // مدیریت دسته‌بندی مجله (در صورت نیاز)
                }
            )
        }

        composable<Screen.ManageWallets> {
            AdminWalletScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageWithdrawals> {
            AdminWithdrawalsScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageOrders> {
            AdminOrderScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageOptions> {
            ManageOptionsScreen(
                onBackClick = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageProduct> {
            val args = it.toRoute<Screen.ManageProduct>()
            ManageProductScreen(
                id = args.id,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageDiscounts> {
            AdminDiscountsScreen(
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageStories> {
            AdminStoryScreen(
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.BlogList> {
            BlogListScreen(
                navigateToDetail = { slug: String ->
                    navController.navigate(Screen.BlogDetail(slug))
                },
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.BlogDetail> {
            val args = it.toRoute<Screen.BlogDetail>()
            BlogDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { newSlug: String ->
                    navController.navigate(Screen.BlogDetail(newSlug))
                }
            )
        }

        composable<Screen.AdminBlogList> {
            AdminBlogListScreen(
                navigateToManageBlog = { id: Long?, slug: String? ->
                    navController.navigate(Screen.ManageBlog(id, slug))
                },
                navigateToManageCategory = { id: Long? ->
                    // Logic for categories if needed
                },
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ManageBlog> {
            val args = it.toRoute<Screen.ManageBlog>()
            ManageBlogScreen(
                id = args.id,
                slug = args.slug,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.ProductDetail> {
            val args = it.toRoute<Screen.ProductDetail>()
            DetailsScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToCart = {
                    navController.navigate(Screen.HomeGraph(showCart = true)) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                },
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                },
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                }
            )
        }

        composable<Screen.CategorySearch> {
            val args = it.toRoute<Screen.CategorySearch>()
            CategorySearchScreen(
                categoryId = args.id,
                categoryName = args.name,
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateBack = { navController.navigateBack() },
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                }
            )
        }

        composable<Screen.Checkout> {
            val args = it.toRoute<Screen.Checkout>()
            CheckoutScreen(
                totalAmount = args.totalAmount,
                navigateBack = { navController.navigateBack() },
                navigateToPaymentCompleted = { success, error ->
                    navController.navigate(Screen.PaymentCompleted(success ?: false, error)) {
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
                navigateBack = {
                    navController.navigate(Screen.HomeGraph()) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                }
            )
        }


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
