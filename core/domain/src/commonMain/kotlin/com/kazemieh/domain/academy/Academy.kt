package com.kazemieh.domain.academy

/** نوعِ فعالیت و شکلِ برگزاری (هم‌ارز با enumهای سرور). */
enum class CourseType { COURSE, SEMINAR, WORKSHOP;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: COURSE }
    val label: String get() = when (this) {
        COURSE -> "دوره"; SEMINAR -> "سمینار"; WORKSHOP -> "کارگاه"
    }
}

enum class CourseFormat { ONLINE_RECORDED, ONLINE_LIVE, IN_PERSON, OFFLINE;
    companion object { fun from(v: String?) = entries.firstOrNull { it.name == v } ?: ONLINE_RECORDED }
    val isOnline: Boolean get() = this == ONLINE_RECORDED || this == ONLINE_LIVE
    val label: String get() = when (this) {
        ONLINE_RECORDED -> "آنلاین (ضبط‌شده)"; ONLINE_LIVE -> "آنلاین (زنده)"
        IN_PERSON -> "حضوری"; OFFLINE -> "آفلاین"
    }
}

data class CourseSummary(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val lessonCount: Int,
    val enrolled: Boolean,
    val courseType: CourseType = CourseType.COURSE,
    val format: CourseFormat = CourseFormat.ONLINE_RECORDED,
    val isOnline: Boolean = true,
    val level: String? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val hasUnseenUpdate: Boolean = false
)

data class VideoVariant(
    val quality: String,
    val url: String
)

/** یک فایلِ ضمیمه‌ی درس (جزوه/کدِ نمونه/...) — کنارِ ویدیو، نه به‌جایِ آن. */
data class LessonFile(
    val name: String,
    val url: String,
    val sizeLabel: String? = null
)

data class Lesson(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val isFreePreview: Boolean,
    val videoUrl: String?,
    val completed: Boolean,
    val lastPositionSeconds: Int,
    val videoVariants: List<VideoVariant> = emptyList(),
    val resourceFiles: List<LessonFile> = emptyList(),
    /** آیا این درس آزمونِ کوتاهِ خودش را دارد (تبِ «آزمون» در پخش‌کننده). */
    val hasQuiz: Boolean = false
)

data class CourseSection(
    val id: Long,
    val title: String,
    val lessons: List<Lesson>
)

data class CourseDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val thumbnailUrl: String?,
    val instructor: String?,
    val price: Double,
    val discountedPrice: Double?,
    val enrolled: Boolean,
    val progressPercent: Int,
    val sections: List<CourseSection>,
    val courseType: CourseType = CourseType.COURSE,
    val format: CourseFormat = CourseFormat.ONLINE_RECORDED,
    val isOnline: Boolean = true,
    val level: String? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val seatsTaken: Int = 0,
    val seatsRemaining: Int? = null,
    val jobMarketBadge: Boolean = false,
    val freeUpdateBadge: Boolean = false,
    val instructorBio: String? = null,
    val instructorSkills: List<String> = emptyList(),
    val isFull: Boolean = false,
    val onWaitlist: Boolean = false,
    val productId: Long? = null,
    /** آیا صدورِ گواهی نیازمندِ تأییدِ پروژه‌ی پایانی هم هست (کنارِ قبولیِ آزمون). */
    val requiresProjectSubmission: Boolean = false,
    /** کدِ تخفیفِ اختصاصیِ مدرس (اگر ادمین تنظیم کرده باشد). */
    val instructorDiscountCode: String? = null,
    /** جعبه‌ی «این دوره شامل چیست» — مجموعِ مدتِ ویدیوها و تعدادِ فایل‌های ضمیمه. */
    val totalDurationSeconds: Int = 0,
    val resourceFileCount: Int = 0,
    val hasUnseenUpdate: Boolean = false
)

data class CourseProgress(
    val courseId: Long,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercent: Int
)

// ---------- Quiz ----------
data class QuizOption(val text: String)

data class QuizQuestion(
    val index: Int,
    val text: String,
    val options: List<QuizOption>
)

data class Quiz(
    val courseId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestion>,
    val alreadyPassed: Boolean
)

data class QuizResult(
    val courseId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int,
    val certificateNumber: String?
)

// ---------- Waitlist ----------
data class WaitlistResult(
    val courseId: Long,
    val joined: Boolean,
    val position: Int?
)

// ---------- Certificate ----------
data class Certificate(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val certNumber: String,
    val issuedAt: String,
    val userName: String?
)

// ---------- Lesson quiz (checkpoint per lesson, separate from the course-final quiz) ----------
data class LessonQuiz(
    val lessonId: Long,
    val title: String,
    val passScore: Int,
    val questions: List<QuizQuestion>,
    val alreadyPassed: Boolean = false
)

data class LessonQuizResult(
    val lessonId: Long,
    val score: Int,
    val passed: Boolean,
    val passScore: Int
)

// ---------- Project-based assessment ----------
data class ProjectSubmission(
    val id: Long,
    val courseId: Long,
    val userId: Long,
    val fileUrl: String,
    val note: String?,
    val status: String,
    val mentorFeedback: String?,
    val submittedAt: String,
    val reviewedAt: String? = null,
    val userName: String? = null
)

// ---------- پرسش‌وپاسخِ درس (Phase V) ----------
data class LessonQuestion(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val content: String,
    val parentId: Long?,
    val createdAt: String?
)

// ---------- نقدِ همتایان (Phase V) ----------
data class PeerComment(
    val id: Long,
    val userId: Long,
    val userName: String,
    val comment: String,
    val createdAt: String?
)

// ---------- تاییدِ گواهی (Phase V) ----------
data class CertificateVerification(
    val valid: Boolean,
    val courseTitle: String?,
    val certNumber: String?,
    val issuedAt: String?
)

// ---------- آزمونِ تعیینِ سطح (Phase V) ----------
data class PlacementQuizOption(val label: String, val score: Int)
data class PlacementQuizQuestion(val id: Int, val text: String, val options: List<PlacementQuizOption>)
data class PlacementQuizResult(val level: String, val label: String)
