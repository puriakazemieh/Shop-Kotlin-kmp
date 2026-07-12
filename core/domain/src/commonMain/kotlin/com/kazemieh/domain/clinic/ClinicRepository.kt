package com.kazemieh.domain.clinic

import com.kazemieh.common.AppResult

interface ClinicRepository {
    suspend fun getTherapists(): AppResult<List<TherapistSummary>>
    suspend fun getTherapist(slug: String): AppResult<TherapistDetail>
    suspend fun getMyAppointments(): AppResult<List<Appointment>>
    suspend fun book(slotId: Long, notes: String?): AppResult<Appointment>
    suspend fun cancel(appointmentId: Long): AppResult<Unit>
    suspend fun getReceipt(appointmentId: Long): AppResult<SessionReceipt>
    suspend fun submitMood(moodScore: Int, note: String?): AppResult<MoodCheckIn>
    suspend fun getMoodHistory(): AppResult<List<MoodCheckIn>>
    suspend fun requestSwitch(fromTherapistId: Long, toTherapistId: Long?, reason: String?): AppResult<SwitchRequest>
    suspend fun getMySwitchRequests(): AppResult<List<SwitchRequest>>

    suspend fun listMessages(therapistId: Long): AppResult<List<ClinicMessage>>
    suspend fun sendMessage(therapistId: Long, body: String): AppResult<ClinicMessage>
    suspend fun messagingStatus(therapistId: Long): AppResult<MessagingPlanStatus>

    suspend fun myHomework(): AppResult<List<Homework>>
    suspend fun completeHomework(id: Long): AppResult<Homework>

    suspend fun myJournal(): AppResult<List<JournalEntry>>
    suspend fun addJournalEntry(content: String, sharedWithTherapistId: Long?): AppResult<JournalEntry>
    suspend fun deleteJournalEntry(id: Long): AppResult<Unit>

    suspend fun matchQuestions(): AppResult<List<TherapistMatchQuestion>>
    suspend fun submitMatch(selectedTags: List<String>): AppResult<List<TherapistMatchResult>>
}
