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
}
