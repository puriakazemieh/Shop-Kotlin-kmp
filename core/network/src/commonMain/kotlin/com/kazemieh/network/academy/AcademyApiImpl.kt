package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers

class AcademyApiImpl(
    private val client: HttpClient
) : AcademyApi {

    override suspend fun getCourses(): List<CourseSummaryResponse> = safeApiCallRaw {
        client.get("api/courses")
    }

    override suspend fun getCourse(slug: String): CourseDetailResponse = safeApiCallRaw {
        client.get("api/courses/$slug")
    }

    override suspend fun getMyCourses(): List<CourseSummaryResponse> = safeApiCallRaw {
        client.get("api/academy/my-courses")
    }

    override suspend fun enroll(courseId: Long): CourseDetailResponse = safeApiCallRaw {
        client.post("api/academy/courses/$courseId/enroll")
    }

    override suspend fun updateLessonProgress(lessonId: Long, request: UpdateProgressRequestDto): ProgressResponse = safeApiCallRaw {
        client.post("api/academy/lessons/$lessonId/progress") {
            setBody(request)
        }
    }

    override suspend fun getQuiz(courseId: Long): QuizResponse = safeApiCallRaw {
        client.get("api/academy/courses/$courseId/quiz")
    }

    override suspend fun submitQuiz(courseId: Long, request: SubmitQuizRequestDto): QuizResultResponse = safeApiCallRaw {
        client.post("api/academy/courses/$courseId/quiz/submit") {
            setBody(request)
        }
    }

    override suspend fun getCertificates(): List<CertificateResponse> = safeApiCallRaw {
        client.get("api/academy/certificates")
    }

    override suspend fun joinWaitlist(courseId: Long): WaitlistResponse = safeApiCallRaw {
        client.post("api/academy/courses/$courseId/waitlist")
    }

    override suspend fun getLessonQuiz(lessonId: Long): LessonQuizResponse = safeApiCallRaw {
        client.get("api/academy/lessons/$lessonId/quiz")
    }

    override suspend fun submitLessonQuiz(lessonId: Long, request: SubmitLessonQuizRequestDto): LessonQuizResultResponse = safeApiCallRaw {
        client.post("api/academy/lessons/$lessonId/quiz/submit") {
            setBody(request)
        }
    }

    override suspend fun submitProject(courseId: Long, fileBytes: ByteArray, fileName: String, note: String?): ProjectSubmissionResponse = safeApiCallRaw {
        client.submitFormWithBinaryData(
            url = "api/academy/courses/$courseId/project",
            formData = formData {
                append("file", fileBytes, Headers.build {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
                if (!note.isNullOrBlank()) append("note", note)
            }
        )
    }

    override suspend fun submitProjectByLink(courseId: Long, request: SubmitProjectRequestDto): ProjectSubmissionResponse = safeApiCallRaw {
        client.post("api/academy/courses/$courseId/project/link") { setBody(request) }
    }

    override suspend fun getMyProject(courseId: Long): MyProjectResponse = safeApiCallRaw {
        client.get("api/academy/courses/$courseId/project")
    }

    override suspend fun getLessonQuestions(lessonId: Long): List<LessonQuestionResponse> = safeApiCallRaw {
        client.get("api/academy/lessons/$lessonId/questions")
    }

    override suspend fun createLessonQuestion(lessonId: Long, request: CreateLessonQuestionRequestDto): LessonQuestionResponse = safeApiCallRaw {
        client.post("api/academy/lessons/$lessonId/questions") { setBody(request) }
    }

    override suspend fun markCourseUpdateSeen(courseId: Long): Unit = safeApiCallRaw {
        client.post("api/academy/courses/$courseId/mark-update-seen")
    }

    override suspend fun getPeerSubmissions(courseId: Long): List<ProjectSubmissionResponse> = safeApiCallRaw {
        client.get("api/academy/courses/$courseId/project/peers")
    }

    override suspend fun getPeerComments(submissionId: Long): List<PeerCommentResponse> = safeApiCallRaw {
        client.get("api/academy/project/$submissionId/comments")
    }

    override suspend fun addPeerComment(submissionId: Long, request: CreatePeerCommentRequestDto): PeerCommentResponse = safeApiCallRaw {
        client.post("api/academy/project/$submissionId/comments") { setBody(request) }
    }

    override suspend fun verifyCertificate(certNumber: String): CertificateVerifyResponse = safeApiCallRaw {
        client.get("api/courses/certificates/verify/$certNumber")
    }

    override suspend fun getPlacementQuiz(): PlacementQuizResponseDto = safeApiCallRaw {
        client.get("api/academy/placement-quiz")
    }

    override suspend fun submitPlacementQuiz(request: SubmitPlacementQuizRequestDto): PlacementQuizResultResponseDto = safeApiCallRaw {
        client.post("api/academy/placement-quiz/submit") { setBody(request) }
    }
}
