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
    val freeUpdateBadge: Boolean = false
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
    val requiresProjectSubmission: Boolean = false
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
