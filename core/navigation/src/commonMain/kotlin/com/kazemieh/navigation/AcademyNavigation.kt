package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
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

fun NavGraphBuilder.academyNavGraph(navController: NavController) {
        composable<Screen.CourseCatalog> {
            CourseListScreen(
                mine = false,
                title = "دوره‌ها",
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) },
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

        composable<Screen.CourseRequests> {
            CourseRequestScreen(navigateBack = { navController.navigateBack() })
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
                navigateBack = { navController.navigateBack() },
                navigateToPeerReview = { courseId -> navController.navigate(Screen.PeerReview(courseId)) }
            )
        }

        composable<Screen.PeerReview> {
            val args = it.toRoute<Screen.PeerReview>()
            PeerReviewScreen(
                courseId = args.courseId,
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.CertificateVerify> {
            CertificateVerifyScreen(navigateBack = { navController.navigateBack() })
        }

        composable<Screen.PlacementQuiz> {
            PlacementQuizScreen(
                navigateBack = { navController.navigateBack() },
                navigateToCoursesByLevel = { level ->
                    navController.navigate(Screen.CoursesByLevel(level)) {
                        popUpTo<Screen.PlacementQuiz> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.CoursesByLevel> {
            val args = it.toRoute<Screen.CoursesByLevel>()
            val levelLabel = when (args.level) {
                "BEGINNER" -> "مقدماتی"; "INTERMEDIATE" -> "متوسط"; "ADVANCED" -> "پیشرفته"; else -> args.level
            }
            CourseListScreen(
                mine = false,
                title = "دوره‌های سطحِ $levelLabel",
                levelFilter = args.level,
                navigateBack = { navController.navigateBack() },
                navigateToCourse = { slug -> navController.navigate(Screen.CourseDetail(slug)) }
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

}
