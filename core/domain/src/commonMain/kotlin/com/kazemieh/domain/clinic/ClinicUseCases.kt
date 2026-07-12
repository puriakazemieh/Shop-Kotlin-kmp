package com.kazemieh.domain.clinic

class GetTherapistsUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.getTherapists()
}

class GetTherapistDetailUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(slug: String) = repository.getTherapist(slug)
}

class GetMyAppointmentsUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.getMyAppointments()
}

class BookAppointmentUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(slotId: Long, notes: String? = null) = repository.book(slotId, notes)
}

class CancelAppointmentUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(appointmentId: Long) = repository.cancel(appointmentId)
}

class GetSessionReceiptUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(appointmentId: Long) = repository.getReceipt(appointmentId)
}

class SubmitMoodCheckInUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(moodScore: Int, note: String? = null) = repository.submitMood(moodScore, note)
}

class GetMoodHistoryUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.getMoodHistory()
}

class RequestTherapistSwitchUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(fromTherapistId: Long, toTherapistId: Long? = null, reason: String? = null) =
        repository.requestSwitch(fromTherapistId, toTherapistId, reason)
}

class GetMySwitchRequestsUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.getMySwitchRequests()
}

class GetClinicMessagesUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(therapistId: Long) = repository.listMessages(therapistId)
}

class SendClinicMessageUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(therapistId: Long, body: String) = repository.sendMessage(therapistId, body)
}

class GetMessagingStatusUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(therapistId: Long) = repository.messagingStatus(therapistId)
}

class GetMyHomeworkUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.myHomework()
}

class CompleteHomeworkUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(id: Long) = repository.completeHomework(id)
}

class GetMyJournalUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.myJournal()
}

class AddJournalEntryUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(content: String, sharedWithTherapistId: Long? = null) =
        repository.addJournalEntry(content, sharedWithTherapistId)
}

class DeleteJournalEntryUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteJournalEntry(id)
}

class GetTherapistMatchQuestionsUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke() = repository.matchQuestions()
}

class SubmitTherapistMatchUseCase(private val repository: ClinicRepository) {
    suspend operator fun invoke(selectedTags: List<String>) = repository.submitMatch(selectedTags)
}
