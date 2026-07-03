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
