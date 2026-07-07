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
    val instructorSkills: String? = null,
    val requiresProjectSubmission: Boolean = false
)

data class AdminCourseUpdateParams(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val isPublished: Boolean? = null,
    val requiresProjectSubmission: Boolean? = null
)

/** یک سؤالِ آزمون در فرمِ تست‌سازِ ادمین: متن + گزینه‌ها + ایندکسِ گزینه‌ی درست. */
data class AdminQuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

data class AdminWaitlistEntry(
    val id: Long,
    val userId: Long,
    val notified: Boolean,
    val createdAt: String,
    val notifiedAt: String?
)

interface AdminAcademyRepository {
    /** آپلودِ عمومیِ رسانه (کاورِ دوره یا ویدیویِ درس) — قبل از ساختِ دوره/درس، فقط URL برمی‌گرداند. */
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): AppResult<String>

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
    suspend fun listWaitlist(courseId: Long): AppResult<List<AdminWaitlistEntry>>
    /** خروجی: نفرِ مطلع‌شده، یا null اگر صف خالی بود. */
    suspend fun notifyNextInWaitlist(courseId: Long): AppResult<AdminWaitlistEntry?>

    // ---- فایل‌های ضمیمه‌ی درس ----
    suspend fun addLessonFile(courseId: Long, lessonId: Long, fileBytes: ByteArray, fileName: String, displayName: String): AppResult<Int>
    /** افزودنِ فایلِ ضمیمه با لینکِ مستقیم (بدونِ آپلود) — هم‌الگو با نحوه‌ی ثبتِ videoUrl. */
    suspend fun addLessonFileByLink(courseId: Long, lessonId: Long, name: String, url: String, sizeLabel: String? = null): AppResult<Int>
    suspend fun deleteLessonFile(courseId: Long, lessonId: Long, index: Int): AppResult<Unit>

    // ---- آزمونِ کوتاهِ درس ----
    suspend fun getLessonQuiz(courseId: Long, lessonId: Long): AppResult<LessonQuiz?>
    suspend fun upsertLessonQuiz(courseId: Long, lessonId: Long, title: String, passScore: Int, questions: List<AdminQuizQuestion>): AppResult<Unit>

    // ---- پروژه‌های پایانی ----
    suspend fun listProjectSubmissions(courseId: Long): AppResult<List<ProjectSubmission>>
    suspend fun reviewProjectSubmission(submissionId: Long, status: String, mentorFeedback: String?): AppResult<Unit>
}
