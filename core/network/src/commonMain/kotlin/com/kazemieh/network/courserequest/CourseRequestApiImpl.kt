package com.kazemieh.network.courserequest

import com.kazemieh.network.common.safeApiCallRaw
import com.kazemieh.network.courserequest.dto.CourseRequestResponse
import com.kazemieh.network.courserequest.dto.CreateCourseRequestRequestDto
import com.kazemieh.network.courserequest.dto.ToggleLikeResponse
import io.ktor.client.*
import io.ktor.client.request.*

class CourseRequestApiImpl(
    private val client: HttpClient
) : CourseRequestApi {

    override suspend fun list(): List<CourseRequestResponse> = safeApiCallRaw {
        client.get("api/course-requests")
    }

    override suspend fun mine(): List<CourseRequestResponse> = safeApiCallRaw {
        client.get("api/course-requests/mine")
    }

    override suspend fun create(request: CreateCourseRequestRequestDto): CourseRequestResponse = safeApiCallRaw {
        client.post("api/course-requests") { setBody(request) }
    }

    override suspend fun toggleLike(id: Long): ToggleLikeResponse = safeApiCallRaw {
        client.post("api/course-requests/$id/like")
    }

    override suspend fun adminList(): List<CourseRequestResponse> = safeApiCallRaw {
        client.get("api/admin/course-requests")
    }

    override suspend fun adminDelete(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/course-requests/$id")
    }
}
