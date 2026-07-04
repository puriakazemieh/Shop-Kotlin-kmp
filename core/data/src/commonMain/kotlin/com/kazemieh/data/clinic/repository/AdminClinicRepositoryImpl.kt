package com.kazemieh.data.clinic.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.AdminAppointment
import com.kazemieh.domain.clinic.AdminAppointmentStatus
import com.kazemieh.domain.clinic.AdminClinicRepository
import com.kazemieh.domain.clinic.AdminPatientSummary
import com.kazemieh.domain.clinic.AdminSlot
import com.kazemieh.domain.clinic.AdminTherapistParams
import com.kazemieh.domain.clinic.AdminTherapistUpdateParams
import com.kazemieh.domain.clinic.PatientFile
import com.kazemieh.domain.clinic.PatientFileAppointment
import com.kazemieh.domain.clinic.PatientFileTestResult
import com.kazemieh.domain.clinic.PatientNote
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.network.clinic.AdminClinicApi
import com.kazemieh.network.clinic.dto.AdminAddPatientNoteRequestDto
import com.kazemieh.network.clinic.dto.AdminAddSlotRequestDto
import com.kazemieh.network.clinic.dto.AdminAppointmentResponse
import com.kazemieh.network.clinic.dto.AdminConfirmAppointmentRequestDto
import com.kazemieh.network.clinic.dto.AdminCreateTherapistRequestDto
import com.kazemieh.network.clinic.dto.AdminGenerateSlotsRequestDto
import com.kazemieh.network.clinic.dto.AdminPatientSummaryResponse
import com.kazemieh.network.clinic.dto.AdminSetPatientTagsRequestDto
import com.kazemieh.network.clinic.dto.AdminSlotResponse
import com.kazemieh.network.clinic.dto.AdminUpdateTherapistRequestDto
import com.kazemieh.network.clinic.dto.PatientFileAppointmentResponse
import com.kazemieh.network.clinic.dto.PatientFileTestResultResponse
import com.kazemieh.network.clinic.dto.PatientNoteResponse
import com.kazemieh.network.clinic.dto.TherapistSummaryResponse
import com.kazemieh.network.common.safeApiCall

