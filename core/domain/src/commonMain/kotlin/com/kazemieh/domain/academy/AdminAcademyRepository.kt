package com.kazemieh.domain.academy

import com.kazemieh.common.AppResult

data class AdminCourseParams(
    val title: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double = 0.0,
    val discountedPrice: Double? = null,
    val productId: Long? = null,
    val isPublished: Boolean = true,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: String? = null
)

data class AdminCourseUpdateParams(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val isPublished: Boolean? = null
)

/** یک سؤالِ آزمون در فرمِ تست‌سازِ ادمین: متن + گزینه‌ها + ایندکسِ گزینه‌ی درست. */
data class AdminQuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

interface AdminAcademyRepository {
    suspend fun listCourses(): AppResult<List<CourseSummary>>
    suspend fun getCourseDetail(id: Long): AppResult<CourseDetail>
    suspend fun createCourse(params: AdminCourseParams): AppResult<Long>
    suspend fun updateCourse(id: Long, params: AdminCourseUpdateParams): AppResult<Unit>
    suspend fun deleteCourse(id: Long): AppResult<Unit>
    suspend fun addSection(courseId: Long, title: String, sortOrder: Int): AppResult<Long>
    suspend fun addLesson(
        courseId: Long,
        sectionId: Long,
        title: String,
        videoUrl: String?,
        durationSeconds: Int,
        sortOrder: Int,
        isFreePreview: Boolean
    ): AppResult<Long>
    suspend fun upsertQuiz(
        courseId: Long,
        title: String,
        passScore: Int,
        questions: List<AdminQuizQuestion>
    ): AppResult<Unit>
}
