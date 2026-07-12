package com.kazemieh.common

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object AuthGraph: Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    @Serializable
    data object ForgotPassword : Screen()

    @Serializable
    data class ResetPassword(val token: String) : Screen()

    @Serializable
    data class HomeGraph(val showCart: Boolean = false) : Screen()

    @Serializable
    data object BlogList : Screen()

    @Serializable
    data class BlogDetail(val slug: String) : Screen()

    @Serializable
    data object ProductsOverview : Screen()

    @Serializable
    data object Cart : Screen()

    @Serializable
    data object Categories : Screen()

    @Serializable
    data object Search : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object MyOrders : Screen()

    @Serializable
    data class OrderDetail(val id: Long) : Screen()

    @Serializable
    data class OrderTracking(val id: Long) : Screen()

    @Serializable
    data class ReturnRequest(val orderItemId: Long? = null, val itemTitle: String? = null) : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object AdminPanel : Screen()

    @Serializable
    data class ManageProduct(val id: Long? = null) : Screen()

    @Serializable
    data object ManageOrders : Screen()

    @Serializable
    data object ManageOptions : Screen()

    @Serializable
    data object ManageDiscounts : Screen()

    @Serializable
    data object ManageWallets : Screen()

    @Serializable
    data object ManageWithdrawals : Screen()

    @Serializable
    data object ManageStories : Screen()

    @Serializable
    data object AdminBlogList : Screen()

    @Serializable
    data class ManageBlog(val id: Long? = null, val slug: String? = null) : Screen()

    @Serializable
    data object Wallet : Screen()

    @Serializable
    data object Favorites : Screen()

    @Serializable
    data class Checkout(val totalAmount: Double) : Screen()

    @Serializable
    data class PaymentCompleted(val success: Boolean, val error: String? = null) : Screen()

    @Serializable
    data class CategorySearch(val id: Long, val name: String) : Screen()

    @Serializable
    data object ContactUs : Screen()

    @Serializable
    data class ProductDetail(val slug: String) : Screen()

    @Serializable
    data object CustomerClub : Screen()

    // ---- Academy (vertical) ----
    @Serializable
    data object CourseCatalog : Screen()

    @Serializable
    data object MyCourses : Screen()

    @Serializable
    data class CourseDetail(val slug: String) : Screen()

    @Serializable
    data class CourseLearn(val slug: String) : Screen()

    @Serializable
    data class CourseQuiz(val courseId: Long) : Screen()

    @Serializable
    data object Certificates : Screen()

    @Serializable
    data class LessonQuiz(val lessonId: Long) : Screen()

    @Serializable
    data class ProjectSubmission(val courseId: Long) : Screen()

    @Serializable
    data object FreeCourses : Screen()

    @Serializable
    data class InstructorCourses(val instructorName: String) : Screen()

    @Serializable
    data class CoursesByLevel(val level: String) : Screen()

    @Serializable
    data object CertificateVerify : Screen()

    @Serializable
    data object PlacementQuiz : Screen()

    @Serializable
    data class PeerReview(val courseId: Long) : Screen()

    // ---- Product bundles (general shop feature) ----
    @Serializable
    data object BundleList : Screen()

    @Serializable
    data object Referral : Screen()

    @Serializable
    data object RecurringOrders : Screen()

    @Serializable
    data object Membership : Screen()

    @Serializable
    data object ShoppingAssistant : Screen()

    @Serializable
    data class BundleDetail(val slug: String) : Screen()

    // ---- Clinic (vertical) ----
    @Serializable
    data object TherapistCatalog : Screen()

    @Serializable
    data object MyAppointments : Screen()

    @Serializable
    data class TherapistDetail(val slug: String) : Screen()

    @Serializable
    data object MoodCheckIn : Screen()

    @Serializable
    data object EmergencyResources : Screen()

    @Serializable
    data class SessionReceipt(val appointmentId: Long) : Screen()

    @Serializable
    data class MessagingThread(val therapistId: Long) : Screen()

    @Serializable
    data object Homework : Screen()

    @Serializable
    data object Journal : Screen()

    @Serializable
    data object TherapistMatch : Screen()

    // ---- Psychology tests (vertical) ----
    @Serializable
    data object PsychTestCatalog : Screen()

    @Serializable
    data class TakeTest(val userTestId: Long) : Screen()

    // ---- Product comparison (general feature) ----
    @Serializable
    data object Comparison : Screen()

}
