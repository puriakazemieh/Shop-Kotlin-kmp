package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers

class AdminAcademyApiImpl(
    private val client: HttpClient
) : AdminAcademyApi {

    override suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): String =
        safeApiCallRaw<MediaUploadUrlResponse> {
            client.submitFormWithBinaryData(
                url = "api/admin/courses/media/upload",
                formData = formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            )
        }.url

    override suspend fun listCourses(): List<CourseSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/courses")
    }

    override suspend fun getCourseDetail(id: Long): CourseDetailResponse = safeApiCallRaw {
        client.get("api/admin/courses/$id")
    }

    override suspend fun createCourse(request: AdminCreateCourseRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/courses") { setBody(request) }
        }.id

    override suspend fun updateCourse(id: Long, request: AdminUpdateCourseRequestDto): Unit = safeApiCallRaw {
        client.patch("api/admin/courses/$id") { setBody(request) }
    }

    override suspend fun deleteCourse(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/courses/$id")
    }

    override suspend fun addSection(courseId: Long, request: AdminCreateSectionRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/courses/$courseId/sections") { setBody(request) }
        }.id

    override suspend fun addLesson(courseId: Long, sectionId: Long, request: AdminCreateLessonRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/courses/$courseId/sections/$sectionId/lessons") { setBody(request) }
        }.id

    override suspend fun upsertQuiz(courseId: Long, request: AdminUpsertQuizRequestDto): Unit = safeApiCallRaw {
        client.put("api/admin/courses/$courseId/quiz") { setBody(request) }
    }

    override suspend fun listWaitlist(courseId: Long): List<AdminWaitlistEntryResponse> = safeApiCallRaw {
        client.get("api/admin/courses/$courseId/waitlist")
    }

    override suspend fun notifyNextInWaitlist(courseId: Long): AdminNotifyNextResponse = safeApiCallRaw {
        client.post("api/admin/courses/$courseId/waitlist/notify-next")
    }

    override suspend fun addLessonFile(courseId: Long, lessonId: Long, fileBytes: ByteArray, fileName: String, displayName: String): Int =
        safeApiCallRaw<IndexResponse> {
            client.submitFormWithBinaryData(
                url = "api/admin/courses/$courseId/lessons/$lessonId/files",
                formData = formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                    append("name", displayName)
                }
            )
        }.index

    override suspend fun addLessonFileByLink(courseId: Long, lessonId: Long, request: AdminAddLessonFileRequestDto): Int =
        safeApiCallRaw<IndexResponse> {
            client.post("api/admin/courses/$courseId/lessons/$lessonId/files/link") { setBody(request) }
        }.index

    override suspend fun deleteLessonFile(courseId: Long, lessonId: Long, index: Int): Unit = safeApiCallRaw {
        client.delete("api/admin/courses/$courseId/lessons/$lessonId/files/$index")
    }

    override suspend fun getLessonQuiz(courseId: Long, lessonId: Long): AdminLessonQuizResponse = safeApiCallRaw {
        client.get("api/admin/courses/$courseId/lessons/$lessonId/quiz")
    }

    override suspend fun upsertLessonQuiz(courseId: Long, lessonId: Long, request: AdminUpsertLessonQuizRequestDto): Unit = safeApiCallRaw {
        client.put("api/admin/courses/$courseId/lessons/$lessonId/quiz") { setBody(request) }
    }

    override suspend fun listProjectSubmissions(courseId: Long): List<ProjectSubmissionResponse> = safeApiCallRaw {
        client.get("api/admin/courses/$courseId/projects")
    }

    override suspend fun reviewProjectSubmission(submissionId: Long, request: AdminReviewProjectRequestDto): Unit = safeApiCallRaw {
        client.post("api/admin/courses/projects/$submissionId/review") { setBody(request) }
    }
}
