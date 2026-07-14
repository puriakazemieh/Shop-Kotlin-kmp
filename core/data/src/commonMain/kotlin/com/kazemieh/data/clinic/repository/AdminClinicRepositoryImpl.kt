package com.kazemieh.data.clinic.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.AdminAppointment
import com.kazemieh.domain.clinic.AdminAppointmentStatus
import com.kazemieh.domain.clinic.AdminClinicRepository
import com.kazemieh.domain.clinic.AdminMatchQuestionParams
import com.kazemieh.domain.clinic.AdminPatientSummary
import com.kazemieh.domain.clinic.AdminSlot
import com.kazemieh.domain.clinic.AdminSwitchRequest
import com.kazemieh.domain.clinic.AdminTherapistParams
import com.kazemieh.domain.clinic.AdminTherapistUpdateParams
import com.kazemieh.domain.clinic.ClinicMessage
import com.kazemieh.domain.clinic.ClinicOrgSeat
import com.kazemieh.domain.clinic.Homework
import com.kazemieh.domain.clinic.HomeworkStatus
import com.kazemieh.domain.clinic.JournalEntry
import com.kazemieh.domain.clinic.MessageSenderType
import com.kazemieh.domain.clinic.PatientFile
import com.kazemieh.domain.clinic.PatientFileAppointment
import com.kazemieh.domain.clinic.PatientFileTestResult
import com.kazemieh.domain.clinic.PatientNote
import com.kazemieh.domain.clinic.SwitchRequestStatus
import com.kazemieh.domain.clinic.TherapistMatchQuestion
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.network.clinic.AdminClinicApi
import com.kazemieh.network.clinic.dto.AdminAddPatientNoteRequestDto
import com.kazemieh.network.clinic.dto.AdminAddSlotRequestDto
import com.kazemieh.network.clinic.dto.AdminAppointmentResponse
import com.kazemieh.network.clinic.dto.AdminAssignHomeworkRequestDto
import com.kazemieh.network.clinic.dto.AdminConfirmAppointmentRequestDto
import com.kazemieh.network.clinic.dto.AdminCreateMatchQuestionRequestDto
import com.kazemieh.network.clinic.dto.AdminCreateTherapistRequestDto
import com.kazemieh.network.clinic.dto.AdminGenerateSlotsRequestDto
import com.kazemieh.network.clinic.dto.AdminPatientSummaryResponse
import com.kazemieh.network.clinic.dto.AdminReviewSwitchRequestDto
import com.kazemieh.network.clinic.dto.AdminSetPatientTagsRequestDto
import com.kazemieh.network.clinic.dto.AdminSlotResponse
import com.kazemieh.network.clinic.dto.AdminSwitchRequestResponse
import com.kazemieh.network.clinic.dto.AdminUpdateTherapistRequestDto
import com.kazemieh.network.clinic.dto.AssignClinicSeatRequestDto
import com.kazemieh.network.clinic.dto.BuyClinicSeatsRequestDto
import com.kazemieh.network.clinic.dto.ClinicMessageResponse
import com.kazemieh.network.clinic.dto.ClinicSeatResponse
import com.kazemieh.network.clinic.dto.HomeworkResponse
import com.kazemieh.network.clinic.dto.JournalEntryResponse
import com.kazemieh.network.clinic.dto.PatientFileAppointmentResponse
import com.kazemieh.network.clinic.dto.PatientFileTestResultResponse
import com.kazemieh.network.clinic.dto.PatientNoteResponse
import com.kazemieh.network.clinic.dto.SendMessageRequestDto
import com.kazemieh.network.clinic.dto.TherapistMatchQuestionResponse
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
                messagingProductId = params.messagingProductId,
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
                isActive = params.isActive,
                messagingProductId = params.messagingProductId
            )
        )
    }

    override suspend fun deleteTherapist(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteTherapist(id)
    }

    override suspend fun addSlot(therapistId: Long, startTime: String, endTime: String, capacity: Int): AppResult<Long> = safeApiCall {
        api.addSlot(therapistId, AdminAddSlotRequestDto(startTime = startTime, endTime = endTime, capacity = capacity))
    }

    override suspend fun generateSlots(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: Int?, capacity: Int): AppResult<Int> = safeApiCall {
        api.generateSlots(therapistId, AdminGenerateSlotsRequestDto(windowStart = windowStart, windowEnd = windowEnd, slotMinutes = slotMinutes, capacity = capacity))
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

    override suspend fun listSwitchRequests(): AppResult<List<AdminSwitchRequest>> = safeApiCall {
        api.listSwitchRequests().map { it.toDomain() }
    }

    override suspend fun reviewSwitchRequest(id: Long, approve: Boolean, adminNote: String?): AppResult<AdminSwitchRequest> = safeApiCall {
        api.reviewSwitchRequest(id, AdminReviewSwitchRequestDto(approve = approve, adminNote = adminNote)).toDomain()
    }

    override suspend fun listMessagesWithPatient(therapistId: Long, userId: Long): AppResult<List<ClinicMessage>> = safeApiCall {
        api.listMessagesWithPatient(therapistId, userId).map { it.toDomain() }
    }

    override suspend fun sendMessageToPatient(therapistId: Long, userId: Long, body: String): AppResult<ClinicMessage> = safeApiCall {
        api.sendMessageToPatient(therapistId, userId, SendMessageRequestDto(body = body)).toDomain()
    }

    override suspend fun listHomeworkForPatient(therapistId: Long, userId: Long): AppResult<List<Homework>> = safeApiCall {
        api.listHomeworkForPatient(therapistId, userId).map { it.toDomain() }
    }

    override suspend fun assignHomework(therapistId: Long, userId: Long, title: String, description: String?, dueDate: String?): AppResult<Homework> = safeApiCall {
        api.assignHomework(therapistId, userId, AdminAssignHomeworkRequestDto(title = title, description = description, dueDate = dueDate)).toDomain()
    }

    override suspend fun sharedJournal(therapistId: Long, userId: Long): AppResult<List<JournalEntry>> = safeApiCall {
        api.sharedJournal(therapistId, userId).map { it.toDomain() }
    }

    override suspend fun listMatchQuestions(): AppResult<List<TherapistMatchQuestion>> = safeApiCall {
        api.listMatchQuestions().map { it.toDomain() }
    }

    override suspend fun createMatchQuestion(params: AdminMatchQuestionParams): AppResult<TherapistMatchQuestion> = safeApiCall {
        api.createMatchQuestion(
            AdminCreateMatchQuestionRequestDto(questionText = params.questionText, tag = params.tag, displayOrder = params.displayOrder)
        ).toDomain()
    }

    override suspend fun deleteMatchQuestion(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteMatchQuestion(id)
    }

    override suspend fun buyClinicSeats(organizationId: Long, therapistId: Long, sessionCount: Int, count: Int): AppResult<List<ClinicOrgSeat>> = safeApiCall {
        api.buyClinicSeats(organizationId, BuyClinicSeatsRequestDto(therapistId = therapistId, sessionCount = sessionCount, count = count)).map { it.toDomain() }
    }

    override suspend fun listClinicSeats(organizationId: Long): AppResult<List<ClinicOrgSeat>> = safeApiCall {
        api.listClinicSeats(organizationId).map { it.toDomain() }
    }

    override suspend fun assignClinicSeat(organizationId: Long, therapistId: Long, email: String): AppResult<ClinicOrgSeat> = safeApiCall {
        api.assignClinicSeat(organizationId, AssignClinicSeatRequestDto(therapistId = therapistId, email = email)).toDomain()
    }

    private fun ClinicMessageResponse.toDomain() = ClinicMessage(
        id = id,
        senderType = when (senderType) {
            "PATIENT" -> MessageSenderType.PATIENT
            "THERAPIST" -> MessageSenderType.THERAPIST
            else -> MessageSenderType.UNKNOWN
        },
        body = body, createdAt = createdAt
    )

    private fun HomeworkResponse.toDomain() = Homework(
        id = id, therapistId = therapistId, therapistName = therapistName, title = title, description = description,
        status = when (status) {
            "ASSIGNED" -> HomeworkStatus.ASSIGNED
            "COMPLETED" -> HomeworkStatus.COMPLETED
            else -> HomeworkStatus.UNKNOWN
        },
        dueDate = dueDate, completedAt = completedAt, createdAt = createdAt
    )

    private fun JournalEntryResponse.toDomain() = JournalEntry(
        id = id, content = content, sharedWithTherapistId = sharedWithTherapistId, createdAt = createdAt
    )

    private fun TherapistMatchQuestionResponse.toDomain() = TherapistMatchQuestion(
        id = id, questionText = questionText, tag = tag
    )

    private fun ClinicSeatResponse.toDomain() = ClinicOrgSeat(
        id = id, organizationId = organizationId, therapistId = therapistId, sessionCount = sessionCount,
        assignedUserId = assignedUserId, assignedEmail = assignedEmail, assignedAt = assignedAt
    )

    private fun AdminSwitchRequestResponse.toDomain() = AdminSwitchRequest(
        id = id, userId = userId, userName = userName,
        fromTherapistId = fromTherapistId, fromTherapistName = fromTherapistName,
        toTherapistId = toTherapistId, toTherapistName = toTherapistName, reason = reason,
        status = when (status) {
            "PENDING" -> SwitchRequestStatus.PENDING
            "APPROVED" -> SwitchRequestStatus.APPROVED
            "REJECTED" -> SwitchRequestStatus.REJECTED
            else -> SwitchRequestStatus.UNKNOWN
        },
        adminNote = adminNote, createdAt = createdAt
    )

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
        id = id, startTime = startTime, endTime = endTime, isBooked = isBooked, capacity = capacity, bookedCount = bookedCount
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
