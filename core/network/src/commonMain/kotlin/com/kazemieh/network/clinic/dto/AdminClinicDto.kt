package com.kazemieh.network.clinic.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdResponse(val id: Long)

@Serializable
data class AdminCreateTherapistRequestDto(
    val name: String,
    val slug: String,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double = 0.0,
    val sessionDurationMinutes: Int = 45,
    val productId: Long? = null,
    val isActive: Boolean = true
)

@Serializable
data class AdminUpdateTherapistRequestDto(
    val name: String? = null,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double? = null,
    val sessionDurationMinutes: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class AdminAddSlotRequestDto(
    val startTime: String,
    val endTime: String
)

@Serializable
data class AdminConfirmAppointmentRequestDto(
    val videoRoomUrl: String
)

@Serializable
data class AdminSlotResponse(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean
)

@Serializable
data class AdminAppointmentResponse(
    val id: Long,
    val userId: Long,
    val therapistId: Long,
    val therapistName: String,
    val status: String,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String? = null,
    val notes: String? = null
)
