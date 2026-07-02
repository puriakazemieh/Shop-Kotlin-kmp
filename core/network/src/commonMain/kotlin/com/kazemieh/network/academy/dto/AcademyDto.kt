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
    val enrolled: Boolean = false
)

@Serializable
data class LessonResponse(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    val videoUrl: String? = null,
    val completed: Boolean = false,
    val lastPositionSeconds: Int = 0
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
    val sections: List<SectionResponse>
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
