package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
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
import com.kazemieh.profile.WalletScreen

fun NavGraphBuilder.adminNavGraph(navController: NavController) {
        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = { navController.navigateBack() },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id))
                },
                navigateToManageBlog = { id: Long?, slug: String? ->
                    navController.navigate(Screen.ManageBlog(id, slug))
                },
                academyContent = { onBackClick ->
                    com.kazemieh.admin.academy.AdminAcademyScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                courseRequestContent = { onBackClick ->
                    com.kazemieh.admin.academy.courserequest.AdminCourseRequestScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                clinicContent = { onBackClick ->
                    com.kazemieh.admin.clinic.AdminClinicScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                psychTestContent = { onBackClick ->
                    com.kazemieh.admin.psychtest.AdminPsychTestScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                optionsContent = { onBackClick ->
                    com.kazemieh.admin.options.ManageOptionsScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                orderContent = { onBackClick ->
                    com.kazemieh.admin.orders.AdminOrderScreen(
                        onBackClick = onBackClick,
                        embedded = true
                    )
                },
                blogListContent = { navBlog, onBackClick ->
                    com.kazemieh.admin.blog.AdminBlogListScreen(
                        navigateToManageBlog = navBlog,
                        navigateBack = onBackClick,
                        embedded = true
                    )
                },
                financeContent = { onBackClick ->
                    com.kazemieh.admin.wallet.AdminFinanceScreen(
                        onBackClick = onBackClick
                    )
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

        composable<Screen.AdminBlogList> {
            AdminBlogListScreen(
                navigateToManageBlog = { id: Long?, slug: String? ->
                    navController.navigate(Screen.ManageBlog(id, slug))
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

}
