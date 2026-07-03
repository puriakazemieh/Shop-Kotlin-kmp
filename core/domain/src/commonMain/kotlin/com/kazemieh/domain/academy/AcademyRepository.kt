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
}
