package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
import com.kazemieh.support.ContactUsScreen
import com.kazemieh.profile.WalletScreen
import com.kazemieh.profile.ReferralScreen
import com.kazemieh.profile.MembershipScreen
import com.kazemieh.settings.SettingsScreen
import com.kazemieh.profile.ProfileScreen
import com.kazemieh.profile.FavoritesScreen
import com.kazemieh.profile.club.CustomerClubScreen
import com.kazemieh.academy.list.CourseListScreen

fun NavGraphBuilder.profileNavGraph(navController: NavController) {
        composable<Screen.Referral> {
            ReferralScreen(onBackClick = { navController.navigateBack() })
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
                navigateToCourse = { slug ->
                    navController.navigate(Screen.CourseDetail(slug))
                },
                navigateToCourseCatalog = {
                    navController.navigate(Screen.CourseCatalog)
                },
                navigateToCourseRequests = {
                    navController.navigate(Screen.CourseRequests)
                },
                navigateToTakeTest = { userTestId ->
                    navController.navigate(Screen.TakeTest(userTestId))
                },
                navigateToTherapistCatalog = {
                    navController.navigate(Screen.TherapistCatalog)
                },
                navigateToSessionReceipt = { appointmentId ->
                    navController.navigate(Screen.SessionReceipt(appointmentId))
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

}
