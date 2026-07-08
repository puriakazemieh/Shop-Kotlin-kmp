package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*

interface AdminClinicApi {
    suspend fun listTherapists(): List<TherapistSummaryResponse>
    suspend fun createTherapist(request: AdminCreateTherapistRequestDto): Long
    suspend fun updateTherapist(id: Long, request: AdminUpdateTherapistRequestDto)
    suspend fun deleteTherapist(id: Long)
    suspend fun addSlot(therapistId: Long, request: AdminAddSlotRequestDto): Long
    suspend fun generateSlots(therapistId: Long, request: AdminGenerateSlotsRequestDto): Int
    suspend fun listSlots(therapistId: Long): List<AdminSlotResponse>
    suspend fun listAppointments(): List<AdminAppointmentResponse>
    suspend fun confirmAppointment(id: Long, request: AdminConfirmAppointmentRequestDto)
    suspend fun completeAppointment(id: Long)
    suspend fun listPatientNotes(appointmentId: Long): List<PatientNoteResponse>
    suspend fun addPatientNote(appointmentId: Long, request: AdminAddPatientNoteRequestDto): Long

    suspend fun listPatients(therapistId: Long): List<AdminPatientSummaryResponse>
    suspend fun setPatientTags(therapistId: Long, userId: Long, request: AdminSetPatientTagsRequestDto)
    suspend fun getPatientFile(therapistId: Long, userId: Long): PatientFileResponse

    suspend fun listSwitchRequests(): List<AdminSwitchRequestResponse>
    suspend fun reviewSwitchRequest(id: Long, request: AdminReviewSwitchRequestDto): AdminSwitchRequestResponse
}
