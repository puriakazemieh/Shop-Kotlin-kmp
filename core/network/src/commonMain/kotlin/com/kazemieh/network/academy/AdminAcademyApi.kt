package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*

interface AdminAcademyApi {
    // ---- آپلودِ عمومیِ رسانه (کاورِ دوره یا ویدیویِ درس) — قبل از ساختِ دوره/درس، فقط URL برمی‌گرداند ----
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): String

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

    // ---- سازمان/صندلیِ سازمانی ----
    suspend fun listOrganizations(): List<OrganizationResponse>
    suspend fun createOrganization(request: CreateOrganizationRequestDto): OrganizationResponse
    suspend fun buySeats(organizationId: Long, request: BuySeatsRequestDto): List<SeatResponse>
    suspend fun listSeats(organizationId: Long): List<SeatResponse>
    suspend fun assignSeat(organizationId: Long, request: AssignSeatRequestDto): SeatResponse

    // ---- گارانتیِ بازگشتِ وجه ----
    suspend fun listRefundRequests(): List<AdminCourseRefundRequestResponse>
    suspend fun reviewRefundRequest(id: Long, request: AdminReviewRefundRequestDto): AdminCourseRefundRequestResponse
}
