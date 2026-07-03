package com.kazemieh.domain.clinic

import com.kazemieh.common.AppResult

interface ClinicRepository {
    suspend fun getTherapists(): AppResult<List<TherapistSummary>>
    suspend fun getTherapist(slug: String): AppResult<TherapistDetail>
    suspend fun getMyAppointments(): AppResult<List<Appointment>>
    suspend fun book(slotId: Long, notes: String?): AppResult<Appointment>
    suspend fun cancel(appointmentId: Long): AppResult<Unit>
}
