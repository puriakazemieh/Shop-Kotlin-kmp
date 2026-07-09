package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class ClinicApiImpl(
    private val client: HttpClient
) : ClinicApi {

    override suspend fun getTherapists(): List<TherapistSummaryResponse> = safeApiCallRaw {
        client.get("api/therapists")
    }

    override suspend fun getTherapist(slug: String): TherapistDetailResponse = safeApiCallRaw {
        client.get("api/therapists/$slug")
    }

    override suspend fun getMyAppointments(): List<AppointmentResponse> = safeApiCallRaw {
        client.get("api/clinic/my-appointments")
    }

    override suspend fun book(request: BookAppointmentRequestDto): AppointmentResponse = safeApiCallRaw {
        client.post("api/clinic/appointments") {
            setBody(request)
        }
    }

    override suspend fun cancel(appointmentId: Long): Unit = safeApiCallRaw {
        client.post("api/clinic/appointments/$appointmentId/cancel")
    }

    override suspend fun getReceipt(appointmentId: Long): SessionReceiptResponse = safeApiCallRaw {
        client.get("api/clinic/appointments/$appointmentId/receipt")
    }

    override suspend fun submitMood(request: MoodCheckInRequestDto): MoodCheckInResponse = safeApiCallRaw {
        client.post("api/clinic/mood-checkins") { setBody(request) }
    }

    override suspend fun getMoodHistory(): List<MoodCheckInResponse> = safeApiCallRaw {
        client.get("api/clinic/mood-checkins")
    }

    override suspend fun requestSwitch(request: SwitchRequestRequestDto): SwitchRequestResponse = safeApiCallRaw {
        client.post("api/clinic/switch-requests") { setBody(request) }
    }

    override suspend fun getMySwitchRequests(): List<SwitchRequestResponse> = safeApiCallRaw {
        client.get("api/clinic/switch-requests/mine")
    }

    override suspend fun listMessages(therapistId: Long): List<ClinicMessageResponse> = safeApiCallRaw {
        client.get("api/clinic/therapists/$therapistId/messages")
    }

    override suspend fun sendMessage(therapistId: Long, request: SendMessageRequestDto): ClinicMessageResponse = safeApiCallRaw {
        client.post("api/clinic/therapists/$therapistId/messages") { setBody(request) }
    }

    override suspend fun messagingStatus(therapistId: Long): MessagingPlanStatusResponse = safeApiCallRaw {
        client.get("api/clinic/therapists/$therapistId/messaging-status")
    }

    override suspend fun myHomework(): List<HomeworkResponse> = safeApiCallRaw {
        client.get("api/clinic/homework")
    }

    override suspend fun completeHomework(id: Long): HomeworkResponse = safeApiCallRaw {
        client.post("api/clinic/homework/$id/complete")
    }

    override suspend fun myJournal(): List<JournalEntryResponse> = safeApiCallRaw {
        client.get("api/clinic/journal")
    }

    override suspend fun addJournalEntry(request: JournalEntryRequestDto): JournalEntryResponse = safeApiCallRaw {
        client.post("api/clinic/journal") { setBody(request) }
    }

    override suspend fun deleteJournalEntry(id: Long): Unit = safeApiCallRaw {
        client.delete("api/clinic/journal/$id")
    }

    override suspend fun matchQuestions(): List<TherapistMatchQuestionResponse> = safeApiCallRaw {
        client.get("api/clinic/therapist-match/questions")
    }

    override suspend fun submitMatch(request: SubmitTherapistMatchRequestDto): List<TherapistMatchResultResponse> = safeApiCallRaw {
        client.post("api/clinic/therapist-match/submit") { setBody(request) }
    }
}
