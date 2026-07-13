package com.kazemieh.network.clinic.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdResponse(val id: Long)

@Serializable
data class CreatedCountResponse(val created: Int)

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
    val messagingProductId: Long? = null,
    @SerialName("active") val isActive: Boolean = true,
    val mode: String = "ONLINE",
    val location: String? = null
)

@Serializable
data class AdminGenerateSlotsRequestDto(
    val windowStart: String,
    val windowEnd: String,
    val slotMinutes: Int? = null,
    val capacity: Int = 1
)

@Serializable
data class AdminUpdateTherapistRequestDto(
    val name: String? = null,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double? = null,
    val sessionDurationMinutes: Int? = null,
    @SerialName("active") val isActive: Boolean? = null,
    val messagingProductId: Long? = null
)

@Serializable
data class AdminAddSlotRequestDto(
    val startTime: String,
    val endTime: String,
    val capacity: Int = 1
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
    @SerialName("booked") val isBooked: Boolean,
    val capacity: Int = 1,
    val bookedCount: Int = 0
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
    val notes: String? = null,
    val mode: String = "ONLINE"
)

// ---------- Patient notes (حساس) ----------
@Serializable
data class AdminAddPatientNoteRequestDto(
    val note: String
)

@Serializable
data class PatientNoteResponse(
    val id: Long,
    val appointmentId: Long,
    val counselorId: Long,
    val note: String,
    val createdAt: String
)

// ---------- CRM + patient file ----------
@Serializable
data class AdminPatientSummaryResponse(
    val userId: Long,
    val userName: String,
    val therapistId: Long,
    val appointmentCount: Int,
    val lastAppointmentAt: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class AdminSetPatientTagsRequestDto(
    val tags: List<String> = emptyList()
)

@Serializable
data class PatientFileAppointmentResponse(
    val id: Long,
    val status: String,
    val dayLabel: String,
    val timeLabel: String,
    val notes: List<PatientNoteResponse> = emptyList()
)

@Serializable
data class PatientFileTestResultResponse(
    val testTitle: String,
    val totalScore: Int? = null,
    val interpretation: String? = null,
    val completedAt: String? = null
)

@Serializable
data class PatientFileResponse(
    val userId: Long,
    val userName: String,
    val therapistId: Long,
    val tags: List<String> = emptyList(),
    val appointments: List<PatientFileAppointmentResponse> = emptyList(),
    val testResults: List<PatientFileTestResultResponse> = emptyList()
)

// ---------- درخواست‌های تعویضِ درمانگر (Phase X) ----------
@Serializable
data class AdminSwitchRequestResponse(
    val id: Long,
    val userId: Long,
    val userName: String? = null,
    val fromTherapistId: Long,
    val fromTherapistName: String,
    val toTherapistId: Long? = null,
    val toTherapistName: String? = null,
    val reason: String? = null,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AdminReviewSwitchRequestDto(
    val approve: Boolean,
    val adminNote: String? = null
)

// ---------- تکلیف/تمرین (Phase Y — از دیدِ ادمین) ----------
@Serializable
data class AdminAssignHomeworkRequestDto(
    val title: String,
    val description: String? = null,
    val dueDate: String? = null
)

// ---------- مدیریتِ پرسشنامه‌ی تطبیقِ درمانگر (Phase Y) ----------
@Serializable
data class AdminCreateMatchQuestionRequestDto(
    val questionText: String,
    val tag: String,
    val displayOrder: Int = 0
)

// ---------- بسته‌ی مشاوره‌ی سازمانی (Phase Y) ----------
@Serializable
data class BuyClinicSeatsRequestDto(
    val therapistId: Long,
    val sessionCount: Int = 1,
    val count: Int = 1
)

@Serializable
data class AssignClinicSeatRequestDto(
    val therapistId: Long,
    val email: String
)

@Serializable
data class ClinicSeatResponse(
    val id: Long,
    val organizationId: Long,
    val therapistId: Long,
    val sessionCount: Int,
    val assignedUserId: Long? = null,
    val assignedEmail: String? = null,
    val assignedAt: String? = null
)
