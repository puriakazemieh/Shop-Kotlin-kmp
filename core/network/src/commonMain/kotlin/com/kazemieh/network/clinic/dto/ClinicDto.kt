package com.kazemieh.network.clinic.dto

import kotlinx.serialization.Serializable

@Serializable
data class TherapistSummaryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double,
    val availableSlotCount: Int
)

@Serializable
data class SlotResponse(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val dayLabel: String,
    val timeLabel: String
)

@Serializable
data class TherapistDetailResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double,
    val sessionDurationMinutes: Int,
    val slots: List<SlotResponse>
)

@Serializable
data class BookAppointmentRequestDto(
    val slotId: Long,
    val notes: String? = null
)

@Serializable
data class AppointmentResponse(
    val id: Long,
    val therapistName: String,
    val therapistPhotoUrl: String? = null,
    val status: String,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String? = null,
    val canJoin: Boolean = false,
    val notes: String? = null
)
