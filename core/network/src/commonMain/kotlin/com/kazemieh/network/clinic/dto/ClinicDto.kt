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
    val availableSlotCount: Int,
    val requiresPurchase: Boolean = false,
    val productSlug: String? = null,
    val sessionCredits: Int = 0
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
    val slots: List<SlotResponse>,
    val requiresPurchase: Boolean = false,
    val productSlug: String? = null,
    val sessionCredits: Int = 0,
    val mode: String = "ONLINE",
    val location: String? = null,
    val productId: Long? = null
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
    val notes: String? = null,
    val mode: String = "ONLINE"
)

// ---------- ثبتِ روزانه‌ی خلق‌وخو (Phase X) ----------
@Serializable
data class MoodCheckInRequestDto(
    val moodScore: Int,
    val note: String? = null
)

@Serializable
data class MoodCheckInResponse(
    val id: Long,
    val moodScore: Int,
    val note: String? = null,
    val createdAt: String? = null
)

// ---------- درخواستِ تعویضِ درمانگر (Phase X) ----------
@Serializable
data class SwitchRequestRequestDto(
    val fromTherapistId: Long,
    val toTherapistId: Long? = null,
    val reason: String? = null
)

@Serializable
data class SwitchRequestResponse(
    val id: Long,
    val fromTherapistId: Long,
    val fromTherapistName: String,
    val toTherapistId: Long? = null,
    val toTherapistName: String? = null,
    val reason: String? = null,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String? = null
)

// ---------- رسیدِ جلسه (Phase X) ----------
@Serializable
data class SessionReceiptResponse(
    val appointmentId: Long,
    val patientName: String,
    val therapistName: String,
    val therapistSpecialty: String? = null,
    val sessionMode: String,
    val sessionDate: String,
    val sessionDurationMinutes: Int,
    val amountPaid: Double
)
