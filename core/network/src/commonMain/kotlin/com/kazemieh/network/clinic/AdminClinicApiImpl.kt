package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class AdminClinicApiImpl(
    private val client: HttpClient
) : AdminClinicApi {

    override suspend fun listTherapists(): List<TherapistSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/therapists")
    }

    override suspend fun createTherapist(request: AdminCreateTherapistRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists") { setBody(request) }
        }.id

    override suspend fun updateTherapist(id: Long, request: AdminUpdateTherapistRequestDto): Unit = safeApiCallRaw {
        client.patch("api/admin/therapists/$id") { setBody(request) }
    }

    override suspend fun deleteTherapist(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/therapists/$id")
    }

    override suspend fun addSlot(therapistId: Long, request: AdminAddSlotRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists/$therapistId/slots") { setBody(request) }
        }.id

    override suspend fun generateSlots(therapistId: Long, request: AdminGenerateSlotsRequestDto): Int =
        safeApiCallRaw<CreatedCountResponse> {
            client.post("api/admin/therapists/$therapistId/generate-slots") { setBody(request) }
        }.created

    override suspend fun listSlots(therapistId: Long): List<AdminSlotResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/slots")
    }

    override suspend fun listAppointments(): List<AdminAppointmentResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/appointments")
    }

    override suspend fun confirmAppointment(id: Long, request: AdminConfirmAppointmentRequestDto): Unit = safeApiCallRaw {
        client.post("api/admin/therapists/appointments/$id/confirm") { setBody(request) }
    }

    override suspend fun completeAppointment(id: Long): Unit = safeApiCallRaw {
        client.post("api/admin/therapists/appointments/$id/complete")
    }

    override suspend fun listPatientNotes(appointmentId: Long): List<PatientNoteResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/appointments/$appointmentId/notes")
    }

    override suspend fun addPatientNote(appointmentId: Long, request: AdminAddPatientNoteRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists/appointments/$appointmentId/notes") { setBody(request) }
        }.id

    override suspend fun listPatients(therapistId: Long): List<AdminPatientSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/patients")
    }

    override suspend fun setPatientTags(therapistId: Long, userId: Long, request: AdminSetPatientTagsRequestDto): Unit = safeApiCallRaw {
        client.put("api/admin/therapists/$therapistId/patients/$userId/tags") { setBody(request) }
    }

    override suspend fun getPatientFile(therapistId: Long, userId: Long): PatientFileResponse = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/patients/$userId")
    }

    override suspend fun listSwitchRequests(): List<AdminSwitchRequestResponse> = safeApiCallRaw {
        client.get("api/admin/clinic/switch-requests")
    }

    override suspend fun reviewSwitchRequest(id: Long, request: AdminReviewSwitchRequestDto): AdminSwitchRequestResponse = safeApiCallRaw {
        client.post("api/admin/clinic/switch-requests/$id/review") { setBody(request) }
    }

    override suspend fun listMessagesWithPatient(therapistId: Long, userId: Long): List<ClinicMessageResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/patients/$userId/messages")
    }

    override suspend fun sendMessageToPatient(therapistId: Long, userId: Long, request: SendMessageRequestDto): ClinicMessageResponse = safeApiCallRaw {
        client.post("api/admin/therapists/$therapistId/patients/$userId/messages") { setBody(request) }
    }

    override suspend fun listHomeworkForPatient(therapistId: Long, userId: Long): List<HomeworkResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/patients/$userId/homework")
    }

    override suspend fun assignHomework(therapistId: Long, userId: Long, request: AdminAssignHomeworkRequestDto): HomeworkResponse = safeApiCallRaw {
        client.post("api/admin/therapists/$therapistId/patients/$userId/homework") { setBody(request) }
    }

    override suspend fun sharedJournal(therapistId: Long, userId: Long): List<JournalEntryResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/patients/$userId/journal")
    }

    override suspend fun listMatchQuestions(): List<TherapistMatchQuestionResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/match-questions")
    }

    override suspend fun createMatchQuestion(request: AdminCreateMatchQuestionRequestDto): TherapistMatchQuestionResponse = safeApiCallRaw {
        client.post("api/admin/therapists/match-questions") { setBody(request) }
    }

    override suspend fun deleteMatchQuestion(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/therapists/match-questions/$id")
    }

    override suspend fun buyClinicSeats(organizationId: Long, request: BuyClinicSeatsRequestDto): List<ClinicSeatResponse> = safeApiCallRaw {
        client.post("api/admin/organizations/$organizationId/clinic-seats") { setBody(request) }
    }

    override suspend fun listClinicSeats(organizationId: Long): List<ClinicSeatResponse> = safeApiCallRaw {
        client.get("api/admin/organizations/$organizationId/clinic-seats")
    }

    override suspend fun assignClinicSeat(organizationId: Long, request: AssignClinicSeatRequestDto): ClinicSeatResponse = safeApiCallRaw {
        client.post("api/admin/organizations/$organizationId/clinic-seats/assign") { setBody(request) }
    }
}
