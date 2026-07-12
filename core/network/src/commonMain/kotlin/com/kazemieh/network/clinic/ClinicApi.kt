package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*

interface ClinicApi {
    suspend fun getTherapists(): List<TherapistSummaryResponse>
    suspend fun getTherapist(slug: String): TherapistDetailResponse
    suspend fun getMyAppointments(): List<AppointmentResponse>
    suspend fun book(request: BookAppointmentRequestDto): AppointmentResponse
    suspend fun cancel(appointmentId: Long)
    suspend fun getReceipt(appointmentId: Long): SessionReceiptResponse
    suspend fun submitMood(request: MoodCheckInRequestDto): MoodCheckInResponse
    suspend fun getMoodHistory(): List<MoodCheckInResponse>
    suspend fun requestSwitch(request: SwitchRequestRequestDto): SwitchRequestResponse
    suspend fun getMySwitchRequests(): List<SwitchRequestResponse>

    suspend fun listMessages(therapistId: Long): List<ClinicMessageResponse>
    suspend fun sendMessage(therapistId: Long, request: SendMessageRequestDto): ClinicMessageResponse
    suspend fun messagingStatus(therapistId: Long): MessagingPlanStatusResponse

    suspend fun myHomework(): List<HomeworkResponse>
    suspend fun completeHomework(id: Long): HomeworkResponse

    suspend fun myJournal(): List<JournalEntryResponse>
    suspend fun addJournalEntry(request: JournalEntryRequestDto): JournalEntryResponse
    suspend fun deleteJournalEntry(id: Long)

    suspend fun matchQuestions(): List<TherapistMatchQuestionResponse>
    suspend fun submitMatch(request: SubmitTherapistMatchRequestDto): List<TherapistMatchResultResponse>
}
