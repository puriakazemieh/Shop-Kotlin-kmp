package com.kazemieh.network.courserequest

import com.kazemieh.network.courserequest.dto.CourseRequestResponse
import com.kazemieh.network.courserequest.dto.CreateCourseRequestRequestDto
import com.kazemieh.network.courserequest.dto.ToggleLikeResponse

interface CourseRequestApi {
    suspend fun list(): List<CourseRequestResponse>
    suspend fun mine(): List<CourseRequestResponse>
    suspend fun create(request: CreateCourseRequestRequestDto): CourseRequestResponse
    suspend fun toggleLike(id: Long): ToggleLikeResponse

    // ادمین
    suspend fun adminList(): List<CourseRequestResponse>
    suspend fun adminDelete(id: Long)
}
