package com.kazemieh.network.academy.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val lessonCount: Int,
    val enrolled: Boolean = false,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val isOnline: Boolean = true,
    val level: String? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val hasUnseenUpdate: Boolean = false
)

@Serializable
data class VideoVariantResponse(
    val quality: String,
    val url: String
)

@Serializable
data class LessonFileResponse(
    val name: String,
    val url: String,
    val sizeLabel: String? = null
)

@Serializable
data class LessonResponse(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    val videoUrl: String? = null,
    val completed: Boolean = false,
    val lastPositionSeconds: Int = 0,
    val videoVariants: List<VideoVariantResponse> = emptyList(),
    val resourceFiles: List<LessonFileResponse> = emptyList(),
    val hasQuiz: Boolean = false
)

@Serializable
data class SectionResponse(
    val id: Long,
    val title: String,
    val lessons: List<LessonResponse>
)

@Serializable
data class CourseDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double,
    val discountedPrice: Double? = null,
    val enrolled: Boolean,
    val progressPercent: Int,
    val sections: List<SectionResponse>,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val isOnline: Boolean = true,
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val seatsTaken: Int = 0,
    val seatsRemaining: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: List<String> = emptyList(),
    val isFull: Boolean = false,
    val onWaitlist: Boolean = false,
    val productId: Long? = null,
    val requiresProjectSubmission: Boolean = false,
    val instructorDiscountCode: String? = null,
    val totalDurationSeconds: Int = 0,
    val resourceFileCount: Int = 0,
    val hasUnseenUpdate: Boolean = false
)

@Serializable
data class ProgressResponse(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int
)

@Serializable
data class UpdateProgressRequestDto(
    val completed: Boolean? = null,
    val lastPositionSeconds: Int? = null
)

// ---------- Quiz ----------
@Serializable
data class QuizOptionResponse(
    val text: String,
    val correct: Boolean? = null
)

@Serializable
data class QuizQuestionResponse(
    val index: Int,
    val text: String,
    val options: List<QuizOptionResponse>
)

@Serializable
data class QuizResponse(
    val courseId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestionResponse>,
    val alreadyPassed: Boolean = false
)

@Serializable
data class SubmitQuizRequestDto(
    val answers: Map<Int, Int> = emptyMap()
)

@Serializable
data class QuizResultResponse(
    val courseId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int,
    val certificateNumber: String? = null
)

// ---------- Waitlist ----------
@Serializable
data class WaitlistResponse(
    val courseId: Long,
    val joined: Boolean,
    val position: Int? = null
)

// ---------- Certificate ----------
@Serializable
data class CertificateResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val certNumber: String,
    val issuedAt: String,
    val userName: String? = null
)

// ---------- Lesson quiz (checkpoint) ----------
@Serializable
data class LessonQuizResponse(
    val lessonId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestionResponse>,
    val alreadyPassed: Boolean = false
)

@Serializable
data class SubmitLessonQuizRequestDto(
    val answers: Map<Int, Int> = emptyMap()
)

@Serializable
data class LessonQuizResultResponse(
    val lessonId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int
)

// ---------- Project-based assessment ----------
@Serializable
data class SubmitProjectRequestDto(
    val fileUrl: String,
    val note: String? = null
)

@Serializable
data class ProjectSubmissionResponse(
    val id: Long,
    val courseId: Long,
    val userId: Long,
    val fileUrl: String,
    val note: String? = null,
    val status: String,
    val mentorFeedback: String? = null,
    val submittedAt: String,
    val reviewedAt: String? = null,
    val userName: String? = null
)

@Serializable
data class AdminReviewProjectRequestDto(
    val status: String,
    val mentorFeedback: String? = null
)

/** wrapper به‌جای JSONِ نال‌بلِ خام. */
@Serializable
data class MyProjectResponse(
    val found: Boolean,
    val submission: ProjectSubmissionResponse? = null
)

// ---------- پرسش‌وپاسخِ درس (Phase V) ----------
@Serializable
data class LessonQuestionResponse(
    val id: Long,
    val userId: Long,
    val userName: String? = null,
    val content: String,
    val parentId: Long? = null,
    val createdAt: String? = null
)

@Serializable
data class CreateLessonQuestionRequestDto(
    val content: String,
    val parentId: Long? = null
)

// ---------- نقدِ همتایان (Phase V) ----------
@Serializable
data class PeerCommentResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val comment: String,
    val createdAt: String? = null
)

@Serializable
data class CreatePeerCommentRequestDto(val comment: String)

// ---------- تاییدِ گواهی (Phase V) ----------
@Serializable
data class CertificateVerifyResponse(
    val valid: Boolean,
    val courseTitle: String? = null,
    val certNumber: String? = null,
    val issuedAt: String? = null
)

// ---------- آزمونِ تعیینِ سطح (Phase V) ----------
@Serializable
data class PlacementQuizOptionResponse(val label: String, val score: Int)

@Serializable
data class PlacementQuizQuestionResponse(val id: Int, val text: String, val options: List<PlacementQuizOptionResponse>)

@Serializable
data class PlacementQuizResponseDto(val questions: List<PlacementQuizQuestionResponse>)

@Serializable
data class SubmitPlacementQuizRequestDto(val answers: List<Int>)

@Serializable
data class PlacementQuizResultResponseDto(val level: String, val label: String)
