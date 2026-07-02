package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

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
}
