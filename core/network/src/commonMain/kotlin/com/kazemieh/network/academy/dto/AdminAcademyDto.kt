package com.kazemieh.network.academy.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdResponse(val id: Long)

@Serializable
data class AdminCreateCourseRequestDto(
    val title: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double = 0.0,
    val discountedPrice: Double? = null,
    val productId: Long? = null,
    val isPublished: Boolean = true,
    val courseType: String = "COURSE",
    val format: String = "ONLINE_RECORDED",
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: String? = null
)

@Serializable
data class AdminUpdateCourseRequestDto(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val instructor: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val isPublished: Boolean? = null
)

@Serializable
data class AdminCreateSectionRequestDto(
    val title: String,
    val sortOrder: Int = 0
)

@Serializable
data class AdminCreateLessonRequestDto(
    val title: String,
    val videoUrl: String? = null,
    val durationSeconds: Int = 0,
    val sortOrder: Int = 0,
    val isFreePreview: Boolean = false
)
