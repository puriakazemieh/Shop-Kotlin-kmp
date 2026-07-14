package com.kazemieh.domain.courserequest

import com.kazemieh.common.AppResult

interface CourseRequestRepository {
    suspend fun list(): AppResult<List<CourseRequest>>
    suspend fun mine(): AppResult<List<CourseRequest>>
    suspend fun create(title: String, description: String?): AppResult<CourseRequest>
    /** بازمی‌گرداند: (liked, likeCount) به‌روزشده. */
    suspend fun toggleLike(id: Long): AppResult<Pair<Boolean, Int>>

    // ادمین
    suspend fun adminList(): AppResult<List<CourseRequest>>
    suspend fun adminDelete(id: Long): AppResult<Unit>
}
