package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class AdminClinicApiImpl(
    private val client: HttpClient
) : AdminClinicApi {

    override suspend fun listTherapists(): List<TherapistSummaryResponse> = safeApiCallRaw {
        client.get("api/admin/therapists")
    }

    override suspend fun createTherapist(request: AdminCreateTherapistRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists") { setBody(request) }
        }.id

    override suspend fun updateTherapist(id: Long, request: AdminUpdateTherapistRequestDto): Unit = safeApiCallRaw {
        client.patch("api/admin/therapists/$id") { setBody(request) }
    }

    override suspend fun deleteTherapist(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/therapists/$id")
    }

    override suspend fun addSlot(therapistId: Long, request: AdminAddSlotRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists/$therapistId/slots") { setBody(request) }
        }.id

    override suspend fun generateSlots(therapistId: Long, request: AdminGenerateSlotsRequestDto): Int =
        safeApiCallRaw<CreatedCountResponse> {
            client.post("api/admin/therapists/$therapistId/generate-slots") { setBody(request) }
        }.created

    override suspend fun listSlots(therapistId: Long): List<AdminSlotResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/$therapistId/slots")
    }

    override suspend fun listAppointments(): List<AdminAppointmentResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/appointments")
    }

    override suspend fun confirmAppointment(id: Long, request: AdminConfirmAppointmentRequestDto): Unit = safeApiCallRaw {
        client.post("api/admin/therapists/appointments/$id/confirm") { setBody(request) }
    }

    override suspend fun completeAppointment(id: Long): Unit = safeApiCallRaw {
        client.post("api/admin/therapists/appointments/$id/complete")
    }

    override suspend fun listPatientNotes(appointmentId: Long): List<PatientNoteResponse> = safeApiCallRaw {
        client.get("api/admin/therapists/appointments/$appointmentId/notes")
    }

    override suspend fun addPatientNote(appointmentId: Long, request: AdminAddPatientNoteRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/therapists/appointments/$appointmentId/notes") { setBody(request) }
        }.id
}
