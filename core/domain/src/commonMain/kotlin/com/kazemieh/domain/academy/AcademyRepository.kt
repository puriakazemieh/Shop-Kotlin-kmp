package com.kazemieh.domain.academy

import com.kazemieh.common.AppResult

interface AcademyRepository {
    suspend fun getCourses(): AppResult<List<CourseSummary>>
    suspend fun getCourse(slug: String): AppResult<CourseDetail>
    suspend fun getMyCourses(): AppResult<List<CourseSummary>>
    suspend fun enroll(courseId: Long): AppResult<CourseDetail>
    suspend fun updateLessonProgress(lessonId: Long, completed: Boolean?, lastPositionSeconds: Int?): AppResult<CourseProgress>
    suspend fun getQuiz(courseId: Long): AppResult<Quiz>
    suspend fun submitQuiz(courseId: Long, answers: Map<Int, Int>): AppResult<QuizResult>
    suspend fun getCertificates(): AppResult<List<Certificate>>
    suspend fun joinWaitlist(courseId: Long): AppResult<WaitlistResult>
    suspend fun getLessonQuiz(lessonId: Long): AppResult<LessonQuiz>
    suspend fun submitLessonQuiz(lessonId: Long, answers: Map<Int, Int>): AppResult<LessonQuizResult>
    suspend fun submitProject(courseId: Long, fileBytes: ByteArray, fileName: String, note: String?): AppResult<ProjectSubmission>
    /** ثبتِ پروژه با لینکِ مستقیم (مثلاً گیت‌هاب/درایو) — بدونِ آپلود. */
    suspend fun submitProjectByLink(courseId: Long, fileUrl: String, note: String?): AppResult<ProjectSubmission>
    suspend fun getMyProject(courseId: Long): AppResult<ProjectSubmission?>

    // ---- پرسش‌وپاسخِ درس ----
    suspend fun getLessonQuestions(lessonId: Long): AppResult<List<LessonQuestion>>
    suspend fun createLessonQuestion(lessonId: Long, content: String, parentId: Long? = null): AppResult<LessonQuestion>

    // ---- نشانِ به‌روزرسانیِ دوره ----
    suspend fun markCourseUpdateSeen(courseId: Long): AppResult<Unit>

    // ---- نقدِ همتایان ----
    suspend fun getPeerSubmissions(courseId: Long): AppResult<List<ProjectSubmission>>
    suspend fun getPeerComments(submissionId: Long): AppResult<List<PeerComment>>
    suspend fun addPeerComment(submissionId: Long, comment: String): AppResult<PeerComment>

    // ---- تاییدِ عمومیِ گواهی ----
    suspend fun verifyCertificate(certNumber: String): AppResult<CertificateVerification>

    // ---- آزمونِ تعیینِ سطح ----
    suspend fun getPlacementQuiz(): AppResult<List<PlacementQuizQuestion>>
    suspend fun submitPlacementQuiz(answers: List<Int>): AppResult<PlacementQuizResult>

    // ---- گارانتیِ بازگشتِ وجهِ دوره ----
    suspend fun requestRefund(courseId: Long, reason: String?): AppResult<CourseRefundRequest>
    suspend fun getMyRefundRequests(): AppResult<List<CourseRefundRequest>>
}