class AdminClinicRepositoryImpl(
    private val api: AdminClinicApi
) : AdminClinicRepository {

    override suspend fun listTherapists(): AppResult<List<TherapistSummary>> = safeApiCall {
        api.listTherapists().map { it.toDomain() }
    }

    override suspend fun createTherapist(params: AdminTherapistParams): AppResult<Long> = safeApiCall {
        api.createTherapist(
            AdminCreateTherapistRequestDto(
                name = params.name,
                slug = params.slug,
                specialty = params.specialty,
                bio = params.bio,
                photoUrl = params.photoUrl,
                sessionPrice = params.sessionPrice,
                sessionDurationMinutes = params.sessionDurationMinutes,
                productId = params.productId,
                isActive = params.isActive,
                mode = params.mode,
                location = params.location
            )
        )
    }

    override suspend fun updateTherapist(id: Long, params: AdminTherapistUpdateParams): AppResult<Unit> = safeApiCall {
        api.updateTherapist(
            id,
            AdminUpdateTherapistRequestDto(
                name = params.name,
                specialty = params.specialty,
                bio = params.bio,
                photoUrl = params.photoUrl,
                sessionPrice = params.sessionPrice,
                sessionDurationMinutes = params.sessionDurationMinutes,
                isActive = params.isActive
            )
        )
    }

    override suspend fun deleteTherapist(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteTherapist(id)
    }

    override suspend fun addSlot(therapistId: Long, startTime: String, endTime: String): AppResult<Long> = safeApiCall {
        api.addSlot(therapistId, AdminAddSlotRequestDto(startTime = startTime, endTime = endTime))
    }

    override suspend fun generateSlots(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: Int?): AppResult<Int> = safeApiCall {
        api.generateSlots(therapistId, AdminGenerateSlotsRequestDto(windowStart = windowStart, windowEnd = windowEnd, slotMinutes = slotMinutes))
    }

    override suspend fun listSlots(therapistId: Long): AppResult<List<AdminSlot>> = safeApiCall {
        api.listSlots(therapistId).map { it.toDomain() }
    }

    override suspend fun listAppointments(): AppResult<List<AdminAppointment>> = safeApiCall {
        api.listAppointments().map { it.toDomain() }
    }

    override suspend fun confirmAppointment(id: Long, videoRoomUrl: String): AppResult<Unit> = safeApiCall {
        api.confirmAppointment(id, AdminConfirmAppointmentRequestDto(videoRoomUrl = videoRoomUrl))
    }

    override suspend fun completeAppointment(id: Long): AppResult<Unit> = safeApiCall {
        api.completeAppointment(id)
    }

    override suspend fun listPatientNotes(appointmentId: Long): AppResult<List<PatientNote>> = safeApiCall {
        api.listPatientNotes(appointmentId).map { it.toDomain() }
    }

    override suspend fun addPatientNote(appointmentId: Long, note: String): AppResult<Long> = safeApiCall {
        api.addPatientNote(appointmentId, AdminAddPatientNoteRequestDto(note = note))
    }

    override suspend fun listPatients(therapistId: Long): AppResult<List<AdminPatientSummary>> = safeApiCall {
        api.listPatients(therapistId).map { it.toDomain() }
    }

    override suspend fun setPatientTags(therapistId: Long, userId: Long, tags: List<String>): AppResult<Unit> = safeApiCall {
        api.setPatientTags(therapistId, userId, AdminSetPatientTagsRequestDto(tags = tags))
    }

    override suspend fun getPatientFile(therapistId: Long, userId: Long): AppResult<PatientFile> = safeApiCall {
        val r = api.getPatientFile(therapistId, userId)
        PatientFile(
            userId = r.userId, userName = r.userName, therapistId = r.therapistId, tags = r.tags,
            appointments = r.appointments.map { it.toDomain() },
            testResults = r.testResults.map { it.toDomain() }
        )
    }

    private fun AdminPatientSummaryResponse.toDomain() = AdminPatientSummary(
        userId = userId, userName = userName, therapistId = therapistId,
        appointmentCount = appointmentCount, lastAppointmentAt = lastAppointmentAt, tags = tags
    )

    private fun PatientFileAppointmentResponse.toDomain() = PatientFileAppointment(
        id = id,
        status = when (status) {
            "PENDING" -> AdminAppointmentStatus.PENDING
            "CONFIRMED" -> AdminAppointmentStatus.CONFIRMED
            "COMPLETED" -> AdminAppointmentStatus.COMPLETED
            "CANCELLED" -> AdminAppointmentStatus.CANCELLED
            else -> AdminAppointmentStatus.UNKNOWN
        },
        dayLabel = dayLabel, timeLabel = timeLabel,
        notes = notes.map { PatientNote(id = it.id, appointmentId = it.appointmentId, counselorId = it.counselorId, note = it.note, createdAt = it.createdAt) }
    )

    private fun PatientFileTestResultResponse.toDomain() = PatientFileTestResult(
        testTitle = testTitle, totalScore = totalScore, interpretation = interpretation, completedAt = completedAt
    )

    private fun PatientNoteResponse.toDomain() = PatientNote(
        id = id, appointmentId = appointmentId, counselorId = counselorId, note = note, createdAt = createdAt
    )

    private fun TherapistSummaryResponse.toDomain() = TherapistSummary(
        id = id, name = name, slug = slug, specialty = specialty, photoUrl = photoUrl,
        sessionPrice = sessionPrice, availableSlotCount = availableSlotCount,
        requiresPurchase = requiresPurchase, productSlug = productSlug, sessionCredits = sessionCredits
    )

    private fun AdminSlotResponse.toDomain() = AdminSlot(
        id = id, startTime = startTime, endTime = endTime, isBooked = isBooked
    )

    private fun AdminAppointmentResponse.toDomain() = AdminAppointment(
        id = id, userId = userId, therapistId = therapistId, therapistName = therapistName,
        status = when (status) {
            "PENDING" -> AdminAppointmentStatus.PENDING
            "CONFIRMED" -> AdminAppointmentStatus.CONFIRMED
            "COMPLETED" -> AdminAppointmentStatus.COMPLETED
            "CANCELLED" -> AdminAppointmentStatus.CANCELLED
            else -> AdminAppointmentStatus.UNKNOWN
        },
        dayLabel = dayLabel, timeLabel = timeLabel, videoRoomUrl = videoRoomUrl, notes = notes, mode = mode
    )
}
