package com.kazemieh.network.courserequest.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseRequestResponse(
    val id: Long,
    val title: String,
    val description: String? = null,
    val requesterName: String? = null,
    val likeCount: Int = 0,
    val liked: Boolean = false,
    val fulfilled: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class CreateCourseRequestRequestDto(
    val title: String,
    val description: String? = null
)

@Serializable
data class ToggleLikeResponse(
    val liked: Boolean,
    val likeCount: Int
)
