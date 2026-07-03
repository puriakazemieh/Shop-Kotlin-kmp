package com.kazemieh.domain.clinic

data class TherapistSummary(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val photoUrl: String?,
    val sessionPrice: Double,
    val availableSlotCount: Int
)

data class AvailabilitySlot(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val dayLabel: String,
    val timeLabel: String
)

data class TherapistDetail(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val bio: String?,
    val photoUrl: String?,
    val sessionPrice: Double,
    val sessionDurationMinutes: Int,
    val slots: List<AvailabilitySlot>
)

enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED, UNKNOWN }

data class Appointment(
    val id: Long,
    val therapistName: String,
    val therapistPhotoUrl: String?,
    val status: AppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String?,
    val canJoin: Boolean,
    val notes: String?
)
