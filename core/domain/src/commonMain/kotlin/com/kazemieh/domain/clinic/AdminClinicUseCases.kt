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
