package com.kazemieh.network.academy.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdResponse(val id: Long)

@Serializable
data class MediaUploadUrlResponse(val url: String)

@Serializable
data class AdminCreateCourseRequestDto(
    val title: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double = 0.0,
    val discountedPrice: Double? = null,
    val productId: Long? = null,
    // ارسال با نامِ کاملِ isPublished؛ سرور (Jackson/Kotlin) پارامترِ سازنده را با همین نام می‌خواند.
    val isPublished: Boolean = true,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: String? = null,
    val requiresProjectSubmission: Boolean = false,
    val cohortStartDate: String? = null
)

@Serializable
data class AdminUpdateCourseRequestDto(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val isPublished: Boolean? = null,
    val requiresProjectSubmission: Boolean? = null,
    val cohortStartDate: String? = null
)

@Serializable
data class AdminCreateSectionRequestDto(
    val title: String,
    val sortOrder: Int = 0
)

@Serializable
data class AdminCreateLessonRequestDto(
    val title: String,
    val videoUrl: String? = null,
    val durationSeconds: Int = 0,
    val sortOrder: Int = 0,
    val isFreePreview: Boolean = false,
    val videoVariants: List<VideoVariantResponse> = emptyList(),
    val subtitles: List<SubtitleTrackResponse> = emptyList()
)

// ---------- Admin quiz builder ----------
@Serializable
data class AdminUpsertQuizRequestDto(
    val title: String = "آزمونِ پایانِ دوره",
    val passScore: Int = 60,
    val questions: List<QuizQuestionResponse> = emptyList()
)

// ---------- Admin waitlist ----------
@Serializable
data class AdminWaitlistEntryResponse(
    val id: Long,
    val userId: Long,
    val notified: Boolean,
    val createdAt: String,
    val notifiedAt: String? = null
)

@Serializable
data class AdminNotifyNextResponse(
    val found: Boolean,
    val entry: AdminWaitlistEntryResponse? = null
)

// ---------- Lesson resources/quiz + project review ----------
@Serializable
data class IndexResponse(val index: Int)

@Serializable
data class AdminAddLessonFileRequestDto(
    val name: String,
    val url: String,
    val sizeLabel: String? = null
)

@Serializable
data class AdminUpsertLessonQuizRequestDto(
    val title: String = "آزمونِ این درس",
    val passScore: Int = 60,
    val questions: List<QuizQuestionResponse> = emptyList()
)

/** wrapper به‌جای JSONِ نال‌بلِ خام. */
@Serializable
data class AdminLessonQuizResponse(
    val found: Boolean,
    val quiz: LessonQuizResponse? = null
)

// ---------- سازمان/صندلیِ سازمانی (Phase W) ----------
@Serializable
data class OrganizationResponse(
    val id: Long,
    val name: String,
    val contactEmail: String? = null,
    val createdAt: String? = null
)

@Serializable
data class CreateOrganizationRequestDto(
    val name: String,
    val contactEmail: String? = null
)

@Serializable
data class SeatResponse(
    val id: Long,
    val organizationId: Long,
    val courseId: Long,
    val assignedUserId: Long? = null,
    val assignedEmail: String? = null,
    val assignedAt: String? = null
)

@Serializable
data class BuySeatsRequestDto(
    val courseId: Long,
    val count: Int
)

@Serializable
data class AssignSeatRequestDto(
    val courseId: Long,
    val email: String
)

// ---------- گارانتیِ بازگشتِ وجهِ دوره (Phase W) ----------
@Serializable
data class AdminCourseRefundRequestResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val userId: Long,
    val userName: String? = null,
    val amount: Double,
    val reason: String? = null,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String? = null,
    val resolvedAt: String? = null
)

@Serializable
data class AdminReviewRefundRequestDto(
    val approve: Boolean,
    val adminNote: String? = null
)
