package com.kazemieh.domain.clinic

class GetAdminTherapistsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke() = repository.listTherapists()
}

class CreateTherapistUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(params: AdminTherapistParams) = repository.createTherapist(params)
}

class UpdateTherapistUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long, params: AdminTherapistUpdateParams) = repository.updateTherapist(id, params)
}

class DeleteTherapistUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTherapist(id)
}

class AddSlotUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, startTime: String, endTime: String, capacity: Int = 1) =
        repository.addSlot(therapistId, startTime, endTime, capacity)
}

class GenerateSlotsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: Int?, capacity: Int = 1) =
        repository.generateSlots(therapistId, windowStart, windowEnd, slotMinutes, capacity)
}

class GetAdminSlotsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long) = repository.listSlots(therapistId)
}

class GetAdminAppointmentsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke() = repository.listAppointments()
}

class ConfirmAppointmentUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long, videoRoomUrl: String) = repository.confirmAppointment(id, videoRoomUrl)
}

class CompleteAppointmentUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long) = repository.completeAppointment(id)
}

class GetPatientNotesUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(appointmentId: Long) = repository.listPatientNotes(appointmentId)
}

class AddPatientNoteUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(appointmentId: Long, note: String) = repository.addPatientNote(appointmentId, note)
}

class GetAdminPatientsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long) = repository.listPatients(therapistId)
}

class SetPatientTagsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long, tags: List<String>) =
        repository.setPatientTags(therapistId, userId, tags)
}

class GetPatientFileUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long) = repository.getPatientFile(therapistId, userId)
}

class GetAdminSwitchRequestsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke() = repository.listSwitchRequests()
}

class ReviewSwitchRequestUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long, approve: Boolean, adminNote: String? = null) =
        repository.reviewSwitchRequest(id, approve, adminNote)
}

class GetAdminMessagesUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long) = repository.listMessagesWithPatient(therapistId, userId)
}

class SendAdminMessageUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long, body: String) = repository.sendMessageToPatient(therapistId, userId, body)
}

class GetAdminHomeworkUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long) = repository.listHomeworkForPatient(therapistId, userId)
}

class AssignHomeworkUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long, title: String, description: String? = null, dueDate: String? = null) =
        repository.assignHomework(therapistId, userId, title, description, dueDate)
}

class GetSharedJournalUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, userId: Long) = repository.sharedJournal(therapistId, userId)
}

class GetAdminMatchQuestionsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke() = repository.listMatchQuestions()
}

class CreateMatchQuestionUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(params: AdminMatchQuestionParams) = repository.createMatchQuestion(params)
}

class DeleteMatchQuestionUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteMatchQuestion(id)
}

class BuyClinicSeatsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(organizationId: Long, therapistId: Long, sessionCount: Int, count: Int) =
        repository.buyClinicSeats(organizationId, therapistId, sessionCount, count)
}

class GetClinicSeatsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(organizationId: Long) = repository.listClinicSeats(organizationId)
}

class AssignClinicSeatUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(organizationId: Long, therapistId: Long, email: String) =
        repository.assignClinicSeat(organizationId, therapistId, email)
}
