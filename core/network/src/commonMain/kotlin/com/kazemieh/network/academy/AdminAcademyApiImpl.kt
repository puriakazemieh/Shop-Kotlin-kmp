package com.kazemieh.network.academy

import com.kazemieh.network.academy.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class AdminAcademyApiImpl(
    private val client: HttpClient
) : AdminAcademyApi {

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
}
