package com.kazemieh.data.clinic.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.Appointment
import com.kazemieh.domain.clinic.AppointmentStatus
import com.kazemieh.domain.clinic.AvailabilitySlot
import com.kazemieh.domain.clinic.ClinicRepository
import com.kazemieh.domain.clinic.MoodCheckIn
import com.kazemieh.domain.clinic.SessionReceipt
import com.kazemieh.domain.clinic.SwitchRequest
import com.kazemieh.domain.clinic.SwitchRequestStatus
import com.kazemieh.domain.clinic.TherapistDetail
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.network.clinic.ClinicApi
import com.kazemieh.network.clinic.dto.AppointmentResponse
import com.kazemieh.network.clinic.dto.BookAppointmentRequestDto
import com.kazemieh.network.clinic.dto.MoodCheckInRequestDto
import com.kazemieh.network.clinic.dto.MoodCheckInResponse
import com.kazemieh.network.clinic.dto.SessionReceiptResponse
import com.kazemieh.network.clinic.dto.SlotResponse
import com.kazemieh.network.clinic.dto.SwitchRequestRequestDto
import com.kazemieh.network.clinic.dto.SwitchRequestResponse
import com.kazemieh.network.clinic.dto.TherapistDetailResponse
import com.kazemieh.network.clinic.dto.TherapistSummaryResponse
import com.kazemieh.network.common.safeApiCall

class ClinicRepositoryImpl(
    private val api: ClinicApi
) : ClinicRepository {

    override suspend fun getTherapists(): AppResult<List<TherapistSummary>> = safeApiCall {
        api.getTherapists().map { it.toDomain() }
    }

    override suspend fun getTherapist(slug: String): AppResult<TherapistDetail> = safeApiCall {
        api.getTherapist(slug).toDomain()
    }

    override suspend fun getMyAppointments(): AppResult<List<Appointment>> = safeApiCall {
        api.getMyAppointments().map { it.toDomain() }
    }

    override suspend fun book(slotId: Long, notes: String?): AppResult<Appointment> = safeApiCall {
        api.book(BookAppointmentRequestDto(slotId = slotId, notes = notes)).toDomain()
    }

    override suspend fun cancel(appointmentId: Long): AppResult<Unit> = safeApiCall {
        api.cancel(appointmentId)
    }

    override suspend fun getReceipt(appointmentId: Long): AppResult<SessionReceipt> = safeApiCall {
        api.getReceipt(appointmentId).toDomain()
    }

    override suspend fun submitMood(moodScore: Int, note: String?): AppResult<MoodCheckIn> = safeApiCall {
        api.submitMood(MoodCheckInRequestDto(moodScore = moodScore, note = note)).toDomain()
    }

    override suspend fun getMoodHistory(): AppResult<List<MoodCheckIn>> = safeApiCall {
        api.getMoodHistory().map { it.toDomain() }
    }

    override suspend fun requestSwitch(fromTherapistId: Long, toTherapistId: Long?, reason: String?): AppResult<SwitchRequest> = safeApiCall {
        api.requestSwitch(
            SwitchRequestRequestDto(fromTherapistId = fromTherapistId, toTherapistId = toTherapistId, reason = reason)
        ).toDomain()
    }

    override suspend fun getMySwitchRequests(): AppResult<List<SwitchRequest>> = safeApiCall {
        api.getMySwitchRequests().map { it.toDomain() }
    }

    private fun SessionReceiptResponse.toDomain() = SessionReceipt(
        appointmentId = appointmentId, patientName = patientName, therapistName = therapistName,
        therapistSpecialty = therapistSpecialty, sessionMode = sessionMode, sessionDate = sessionDate,
        sessionDurationMinutes = sessionDurationMinutes, amountPaid = amountPaid
    )

    private fun MoodCheckInResponse.toDomain() = MoodCheckIn(
        id = id, moodScore = moodScore, note = note, createdAt = createdAt
    )

    private fun SwitchRequestResponse.toDomain() = SwitchRequest(
        id = id, fromTherapistId = fromTherapistId, fromTherapistName = fromTherapistName,
        toTherapistId = toTherapistId, toTherapistName = toTherapistName, reason = reason,
        status = when (status) {
            "PENDING" -> SwitchRequestStatus.PENDING
            "APPROVED" -> SwitchRequestStatus.APPROVED
            "REJECTED" -> SwitchRequestStatus.REJECTED
            else -> SwitchRequestStatus.UNKNOWN
        },
        adminNote = adminNote, createdAt = createdAt
    )

    private fun TherapistSummaryResponse.toDomain() = TherapistSummary(
        id = id, name = name, slug = slug, specialty = specialty, photoUrl = photoUrl,
        sessionPrice = sessionPrice, availableSlotCount = availableSlotCount,
        requiresPurchase = requiresPurchase, productSlug = productSlug, sessionCredits = sessionCredits
    )

    private fun SlotResponse.toDomain() = AvailabilitySlot(
        id = id, startTime = startTime, endTime = endTime, dayLabel = dayLabel, timeLabel = timeLabel
    )

    private fun TherapistDetailResponse.toDomain() = TherapistDetail(
        id = id, name = name, slug = slug, specialty = specialty, bio = bio, photoUrl = photoUrl,
        sessionPrice = sessionPrice, sessionDurationMinutes = sessionDurationMinutes,
        slots = slots.map { it.toDomain() },
        requiresPurchase = requiresPurchase, productSlug = productSlug, sessionCredits = sessionCredits,
        mode = mode, location = location, productId = productId
    )

    private fun AppointmentResponse.toDomain() = Appointment(
        id = id,
        therapistName = therapistName,
        therapistPhotoUrl = therapistPhotoUrl,
        status = when (status) {
            "PENDING" -> AppointmentStatus.PENDING
            "CONFIRMED" -> AppointmentStatus.CONFIRMED
            "COMPLETED" -> AppointmentStatus.COMPLETED
            "CANCELLED" -> AppointmentStatus.CANCELLED
            else -> AppointmentStatus.UNKNOWN
        },
        dayLabel = dayLabel,
        timeLabel = timeLabel,
        videoRoomUrl = videoRoomUrl,
        canJoin = canJoin,
        notes = notes,
        mode = mode
    )
}
