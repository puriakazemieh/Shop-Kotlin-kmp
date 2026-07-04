package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*

interface AdminAcademyApi {
    suspend fun listCourses(): List<CourseSummaryResponse>
    suspend fun getCourseDetail(id: Long): CourseDetailResponse
    suspend fun createCourse(request: AdminCreateCourseRequestDto): Long
    suspend fun updateCourse(id: Long, request: AdminUpdateCourseRequestDto)
    suspend fun deleteCourse(id: Long)
    suspend fun addSection(courseId: Long, request: AdminCreateSectionRequestDto): Long
    suspend fun addLesson(courseId: Long, sectionId: Long, request: AdminCreateLessonRequestDto): Long
    suspend fun upsertQuiz(courseId: Long, request: AdminUpsertQuizRequestDto)
    suspend fun listWaitlist(courseId: Long): List<AdminWaitlistEntryResponse>
    suspend fun notifyNextInWaitlist(courseId: Long): AdminNotifyNextResponse

    // ---- فایل‌های ضمیمه‌ی درس ----
    suspend fun addLessonFile(courseId: Long, lessonId: Long, fileBytes: ByteArray, fileName: String, displayName: String): Int
    suspend fun addLessonFileByLink(courseId: Long, lessonId: Long, request: AdminAddLessonFileRequestDto): Int
    suspend fun deleteLessonFile(courseId: Long, lessonId: Long, index: Int)

    // ---- آزمونِ کوتاهِ درس ----
    suspend fun getLessonQuiz(courseId: Long, lessonId: Long): AdminLessonQuizResponse
    suspend fun upsertLessonQuiz(courseId: Long, lessonId: Long, request: AdminUpsertLessonQuizRequestDto)

    // ---- پروژه‌های پایانی ----
    suspend fun listProjectSubmissions(courseId: Long): List<ProjectSubmissionResponse>
    suspend fun reviewProjectSubmission(submissionId: Long, request: AdminReviewProjectRequestDto)
}
