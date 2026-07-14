package com.kazemieh.domain.clinic

data class TherapistSummary(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val photoUrl: String?,
    val sessionPrice: Double,
    val availableSlotCount: Int,
    val requiresPurchase: Boolean,
    val productSlug: String?,
    val sessionCredits: Int
)

data class AvailabilitySlot(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val dayLabel: String,
    val timeLabel: String,
    val capacity: Int = 1,
    val remainingCapacity: Int = 1
) {
    /** ظرفیت بیش‌تر از ۱ یعنی این بازه یک جلسه‌ی گروهی است. */
    val isGroupSession: Boolean get() = capacity > 1
}

data class TherapistDetail(
    val id: Long,
    val name: String,
    val slug: String,
    val specialty: String?,
    val bio: String?,
    val photoUrl: String?,
    val sessionPrice: Double,
    val sessionDurationMinutes: Int,
    val slots: List<AvailabilitySlot>,
    val requiresPurchase: Boolean,
    val productSlug: String?,
    val sessionCredits: Int,
    val mode: String = "ONLINE",
    val location: String? = null,
    val productId: Long? = null
) {
    /** آیا کاربر می‌تواند همین حالا رزرو کند (رایگان است یا اعتبارِ کافی دارد). */
    val canBook: Boolean get() = !requiresPurchase || sessionCredits > 0

    /** برچسبِ فارسیِ نحوه‌ی برگزاری. */
    val modeLabel: String get() = when (mode) {
        "IN_PERSON" -> "حضوری"; "PHONE" -> "تلفنی"; else -> "آنلاین"
    }
    val isInPerson: Boolean get() = mode == "IN_PERSON"
}

enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED, UNKNOWN }

data class Appointment(
    val id: Long,
    val therapistName: String,
    val therapistPhotoUrl: String?,
    val status: AppointmentStatus,
    val dayLabel: String,
    val timeLabel: String,
    /** برای حالتِ ONLINE: لینکِ اتاقِ تماس. برای PHONE: همین فیلد شماره‌تماس را نگه می‌دارد. */
    val videoRoomUrl: String?,
    val canJoin: Boolean,
    val notes: String?,
    val mode: String = "ONLINE"
) {
    val isPhone: Boolean get() = mode == "PHONE"
}

// ---------- ثبتِ روزانه‌ی خلق‌وخو (Phase X) ----------
data class MoodCheckIn(
    val id: Long,
    val moodScore: Int,
    val note: String?,
    val createdAt: String?
)

// ---------- درخواستِ تعویضِ درمانگر (Phase X) ----------
enum class SwitchRequestStatus { PENDING, APPROVED, REJECTED, UNKNOWN }

data class SwitchRequest(
    val id: Long,
    val fromTherapistId: Long,
    val fromTherapistName: String,
    val toTherapistId: Long?,
    val toTherapistName: String?,
    val reason: String?,
    val status: SwitchRequestStatus,
    val adminNote: String?,
    val createdAt: String?
)

// ---------- رسیدِ جلسه، آماده برایِ ارائه به بیمه (Phase X) ----------
data class SessionReceipt(
    val appointmentId: Long,
    val patientName: String,
    val therapistName: String,
    val therapistSpecialty: String?,
    val sessionMode: String,
    val sessionDate: String,
    val sessionDurationMinutes: Int,
    val amountPaid: Double
)

// ---------- پیام‌رسانیِ امنِ بینِ‌جلسه‌ای (Phase Y) ----------
enum class MessageSenderType { PATIENT, THERAPIST, UNKNOWN }

data class ClinicMessage(
    val id: Long,
    val senderType: MessageSenderType,
    val body: String,
    val createdAt: String?
)

data class MessagingPlanStatus(
    val therapistId: Long,
    val active: Boolean,
    val freeMessagesRemaining: Int
)

// ---------- تکلیف/تمرینِ بینِ‌جلسه‌ای (Phase Y) ----------
enum class HomeworkStatus { ASSIGNED, COMPLETED, UNKNOWN }

data class Homework(
    val id: Long,
    val therapistId: Long,
    val therapistName: String,
    val title: String,
    val description: String?,
    val status: HomeworkStatus,
    val dueDate: String?,
    val completedAt: String?,
    val createdAt: String?
)

// ---------- یادداشتِ روزانه (ژورنال) — Phase Y ----------
data class JournalEntry(
    val id: Long,
    val content: String,
    val sharedWithTherapistId: Long?,
    val createdAt: String?
)

// ---------- پرسشنامه‌ی تطبیقِ درمانگر (Phase Y) ----------
data class TherapistMatchQuestion(
    val id: Long,
    val questionText: String,
    val tag: String
)

data class TherapistMatchResult(
    val therapist: TherapistSummary,
    val matchScore: Int
)
