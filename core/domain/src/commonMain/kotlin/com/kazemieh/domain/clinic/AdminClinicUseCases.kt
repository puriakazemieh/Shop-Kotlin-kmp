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
    suspend operator fun invoke(therapistId: Long, startTime: String, endTime: String) =
        repository.addSlot(therapistId, startTime, endTime)
}

class GenerateSlotsUseCase(private val repository: AdminClinicRepository) {
    suspend operator fun invoke(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: Int?) =
        repository.generateSlots(therapistId, windowStart, windowEnd, slotMinutes)
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
