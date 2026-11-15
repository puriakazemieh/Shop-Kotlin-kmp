package com.kazemieh.data.courserequest.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.courserequest.CourseRequest
import com.kazemieh.domain.courserequest.CourseRequestRepository
import com.kazemieh.network.courserequest.CourseRequestApi
import com.kazemieh.network.courserequest.dto.CourseRequestResponse
import com.kazemieh.network.courserequest.dto.CreateCourseRequestRequestDto
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.data.common.guardOrError

class CourseRequestRepositoryImpl(
    private val api: CourseRequestApi,
    private val guard: FeatureUseCaseGuard? = null
) : CourseRequestRepository {

    override suspend fun list(): AppResult<List<CourseRequest>> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                api.list().map { it.toDomain() }
            }
        }

    override suspend fun mine(): AppResult<List<CourseRequest>> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                api.mine().map { it.toDomain() }
            }
        }

    override suspend fun create(title: String, description: String?): AppResult<CourseRequest> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                api.create(CreateCourseRequestRequestDto(title = title, description = description)).toDomain()
            }
        }

    override suspend fun toggleLike(id: Long): AppResult<Pair<Boolean, Int>> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                val r = api.toggleLike(id)
                r.liked to r.likeCount
            }
        }

    override suspend fun adminList(): AppResult<List<CourseRequest>> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                api.adminList().map { it.toDomain() }
            }
        }

    override suspend fun adminDelete(id: Long): AppResult<Unit> =
        guard.guardOrError("academy.core") {
            safeApiCall {
                api.adminDelete(id)
            }
        }

    private fun CourseRequestResponse.toDomain() = CourseRequest(
        id = id,
        title = title,
        description = description,
        requesterName = requesterName,
        likeCount = likeCount,
        liked = liked,
        fulfilled = fulfilled,
        createdAt = createdAt
    )
}
