package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*

interface AcademyApi {
    suspend fun getCourses(): List<CourseSummaryResponse>
    suspend fun getCourse(slug: String): CourseDetailResponse
    suspend fun getMyCourses(): List<CourseSummaryResponse>
    suspend fun enroll(courseId: Long): CourseDetailResponse
    suspend fun updateLessonProgress(lessonId: Long, request: UpdateProgressRequestDto): ProgressResponse
    suspend fun getQuiz(courseId: Long): QuizResponse
    suspend fun submitQuiz(courseId: Long, request: SubmitQuizRequestDto): QuizResultResponse
    suspend fun getCertificates(): List<CertificateResponse>
    suspend fun joinWaitlist(courseId: Long): WaitlistResponse
    suspend fun getLessonQuiz(lessonId: Long): LessonQuizResponse
    suspend fun submitLessonQuiz(lessonId: Long, request: SubmitLessonQuizRequestDto): LessonQuizResultResponse
    suspend fun submitProject(courseId: Long, fileBytes: ByteArray, fileName: String, note: String?): ProjectSubmissionResponse
    suspend fun submitProjectByLink(courseId: Long, request: SubmitProjectRequestDto): ProjectSubmissionResponse
    suspend fun getMyProject(courseId: Long): MyProjectResponse

    // ---- پرسش‌وپاسخِ درس ----
    suspend fun getLessonQuestions(lessonId: Long): List<LessonQuestionResponse>
    suspend fun createLessonQuestion(lessonId: Long, request: CreateLessonQuestionRequestDto): LessonQuestionResponse

    // ---- نشانِ به‌روزرسانیِ دوره ----
    suspend fun markCourseUpdateSeen(courseId: Long)

    // ---- نقدِ همتایان ----
    suspend fun getPeerSubmissions(courseId: Long): List<ProjectSubmissionResponse>
    suspend fun getPeerComments(submissionId: Long): List<PeerCommentResponse>
    suspend fun addPeerComment(submissionId: Long, request: CreatePeerCommentRequestDto): PeerCommentResponse

    // ---- تاییدِ عمومیِ گواهی (بدونِ نیازِ ورود) ----
    suspend fun verifyCertificate(certNumber: String): CertificateVerifyResponse

    // ---- آزمونِ تعیینِ سطح ----
    suspend fun getPlacementQuiz(): PlacementQuizResponseDto
    suspend fun submitPlacementQuiz(request: SubmitPlacementQuizRequestDto): PlacementQuizResultResponseDto

    // ---- گارانتیِ بازگشتِ وجهِ دوره ----
    suspend fun requestRefund(courseId: Long, request: CourseRefundRequestRequestDto): CourseRefundRequestResponse
    suspend fun getMyRefundRequests(): List<CourseRefundRequestResponse>
}
