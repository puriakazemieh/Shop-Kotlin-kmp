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
}
