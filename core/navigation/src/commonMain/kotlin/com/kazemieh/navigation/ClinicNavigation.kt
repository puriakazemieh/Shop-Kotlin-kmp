package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
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

fun NavGraphBuilder.clinicNavGraph(navController: NavController) {
        composable<Screen.TherapistCatalog> {
            TherapistListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToTherapist = { slug -> navController.navigate(Screen.TherapistDetail(slug)) },
                navigateToMoodCheckIn = { navController.navigate(Screen.MoodCheckIn) },
                navigateToEmergencyResources = { navController.navigate(Screen.EmergencyResources) }
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
                },
                navigateToMessaging = { therapistId ->
                    navController.navigate(Screen.MessagingThread(therapistId))
                }
            )
        }

        composable<Screen.MessagingThread> {
            val args = it.toRoute<Screen.MessagingThread>()
            MessagingScreen(therapistId = args.therapistId, navigateBack = { navController.navigateBack() })
        }

        composable<Screen.Homework> {
            HomeworkScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.Journal> {
            JournalScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.TherapistMatch> {
            TherapistMatchScreen(
                navigateBack = { navController.navigateBack() },
                navigateToTherapist = { slug -> navController.navigate(Screen.TherapistDetail(slug)) }
            )
        }

        composable<Screen.MyAppointments> {
            MyAppointmentsScreen(
                navigateBack = { navController.navigateBack() },
                navigateToCatalog = { navController.navigate(Screen.TherapistCatalog) },
                navigateToReceipt = { appointmentId -> navController.navigate(Screen.SessionReceipt(appointmentId)) }
            )
        }

        composable<Screen.MoodCheckIn> {
            MoodCheckInScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.EmergencyResources> {
            EmergencyResourcesScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.SessionReceipt> {
            val args = it.toRoute<Screen.SessionReceipt>()
            SessionReceiptScreen(appointmentId = args.appointmentId, navigateBack = { navController.navigateBack() })
        }

}
