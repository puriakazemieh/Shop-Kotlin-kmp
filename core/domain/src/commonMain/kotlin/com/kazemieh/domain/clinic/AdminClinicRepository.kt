package com.kazemieh.domain.clinic

import com.kazemieh.common.AppResult

data class AdminTherapistParams(
    val name: String,
    val slug: String,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double = 0.0,
    val sessionDurationMinutes: Int = 45,
    val productId: Long? = null,
    val messagingProductId: Long? = null,
    val isActive: Boolean = true,
    val mode: String = "ONLINE",
    val location: String? = null
)

data class AdminTherapistUpdateParams(
    val name: String? = null,
    val specialty: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val sessionPrice: Double? = null,
    val sessionDurationMinutes: Int? = null,
    val isActive: Boolean? = null,
    val messagingProductId: Long? = null
)

data class AdminSlot(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean,
    val capacity: Int = 1,
    val bookedCount: Int = 0
)

enum class AdminAppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED, UNKNOWN }

data class AdminAppointment(
    val id: Long,
    val userId: Long,
    val therapistId: Long,
    val therapistName: String,
    val status: AdminAppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    val videoRoomUrl: String?,
    val notes: String?,
    val mode: String = "ONLINE"
) {
    val isPhone: Boolean get() = mode == "PHONE"
}

data class PatientNote(
    val id: Long,
    val appointmentId: Long,
    val counselorId: Long,
    val note: String,
    val createdAt: String
)

/** یک ردیفِ لیستِ مراجعانِ یک درمانگر (کارتِ CRM). */
data class AdminPatientSummary(
    val userId: Long,
    val userName: String,
    val therapistId: Long,
    val appointmentCount: Int,
    val lastAppointmentAt: String?,
    val tags: List<String> = emptyList()
)

data class PatientFileAppointment(
    val id: Long,
    val status: AdminAppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    val notes: List<PatientNote> = emptyList()
)

data class PatientFileTestResult(
    val testTitle: String,
    val totalScore: Int?,
    val interpretation: String?,
    val completedAt: String?
)

/** پرونده‌ی کاملِ مراجع: نوبت‌ها + یادداشت‌ها + نتایجِ تست، همه در یک نما. */
data class PatientFile(
    val userId: Long,
    val userName: String,
    val therapistId: Long,
    val tags: List<String> = emptyList(),
    val appointments: List<PatientFileAppointment> = emptyList(),
    val testResults: List<PatientFileTestResult> = emptyList()
)

/** درخواستِ تعویضِ درمانگر از دیدِ ادمین (Phase X). */
data class AdminSwitchRequest(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val fromTherapistId: Long,
    val fromTherapistName: String,
    val toTherapistId: Long?,
    val toTherapistName: String?,
    val reason: String?,
    val status: SwitchRequestStatus,
    val adminNote: String?,
    val createdAt: String?
)

/** یک سؤالِ پرسشنامه‌ی تطبیقِ درمانگر برایِ مدیریتِ ادمین (Phase Y). */
data class AdminMatchQuestionParams(
    val questionText: String,
    val tag: String,
    val displayOrder: Int = 0
)

/** بسته‌ی مشاوره‌ی سازمانی (Phase Y). */
data class ClinicOrgSeat(
    val id: Long,
    val organizationId: Long,
    val therapistId: Long,
    val sessionCount: Int,
    val assignedUserId: Long?,
    val assignedEmail: String?,
    val assignedAt: String?
)

interface AdminClinicRepository {
    suspend fun listTherapists(): AppResult<List<TherapistSummary>>
    suspend fun createTherapist(params: AdminTherapistParams): AppResult<Long>
    suspend fun updateTherapist(id: Long, params: AdminTherapistUpdateParams): AppResult<Unit>
    suspend fun deleteTherapist(id: Long): AppResult<Unit>
    /** startTime/endTime باید در قالبِ ISO-8601 با آفستِ زمانی باشند (مثلاً 2026-07-10T14:00:00+03:30). */
    suspend fun addSlot(therapistId: Long, startTime: String, endTime: String, capacity: Int = 1): AppResult<Long>
    /** تولیدِ خودکارِ بازه‌ها از یک بازه‌ی کاری (ISO-8601). خروجی: تعدادِ بازه‌ی ساخته‌شده. */
    suspend fun generateSlots(therapistId: Long, windowStart: String, windowEnd: String, slotMinutes: Int?, capacity: Int = 1): AppResult<Int>
    suspend fun listSlots(therapistId: Long): AppResult<List<AdminSlot>>
    suspend fun listAppointments(): AppResult<List<AdminAppointment>>
    suspend fun confirmAppointment(id: Long, videoRoomUrl: String): AppResult<Unit>
    suspend fun completeAppointment(id: Long): AppResult<Unit>
    suspend fun listPatientNotes(appointmentId: Long): AppResult<List<PatientNote>>
    suspend fun addPatientNote(appointmentId: Long, note: String): AppResult<Long>

    // ---- CRMِ سبکِ مراجعان + پرونده‌ی کاملِ مراجع ----
    suspend fun listPatients(therapistId: Long): AppResult<List<AdminPatientSummary>>
    suspend fun setPatientTags(therapistId: Long, userId: Long, tags: List<String>): AppResult<Unit>
    suspend fun getPatientFile(therapistId: Long, userId: Long): AppResult<PatientFile>

    // ---- درخواست‌های تعویضِ درمانگر ----
    suspend fun listSwitchRequests(): AppResult<List<AdminSwitchRequest>>
    suspend fun reviewSwitchRequest(id: Long, approve: Boolean, adminNote: String?): AppResult<AdminSwitchRequest>

    // ---- پیام‌رسانی/تکلیف/ژورنالِ به‌اشتراک‌گذاشته‌شده (به‌عنوانِ درمانگر) ----
    suspend fun listMessagesWithPatient(therapistId: Long, userId: Long): AppResult<List<ClinicMessage>>
    suspend fun sendMessageToPatient(therapistId: Long, userId: Long, body: String): AppResult<ClinicMessage>
    suspend fun listHomeworkForPatient(therapistId: Long, userId: Long): AppResult<List<Homework>>
    suspend fun assignHomework(therapistId: Long, userId: Long, title: String, description: String?, dueDate: String?): AppResult<Homework>
    suspend fun sharedJournal(therapistId: Long, userId: Long): AppResult<List<JournalEntry>>

    // ---- مدیریتِ پرسشنامه‌ی تطبیقِ درمانگر ----
    suspend fun listMatchQuestions(): AppResult<List<TherapistMatchQuestion>>
    suspend fun createMatchQuestion(params: AdminMatchQuestionParams): AppResult<TherapistMatchQuestion>
    suspend fun deleteMatchQuestion(id: Long): AppResult<Unit>

    // ---- بسته‌ی مشاوره‌ی سازمانی ----
    suspend fun buyClinicSeats(organizationId: Long, therapistId: Long, sessionCount: Int, count: Int): AppResult<List<ClinicOrgSeat>>
    suspend fun listClinicSeats(organizationId: Long): AppResult<List<ClinicOrgSeat>>
    suspend fun assignClinicSeat(organizationId: Long, therapistId: Long, email: String): AppResult<ClinicOrgSeat>
}
