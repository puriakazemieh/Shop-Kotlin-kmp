package com.kazemieh.domain.academy

data class CourseSummary(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val lessonCount: Int,
    val enrolled: Boolean
)

data class Lesson(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    val videoUrl: String?,
    val completed: Boolean,
    val lastPositionSeconds: Int
)

data class CourseSection(
    val id: Long,
    val title: String,
    val lessons: List<Lesson>
)

data class CourseDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val enrolled: Boolean,
    val progressPercent: Int,
    val sections: List<CourseSection>
)

data class CourseProgress(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int
)
