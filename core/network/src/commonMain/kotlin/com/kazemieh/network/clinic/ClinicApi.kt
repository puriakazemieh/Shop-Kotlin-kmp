package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*

interface ClinicApi {
    suspend fun getTherapists(): List<TherapistSummaryResponse>
    suspend fun getTherapist(slug: String): TherapistDetailResponse
    suspend fun getMyAppointments(): List<AppointmentResponse>
    suspend fun book(request: BookAppointmentRequestDto): AppointmentResponse
    suspend fun cancel(appointmentId: Long)
}
