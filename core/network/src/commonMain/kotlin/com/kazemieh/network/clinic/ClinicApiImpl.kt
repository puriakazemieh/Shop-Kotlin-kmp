package com.kazemieh.network.clinic

import com.kazemieh.network.clinic.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class ClinicApiImpl(
    private val client: HttpClient
) : ClinicApi {

    override suspend fun getTherapists(): List<TherapistSummaryResponse> = safeApiCallRaw {
        client.get("api/therapists")
    }

    override suspend fun getTherapist(slug: String): TherapistDetailResponse = safeApiCallRaw {
        client.get("api/therapists/$slug")
    }

    override suspend fun getMyAppointments(): List<AppointmentResponse> = safeApiCallRaw {
        client.get("api/clinic/my-appointments")
    }

    override suspend fun book(request: BookAppointmentRequestDto): AppointmentResponse = safeApiCallRaw {
        client.post("api/clinic/appointments") {
            setBody(request)
        }
    }

    override suspend fun cancel(appointmentId: Long): Unit = safeApiCallRaw {
        client.post("api/clinic/appointments/$appointmentId/cancel")
    }
}
