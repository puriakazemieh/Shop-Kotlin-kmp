package com.kazemieh.domain.courserequest

data class CourseRequest(
    val id: Long,
    val title: String,
    val description: String? = null,
    val requesterName: String? = null,
    val likeCount: Int = 0,
    val liked: Boolean = false,
    val fulfilled: Boolean = false,
    val createdAt: String? = null
)
